import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final List<String> RESERVED = ImmutableList.of("to", "do", "on", "from", "graph", "DFS", "BFS", "DIJKSTRAS");

    private static final Pattern LIST_PATTERN = Pattern.compile("\\{(.*)\\}");
    private static final Pattern NODE_PATTERN = Pattern.compile("([A-Za-z0-9]+)");
    private static final Pattern EDGE_PATTERN = Pattern.compile("\\{(.*) to (.*)\\}");
    // [\w\s]* is to match node list, hacky way to get around regex issues
    private static final Pattern GRAPH_PATTERN = Pattern.compile("\\{graph (\\{[\\w\\s]*\\}) (.*)\\}");
    private static final Pattern DEMO_PATTERN = Pattern.compile("\\{do (\\w+) on (.*) from (\\w+) to (\\w+)\\}");

    private static final List<Function<String, GraphExpression>> parsingFunctions = ImmutableList.of(
            Parser::parseNode,
            Parser::parseNodes,
            Parser::parseEdge,
            Parser::parseEdges,
            Parser::parseAlgorithm,
            Parser::parseGraph,
            Parser::parseDemo
    );

    public static GraphExpression parse(String concrete) {
        return parsingFunctions.stream()
                .map(func -> func.apply(concrete))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ParsingException("Could not parse " + concrete));
    }

    private static GraphExpression.Node parseNode(String concrete) {
        Matcher matcher = NODE_PATTERN.matcher(concrete);
        if (matcher.matches() && !RESERVED.contains(matcher.group(1))) {
            return GraphExpression.Node.of(matcher.group(1));
        }
        return null;
    }

    private static <T> List<T> parseList(String concrete, Function<String, T> function) {
        Matcher matcher = LIST_PATTERN.matcher(concrete);
        if (!matcher.matches()) {
            return null;
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
                T elem = function.apply(concreteElement);
                if (elem == null) {
                    return null;
                }
                lst.add(elem);
            }
        }
        return lst;
    }

    private static GraphExpression.Nodes parseNodes(String concrete) {
        List<GraphExpression.Node> nodes = parseList(concrete, Parser::parseNode);
        return nodes == null ? null : GraphExpression.Nodes.of(nodes);
    }

    private static GraphExpression.Edge parseEdge(String concrete) {
        Matcher matcher = EDGE_PATTERN.matcher(concrete);
        if (!matcher.matches()) {
            return null;
        }
        GraphExpression.Node start = parseNode(matcher.group(1));
        GraphExpression.Node end = parseNode(matcher.group(2));
        if (start == null || end == null) {
            return null;
        }
        return GraphExpression.Edge.of(start, end);
    }

    private static GraphExpression.Edges parseEdges(String concrete) {
        List<GraphExpression.Edge> edges = parseList(concrete, Parser::parseEdge);
        return edges == null ? null : GraphExpression.Edges.of(edges);
    }

    private static GraphExpression.Graph parseGraph(String concrete) {
        Matcher matcher = GRAPH_PATTERN.matcher(concrete);
        if (!matcher.matches()) {
            return null;
        }
        GraphExpression.Nodes nodes = parseNodes(matcher.group(1));
        GraphExpression.Edges edges = parseEdges(matcher.group(2));
        if (nodes == null || edges == null) {
            return null;
        }
        return GraphExpression.Graph.of(nodes, edges);
    }

    private static GraphExpression.Demo parseDemo(String concrete) {
        Matcher matcher = DEMO_PATTERN.matcher(concrete);
        if (!matcher.matches()) {
            return null;
        }
        GraphExpression.Algorithm algorithm = parseAlgorithm(matcher.group(1));
        GraphExpression.Graph graph = parseGraph(matcher.group(2));
        GraphExpression.Node start = parseNode(matcher.group(3));
        GraphExpression.Node end = parseNode(matcher.group(4));
        if (algorithm == null || graph == null || start == null || end == null) {
            return null;
        }
        return GraphExpression.Demo.of(algorithm, graph, start, end);
    }

    private static GraphExpression.Algorithm parseAlgorithm(String concrete) {
        try {
            return GraphExpression.Algorithm.valueOf(concrete);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public static class ParsingException extends RuntimeException {
        public ParsingException(String message) {
            super(message);
        }
    }
}