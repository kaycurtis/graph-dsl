import com.google.common.collect.ImmutableMap;
import model.Algorithm;
import model.Edge;
import model.Node;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.*;
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

    private static void doAnimation(Demo demo, Graph graph, Map<Node, org.graphstream.graph.Node> nodeMap) {
        graph.display();
        int i = 0;
        Node current = demo.getStart();
        while (true) {
            nodeMap.get(current).setAttribute("ui.style", "fill-color: grey;size: 30px;");
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
    public static Node bfs(Demo demo, int i) {
        Node current = demo.getStart();
        Queue<Node> toTraverse = new LinkedList<>();
        toTraverse.add(current);
        for (int j = 0; j <= i; j++) {
            if (toTraverse.isEmpty()) {
                return null;
            }
            current = toTraverse.remove();
            if (current.equals(demo.getEnd())) {
                return current;
            }
            toTraverse.addAll(accessibleFrom(current, demo.getGraph().getEdges()));
        }
        return current;
    }

    /**
     * Return either:
     * - the node processed on the ith iteration of the DFS algorithm
     * - the destination node if it is reached before iteration i
     * - null if the algorithm fails before iteration i
     */
    public static Node dfs(Demo demo, int i) {
        Node current = demo.getStart();
        Stack<Node> toTraverse = new Stack<>();
        toTraverse.push(current);
        for (int j = 0; j <= i; j++) {
            if (toTraverse.isEmpty()) {
                return null;
            }
            current = toTraverse.pop();
            if (current.equals(demo.getEnd())) {
                return current;
            }
            accessibleFrom(current, demo.getGraph().getEdges()).forEach(toTraverse::push);
        }
        return current;
    }

    public static java.util.List<Node> accessibleFrom(Node begin, java.util.List<Edge> edges) {
        return edges.stream()
                .filter(edge -> edge.getStart().equals(begin))
                .map(Edge::getEnd)
                .collect(Collectors.toList());
    }
}
