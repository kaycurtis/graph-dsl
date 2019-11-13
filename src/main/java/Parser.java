import com.google.common.collect.ImmutableList;
import model.Algorithm;
import model.Demo;
import model.Edge;
import model.Graph;
import model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    /**
     * EBNF:
     *
     * <Demo>        ::= {do <Algorithm> on <Graph> from <Node> to <Node>}
     * <Graph>       ::= {graph {<Nodes>} {<Edges>}}
     * <Node>        ::= <string>
     * <Edge>        ::= {<Node> to <Node>}
     *                  | {<Node> to <Node> <number>}
     * <Nodes>       ::=
     *                  | <Node> <Nodes>
     * <Edges>       ::=
     *                  | <Edge> <Edges>
     * <Algorithm>   ::= DFS
     *                  | BFS
     *                  | Dijkstra's
     */

    private static final List<String> RESERVED = ImmutableList.of("to", "do", "on", "from", "graph", "DFS", "BFS", "DIJKSTRAS");

    private static final Pattern LIST_PATTERN = Pattern.compile("\\{(.*)\\}");
    private static final Pattern NODE_PATTERN = Pattern.compile("([A-Za-z0-9]+)");
    private static final Pattern EDGE_PATTERN = Pattern.compile("\\{(.*) to (.*)\\}");
    // [\w\s]* is to match node list, hacky way to get around regex issues
    private static final Pattern GRAPH_PATTERN = Pattern.compile("\\{graph (\\{[\\w\\s]*\\}) (.*)\\}");
    private static final Pattern DEMO_PATTERN = Pattern.compile("\\{do (\\w+) on (.*) from (\\w+) to (\\w+)\\}");

    public static Demo parse(String concrete) {
        try {
            return parseDemo(concrete);
        } catch (Exception e) {
            throw new ParsingException("Unable to parse " + concrete);
        }
    }

    static Node parseNode(String concrete) {
        Matcher matcher = NODE_PATTERN.matcher(concrete);
        if (matcher.matches() && !RESERVED.contains(matcher.group(1))) {
            return Node.of(matcher.group(1));
        }
        throw new ParsingException(concrete + " did not match the expected pattern for a node");
    }

    private static <T> List<T> parseList(String concrete, Function<String, T> function) {
        Matcher matcher = LIST_PATTERN.matcher(concrete);
        if (!matcher.matches()) {
            throw new ParsingException(concrete + " did not match the expected pattern for a list");
        }
        List<T> lst = new ArrayList<>();
        String[] concreteElements = matcher.group(1).split(" ");
        for (int i = 0; i < concreteElements.length; i++) {
            String concreteElement = concreteElements[i];
            // This is hacky as fuck but works for our DSL :shrug:
            if (concreteElement.contains("{")) {
                do {
                    concreteElement = concreteElement + " " + concreteElements[++i];
                } while (!concreteElements[i].contains("}"));
            }
            if (concreteElement.length() != 0) {
                lst.add(function.apply(concreteElement));
            }
        }
        return lst;
    }

    static List<Node> parseNodes(String concrete) {
        return parseList(concrete, Parser::parseNode);
    }

    static Edge parseEdge(String concrete) {
        Matcher matcher = EDGE_PATTERN.matcher(concrete);
        if (!matcher.matches()) {
            throw new ParsingException(concrete + " did not match the expected pattern for an edge");
        }
        Node start = parseNode(matcher.group(1));
        Node end = parseNode(matcher.group(2));
        return Edge.of(start, end);
    }

    static List<Edge> parseEdges(String concrete) {
        return parseList(concrete, Parser::parseEdge);
    }

    static Graph parseGraph(String concrete) {
        Matcher matcher = GRAPH_PATTERN.matcher(concrete);
        if (!matcher.matches()) {
            throw new ParsingException(concrete + " did not match the expected pattern for a graph");
        }
        List<Node> nodes = parseNodes(matcher.group(1));
        List<Edge> edges = parseEdges(matcher.group(2));
        return Graph.of(nodes, edges);
    }

    private static Demo parseDemo(String concrete) {
        Matcher matcher = DEMO_PATTERN.matcher(concrete);
        if (!matcher.matches()) {
            throw new ParsingException(concrete + " did not match the expected pattern for a demo");
        }
        Algorithm algorithm = parseAlgorithm(matcher.group(1));
        Graph graph = parseGraph(matcher.group(2));
        Node start = parseNode(matcher.group(3));
        Node end = parseNode(matcher.group(4));

        return Demo.of(algorithm, graph, start, end);
    }

    static Algorithm parseAlgorithm(String concrete) {
        try {
            return Algorithm.valueOf(concrete);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ParsingException("Unrecognized algorithm " + concrete);
        }
    }

}