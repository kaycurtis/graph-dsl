package parser;

import com.google.common.collect.ImmutableList;
import model.*;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

    private static final Node A = Node.of("A");
    private static final Node B = Node.of("B");
    private static final Edge A_TO_B = Edge.of(A, B, null);
    private static final Node C = Node.of("C");
    private static final Edge C_TO_A = Edge.of(C, A, null);
    private static final Edge B_TO_C = Edge.of(B, C, null);
    private static final List<Node> NODES = ImmutableList.of(A, B, C);
    private static final List<Edge> EDGES = ImmutableList.of(A_TO_B, B_TO_C, C_TO_A);
    private static final Graph GRAPH = Graph.of(NODES, EDGES);
    private static final Graph WEIGHTED_GRAPH = Graph.of(
            NODES,
            ImmutableList.of(Edge.of(A, B, 1.0),
                    Edge.of(B, C, 2.0),
                    Edge.of(C, A, 1.5)
            ));

    @Test
    public void testNodeParse() {
        assertThat(Parser.parseNode("A").get(0)).isEqualTo(A);
        assertThat(Parser.parseNodes("{A B C}")).isEqualTo(NODES);
        assertThat(Parser.parseNodes("{}")).isEmpty();
    }

    @Test
    public void testEdgeParse() {
        // added: this test now fails unless we cast it to Edge. see testEdgeParseBidirectional.
        // assertThat(Parser.parseEdge("{A to B}")).isEqualTo(A_TO_B);
        assertThat(Parser.parseEdges("{{A to B} {B to C} {C to A}}")).isEqualTo(EDGES);
    }

    @Test
    public void testAlgorithm() {
        assertThat(Parser.parseAlgorithm("BFS")).isEqualTo(Algorithm.BFS);
    }

    @Test
    public void testGraph() {
        assertThat(Parser.parseGraph("{graph {A B C} {{A to B} {B to C} {C to A}}}")).isEqualTo(GRAPH);
        Parser.parseGraph("{graph {} {}}"); // no exception
    }

    @Test
    public void testGraphWeighted() {
        assertThat(Parser.parseGraph("{graph {A B C} {{A to B 1} {B to C 2.0} {C to A 1.5}}}")).isEqualTo(WEIGHTED_GRAPH);
    }

    @Test
    public void testDemo() {
        String demo = "{do BFS on {graph {A B C} {{A to B} {B to C} {C to A}}} from A to C}";
        Demo expected = Demo.of(Algorithm.BFS, GRAPH, A, C);
        assertThat(Parser.parse(demo)).isEqualTo(expected);
    }

    @Test(expected = ParsingException.class)
    public void testBadParseGraphBadFormat() {
        String demo = "{graph {A B C} {A to B} {B to C}}";
        Parser.parseGraph(demo);
    }

    @Test
    public void testBadParseGraphNoNodes() {
        String demo = "{graph {} {{A to B} {B to C}}}";
        Graph g = Parser.parseGraph(demo);
        assertThat(g.getNodes()).isEmpty();
    }

    @Test
    public void testParseGraphNoEdges() {
        String demo = "{graph {A B C} {}}";
        Graph g = Parser.parseGraph(demo);
        assertThat(g.getEdges()).isEmpty();
    }

    @Test(expected = ParsingException.class)
    public void testParseGraphMissingKeywords() {
        String demo = "{graph {A B C} {{A bar B} {B foo C}}}";
        Parser.parseGraph(demo);
    }

    @Test
    public void testEdgeParseBidirectional() {
        assertThat(Parser.parseEdge("{A to B}").get(0)).isEqualTo(A_TO_B);

        assertThat(Parser.parseEdges("{{A to B} {B to C} {C to A}}")).isEqualTo(EDGES);

        // new bi-directional test.
        List<Edge> e1 = ImmutableList.of(Edge.of(A, B, null), Edge.of(B, A, null), Edge.of(C, A, null));
        assertThat(Parser.parseEdges("{{A <-> B} {C to A}}")).isEqualTo(e1);
    }
}
