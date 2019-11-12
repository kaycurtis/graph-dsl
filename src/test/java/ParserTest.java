import com.google.common.collect.ImmutableList;
import model.Algorithm;
import model.Demo;
import model.Edge;
import model.Graph;
import model.Node;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

    private static final Node A = Node.of("A");
    private static final Node B = Node.of("B");
    private static final Edge A_TO_B = Edge.of(A, B);
    private static final Node C = Node.of("C");
    private static final Edge C_TO_A = Edge.of(C, A);
    private static final Edge B_TO_C = Edge.of(B, C);
    private static final List<Node> NODES = ImmutableList.of(A, B, C);
    private static final List<Edge> EDGES = ImmutableList.of(A_TO_B, B_TO_C, C_TO_A);
    private static final Graph GRAPH = Graph.of(NODES, EDGES);

    @Test
    public void testNodeParse() {
        assertThat(Parser.parseNode("A")).isEqualTo(A);
        assertThat(Parser.parseNodes("{A B C}")).isEqualTo(NODES);
        assertThat(Parser.parseNodes("{}")).isEmpty();
    }

    @Test
    public void testEdgeParse() {
        assertThat(Parser.parseEdge("{A to B}")).isEqualTo(A_TO_B);
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
    public void testDemo() {
        String demo = "{do BFS on {graph {A B C} {{A to B} {B to C} {C to A}}} from A to C}";
        Demo expected = Demo.of(Algorithm.BFS, GRAPH, A, C);
        assertThat(Parser.parse(demo)).isEqualTo(expected);
    }
    
    @Test
    public void testBadParseGraphBadFormat() {
        String demo = "{graph {A B C} {A to B} {B to C}}";
        try {
            Graph g = Parser.parseGraph(demo);
            fail("supposed to throw parsing exception");
        } catch (Parser.ParsingException p) {
        }
    }

    @Test
    public void testBadParseGraphNoNodes() {
        // ?????????? do the nodes used in the edges list have to exist in the nodes list?

        String demo = "{graph {} {{A to B} {B to C}}}";
        try {
            Graph g = Parser.parseGraph(demo);
            fail("supposed to throw parsing exception");
        } catch (Parser.ParsingException p) {
        }
    }

    @Test
    public void testParseGraphNoEdges() {
        String demo = "{graph {A B C} {}}";
        try {
            Graph g = Parser.parseGraph(demo);
        } catch (Parser.ParsingException p) {
            fail("ok to have no edges");
        }
    }
}
