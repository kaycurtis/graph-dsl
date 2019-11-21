package interpreter;

import com.google.common.collect.ImmutableMap;
import model.Algorithm;
import model.Demo;
import model.Edge;
import model.Node;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import parser.Parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Interpreter {

    private static final Map<Algorithm, Supplier<Snapshot>> SNAPSHOT_SUPPLIERS = ImmutableMap.of(
            Algorithm.DFS, DfsSnapshot::new,
            Algorithm.BFS, BfsSnapshot::new);

    // might need to make this a function if we need more arguments for djikstra
    private static final Map<Algorithm, BiConsumer<Demo, GraphStreamGraph>> ANIMATION_FUNCTION_SUPPLIERS = ImmutableMap.of(
            Algorithm.DFS, Interpreter::doSearchAnimation,
            Algorithm.BFS, Interpreter::doSearchAnimation,
            Algorithm.DIJKSTRAS, Interpreter::doDjikstraAnimation
    );

    private static final int DISPLAY_SECONDS = 3;

    public static void run(String concrete) {
        Interpreter.interpret(Parser.parse(concrete));
    }
    
    public static void interpret(Demo demo) {
        validateDemo(demo);
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Graph graph = new SingleGraph("Graph");
        Map<Node, org.graphstream.graph.Node> nodeMap = new HashMap<>();
        Map<Edge, org.graphstream.graph.Edge> edgeMap = new HashMap<>();
        for (Node node : demo.getGraph().getNodes()) {
            org.graphstream.graph.Node gNode = graph.addNode(node.getName());
            gNode.addAttribute("ui.style", "fill-color: grey;size: 30px;");
            gNode.addAttribute("ui.label", node.getName());
            nodeMap.put(node, gNode);
        }
        for (Edge edge : demo.getGraph().getEdges()) {
            org.graphstream.graph.Edge gEdge = graph.addEdge(edge.getStart().getName() + edge.getEnd().getName(),
                    edge.getStart().getName(), edge.getEnd().getName(), true);
            if (edge.getWeight() != null) {
                gEdge.setAttribute("weight", edge.getWeight());
            }
            edgeMap.put(edge, gEdge);
        }
        graph.display();
        ANIMATION_FUNCTION_SUPPLIERS.get(demo.getAlgorithm()).accept(demo, GraphStreamGraph.of(nodeMap, edgeMap));
    }

    @SuppressWarnings("unchecked") // i checked
    private static void doSearchAnimation(Demo demo, GraphStreamGraph graphStreamGraph) {
        Map<Node, org.graphstream.graph.Node> nodes = graphStreamGraph.getNodes();

        SearchSnapshot searchSnapshot = (SearchSnapshot) SNAPSHOT_SUPPLIERS.get(demo.getAlgorithm()).get();
        searchSnapshot.getNodeAdder().accept(demo.getStart());

        while (true) {
            Node current = searchSnapshot.getCurrent();
            if (current != null) {
                nodes.get(current).setAttribute("ui.style", "fill-color: green;size: 30px;");
            }
            searchSnapshot = search(demo, searchSnapshot);
            current = searchSnapshot.getCurrent();
            if (current == null) {
                display();
                return;
            }
            if (current.equals(demo.getEnd())) {
                nodes.get(current).setAttribute("ui.style", "fill-color: yellow;size: 30px;");
                display();
                return;
            }
            nodes.get(current).setAttribute("ui.style", "fill-color: red;size: 30px;");
            display();
        }
    }

    private static void doDjikstraAnimation(Demo demo, GraphStreamGraph graphStreamGraph) {
        DijkstraSnapshot dijkstraSnapshot = new DijkstraSnapshot(demo);
        while (true) {
            dijkstraSnapshot.getPath().forEach(edge ->
                    graphStreamGraph.getEdge(edge).setAttribute("ui.style", "fill-color: black;"));
            dijkstraSnapshot = search(demo, dijkstraSnapshot);
            if (dijkstraSnapshot.getCurrent() == null) {
                display();
                return;
            }
            for (Edge edge : dijkstraSnapshot.getPath()) {
                graphStreamGraph.getEdge(edge).setAttribute("ui.style", "fill-color: green;");
            }
            if (dijkstraSnapshot.getCurrent().equals(demo.getEnd())) {
                graphStreamGraph.getNode(dijkstraSnapshot.getCurrent()).setAttribute("ui.style", "fill-color: yellow;size: 30px;");
                display();
                return;
            }
            display();
        }
    }

    private static void display() {
        DateTime startTime = new DateTime();
        while (Seconds.secondsBetween(startTime, new DateTime()).getSeconds() < DISPLAY_SECONDS) {
            // loop
        }
    }

    private static <T extends Collection<Node>> SearchSnapshot search(Demo demo, SearchSnapshot<T> searchSnapshot) {
        T toTraverse = searchSnapshot.getToTraverse();
        Set<Node> visited = searchSnapshot.getVisited();
        if (toTraverse.isEmpty()) {
            searchSnapshot.setCurrent(null);
            return searchSnapshot;
        }
        Node current = searchSnapshot.getNextNodeProvider().get();
        if (!visited.contains(current)) {
            searchSnapshot.setCurrent(current);
            if (current.equals(demo.getEnd())) {
                visited.add(current);
                return searchSnapshot;
            }
            accessibleFrom(current, demo.getGraph().getEdges())
                    .forEach(node -> searchSnapshot.getNodeAdder().accept(node));
            visited.add(current);
            return searchSnapshot;
        }
        // already visited this node
        return search(demo, searchSnapshot);
    }

    private static DijkstraSnapshot search(Demo demo, DijkstraSnapshot dijkstraSnapshot) {
        List<Node> toTraverse = dijkstraSnapshot.getToTraverse();
        if (toTraverse.isEmpty()) {
            dijkstraSnapshot.setCurrent(null);
            return dijkstraSnapshot;
        }
        Node current = dijkstraSnapshot.getNext();
        System.out.println(current);
        dijkstraSnapshot.setCurrent(current);
        if (current.equals(demo.getEnd())) {
            return dijkstraSnapshot;
        }
        for (Edge edge: outgoingEdges(current, demo.getGraph().getEdges())) {
            double alt = dijkstraSnapshot.getDist().get(current) + edge.getWeight();
            if (alt < dijkstraSnapshot.getDist().get(edge.getEnd())) {
                dijkstraSnapshot.getDist().put(edge.getEnd(), alt);
                dijkstraSnapshot.insertEdge(edge);
            }
        }
        return dijkstraSnapshot;
    }

    public static List<Node> accessibleFrom(Node begin, List<Edge> edges) {
        return edges.stream()
                .filter(edge -> edge.getStart().equals(begin))
                .map(Edge::getEnd)
                .collect(Collectors.toList());
    }

    public static List<Edge> outgoingEdges(Node begin, List<Edge> edges) {
        return edges.stream()
                .filter(edge -> edge.getStart().equals(begin))
                .collect(Collectors.toList());
    }

    private static void validateDemo(Demo demo) {
        model.Graph graph = demo.getGraph();
        List<Edge> edges = graph.getEdges();
        if (new HashSet<>(edges).size() != edges.size()) {
            throw new InterpreterException("Duplicate edges");
        }
        List<Node> nodes = graph.getNodes();
        if (new HashSet<>(nodes).size() != nodes.size()) {
            throw new InterpreterException("Duplicate nodes");
        }
        for (Edge edge: edges) {
            if (!nodes.contains(edge.getStart())) {
                throw new InterpreterException("Node " + edge.getStart() + " does not exist");
            }
            if (!nodes.contains(edge.getEnd())) {
                throw new InterpreterException("Node " + edge.getEnd() + " does not exist");
            }
        }
        if (!nodes.contains(demo.getStart())) {
            throw new InterpreterException("Demo must begin with a node that exists in the graph");
        }
        // non-existent end node is ok
    }
}
