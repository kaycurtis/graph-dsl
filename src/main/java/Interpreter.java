import com.google.common.collect.ImmutableMap;
import model.Algorithm;
import model.Demo;
import model.Edge;
import model.Node;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Interpreter {

    // TODO add to this after POC
    private static final Map<Algorithm, BiFunction<Demo, Integer, Node>> TRAVERSAL_FUNCTIONS = ImmutableMap.of(
            Algorithm.BFS, Interpreter::bfs,
            Algorithm.DFS, Interpreter::dfs
    );

    private static final int DISPLAY_SECONDS = 3;

    public static void interpret(Demo demo) {

        validateDemo(demo);

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Graph graph = new SingleGraph("Graph");
        Map<Node, org.graphstream.graph.Node> nodeMap = new HashMap<>();
        for (Node node : demo.getGraph().getNodes()) {
            org.graphstream.graph.Node gNode = graph.addNode(node.getName());
            gNode.addAttribute("ui.style", "fill-color: grey;size: 30px;");
            gNode.addAttribute("ui.label", node.getName());
            nodeMap.put(node, gNode);
        }

        for (Edge edge : demo.getGraph().getEdges()) {
            graph.addEdge(edge.getStart().getName() + edge.getEnd().getName(), edge.getStart().getName(), edge.getEnd().getName(), true);
        }
        doAnimation(demo, graph, nodeMap);
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

    private static void doAnimation(Demo demo, Graph graph, Map<Node, org.graphstream.graph.Node> nodeMap) {
        graph.display();
        int i = 0;
        Node current = null;
        while (true) {
            if (current != null) {
                nodeMap.get(current).setAttribute("ui.style", "fill-color: green;size: 30px;");
            }
            current = TRAVERSAL_FUNCTIONS.get(demo.getAlgorithm()).apply(demo, i);
            if (current == null) {
                display();
                return;
            }
            if (current.equals(demo.getEnd())) {
                nodeMap.get(current).setAttribute("ui.style", "fill-color: yellow;size: 30px;");
                display();
                return;
            }
            nodeMap.get(current).setAttribute("ui.style", "fill-color: red;size: 30px;");
            i++;
            display();
        }
    }

    private static void display() {
        DateTime startTime = new DateTime();
        while (Seconds.secondsBetween(startTime, new DateTime()).getSeconds() < DISPLAY_SECONDS) {
            // loop
        }
    }

    /**
     * Return either:
     * - the node processed on the ith iteration of the BFS algorithm
     * - the destination node if it is reached before iteration i
     * - null if the algorithm fails before iteration i
     */
    private static Node bfs(Demo demo, int i) {
        Node current = demo.getStart();
        Queue<Node> toTraverse = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        toTraverse.add(current);
        for (int j = 0; j <= i; j++) {
            if (toTraverse.isEmpty()) {
                return null;
            }
            current = toTraverse.remove();
            if (!visited.contains(current)) {
                if (current.equals(demo.getEnd())) {
                    return current;
                }
                toTraverse.addAll(accessibleFrom(current, demo.getGraph().getEdges()));
                visited.add(current);
            } else {
                j--; // we already visited this node, doesn't count
            }
        }
        return current;
    }

    /**
     * Return either:
     * - the node processed on the ith iteration of the DFS algorithm
     * - the destination node if it is reached before iteration i
     * - null if the algorithm fails before iteration i
     */
    private static Node dfs(Demo demo, int i) {
        Node current = demo.getStart();
        Stack<Node> toTraverse = new Stack<>();
        toTraverse.push(current);
        Set<Node> visited = new HashSet<>();
        for (int j = 0; j <= i; j++) {
            if (toTraverse.isEmpty()) {
                return null;
            }
            current = toTraverse.pop();
            if (!visited.contains(current)) {
                if (current.equals(demo.getEnd())) {
                    return current;
                }
                accessibleFrom(current, demo.getGraph().getEdges()).forEach(toTraverse::push);
                visited.add(current);
            } else {
                j--;
            }
        }
        return current;
    }

    public static java.util.List<Node> accessibleFrom(Node begin, java.util.List<Edge> edges) {
        return edges.stream()
                .filter(edge -> edge.getStart().equals(begin))
                .map(Edge::getEnd)
                .collect(Collectors.toList());
    }

    private static class InterpreterException extends RuntimeException {
        public InterpreterException(String message) {
        }
    }
}
