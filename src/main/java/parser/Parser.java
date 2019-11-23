package parser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import model.*;

import java.util.ArrayList;
import java.util.Collections;
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

    private static final List<String> RESERVED = ImmutableList.of("to", "do", "on", "from", "graph", "DFS", "BFS", "DIJKSTRAS","NOTHING");

    private static final Pattern LIST_PATTERN = Pattern.compile("\\{(.*)\\}");
    private static final Pattern NODE_PATTERN = Pattern.compile("([A-Za-z0-9]+)");
    private static final Pattern EDGE_PATTERN = Pattern.compile("\\{(.*) to (\\w+)( [\\d\\.]+)?\\}");
    private static final Pattern BIDIRECTIONAL_EDGE_PATTERN = Pattern.compile("\\{(.*) <-> (\\w+)( [\\d\\.]+)?\\}");

    // [\w\s]* is to match node list, hacky way to get around regex issues
    private static final Pattern GRAPH_PATTERN = Pattern.compile("\\{graph (\\{[\\w\\s]*\\}) (.*)\\}");
    private static final Pattern DEMO_PATTERN = Pattern.compile("\\{do (\\w+) on (.*) from (\\w+) to (\\w+)\\}");
    // added: pattern for NOTHING example: {do NOTHING on {graph {A B C} {{A to B} {B to C} {C to A}}}}
    private static final Pattern DEMO_PATTERN_NOTHING = Pattern.compile("\\{do NOTHING on (.*)\\}");

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
                // added : if returnValue of function is array, do addAll(), else do add()
                T thing = function.apply(concreteElement);
                if (thing instanceof List) {
                    for (Object each : (List)thing){
                        if (!lst.contains(each)){
                            lst.add((T) each);
                        }
                    }
                } else {
                    if (!lst.contains(thing)){
                        lst.add(thing);
                    }

                }

            }
        }
        return lst;
    }

    static List<Node> parseNodes(String concrete) {
        return parseList(concrete, Parser::parseNode);
    }

    static <T> T parseEdge(String concrete) {
        Matcher matcher = EDGE_PATTERN.matcher(concrete);
        Matcher matcherAnother = BIDIRECTIONAL_EDGE_PATTERN.matcher(concrete);
        List<Edge> returnArray = new ArrayList<>();
        if (matcher.matches()) {
            Node start = parseNode(matcher.group(1));
            Node end = parseNode(matcher.group(2));
            Double cost = getCost(matcher.group(3));
            return (T) Collections.singletonList(Edge.of(start,end,cost));
        } else if (matcherAnother.matches()){
            Node start = parseNode(matcherAnother.group(1));
            Node end = parseNode(matcherAnother.group(2));
            Double cost = getCost(matcherAnother.group(3));

            returnArray.add(Edge.of(start, end, cost));
            returnArray.add(Edge.of(end,start, cost));
            return (T) returnArray; //TODO

        } else {
            throw new ParsingException(concrete + " did not match the expected pattern for an edge");
        }

    }

    private static Double getCost(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            throw new ParsingException("Malformed edge cost!");
        }
    }

    static List<Edge> parseEdges(String concrete) {
        return parseList(concrete, Parser::parseEdge);
    }

    @VisibleForTesting
    public static Graph parseGraph(String concrete) {
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
        Matcher matcherNothing = DEMO_PATTERN_NOTHING.matcher(concrete);
        if (matcherNothing.matches()) {
            Graph graph = parseGraph(matcherNothing.group(1));
            return Demo.of(Algorithm.NOTHING, graph, null, null);
        } else if (matcher.matches()) {
            Algorithm algorithm = parseAlgorithm(matcher.group(1));
            Graph graph = parseGraph(matcher.group(2));
            Node start = parseNode(matcher.group(3));
            Node end = parseNode(matcher.group(4));
            return Demo.of(algorithm, graph, start, end);
        } else {
            throw new ParsingException(concrete + " did not match the expected pattern for a demo");
        }
    }

    static Algorithm parseAlgorithm(String concrete) {
        try {
            return Algorithm.valueOf(concrete);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ParsingException("Unrecognized algorithm " + concrete);
        }
    }

}