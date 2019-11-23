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

import java.util.ArrayList;
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

    private static Graph ds = new SingleGraph("Data Structure");

    private static final Map<Algorithm, Supplier<Snapshot>> SNAPSHOT_SUPPLIERS = ImmutableMap.of(
            Algorithm.DFS, DfsSnapshot::new,
            Algorithm.BFS, BfsSnapshot::new);

    // might need to make this a function if we need more arguments for djikstra
    private static final Map<Algorithm, BiConsumer<Demo, GraphStreamGraph>> ANIMATION_FUNCTION_SUPPLIERS = ImmutableMap.of(
            Algorithm.DFS, Interpreter::doSearchAnimation,
            Algorithm.BFS, Interpreter::doSearchAnimation,
            Algorithm.DIJKSTRAS, Interpreter::doDjikstraAnimation
    );

    private static final int SLOW_STEP_SECONDS = 3;
    private static final int FAST_STEP_SECONDS = 1;

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
            ds.addNode(node.getName());
        }
        for (Edge edge : demo.getGraph().getEdges()) {
            org.graphstream.graph.Edge gEdge = graph.addEdge(edge.getStart().getName() + edge.getEnd().getName(),
                    edge.getStart().getName(), edge.getEnd().getName(), true);
            if (edge.getWeight() != null) {
                gEdge.setAttribute("weight", edge.getWeight());
                gEdge.addAttribute("ui.label", edge.getWeight());
            }
            edgeMap.put(edge, gEdge);
        }
        Node firstNode = demo.getGraph().getNodes().get(0);
        for (int i = 1; i < demo.getGraph().getNodes().size(); i++) {
            Node secondNode = demo.getGraph().getNodes().get(i);
            ds.addEdge(""+i+"", firstNode.getName(), secondNode.getName());
            firstNode = demo.getGraph().getNodes().get(i);
        }

        graph.display();
        ds.display();
        display(SLOW_STEP_SECONDS);
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
                display(SLOW_STEP_SECONDS);
                return;
            }
            if (current.equals(demo.getEnd())) {
                nodes.get(current).setAttribute("ui.style", "fill-color: yellow;size: 30px;");
                display(SLOW_STEP_SECONDS);
                return;
            }
            nodes.get(current).setAttribute("ui.style", "fill-color: red;size: 30px;");
            display(SLOW_STEP_SECONDS);
        }
    }

    private static void doDjikstraAnimation(Demo demo, GraphStreamGraph graphStreamGraph) {
        DijkstraSnapshot dijkstraSnapshot = new DijkstraSnapshot(demo);
        while (true) {
            List<Edge> oldPath = new ArrayList<>(dijkstraSnapshot.getPath());
            graphStreamGraph.getEdges().values().forEach(edge ->
                    edge.setAttribute("ui.style", "fill-color: black;"));
            dijkstraSnapshot = search(demo, dijkstraSnapshot);
            if (dijkstraSnapshot.getCurrent() == null) {
                display(SLOW_STEP_SECONDS);
                return;
            }
            for (Edge edge : dijkstraSnapshot.getPath()) {
                graphStreamGraph.getEdge(edge).setAttribute("ui.style", "fill-color: green;");
                if (!oldPath.contains(edge)) {
                    display(FAST_STEP_SECONDS);
                }
            }
            if (dijkstraSnapshot.getCurrent().equals(demo.getEnd())) {
                graphStreamGraph.getNode(dijkstraSnapshot.getCurrent()).setAttribute("ui.style", "fill-color: yellow;size: 30px;");
                getPath(demo.getStart(), demo.getEnd(), dijkstraSnapshot.getPath()).forEach(edge ->
                        graphStreamGraph.getEdge(edge).setAttribute("ui.style", "fill-color: red;"));
                display(SLOW_STEP_SECONDS);
                return;
            }
        }
    }

    private static List<Edge> getPath(Node start, Node end, List<Edge> path) {
        List<Edge> edges = new ArrayList<>();
        Node current = end;
        while (!current.equals(start)) {
            Edge edgeEndingInCurrent = getEdgeTo(path, current);
            edges.add(edgeEndingInCurrent);
            current = edgeEndingInCurrent.getStart();
        }
        return edges;
    }

    private static Edge getEdgeTo(List<Edge> edges, Node node) {
        return edges.stream()
                .filter(edge -> edge.getEnd().equals(node))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("yikes"));
    }

    private static void display(int seconds) {
        DateTime startTime = new DateTime();
        while (Seconds.secondsBetween(startTime, new DateTime()).getSeconds() < seconds) {
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
