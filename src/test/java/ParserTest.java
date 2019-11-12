import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

    private static final GraphExpression.Node A = GraphExpression.Node.of("A");
    private static final GraphExpression.Node B = GraphExpression.Node.of("B");
    private static final GraphExpression.Edge A_TO_B = GraphExpression.Edge.of(A, B);
    private static final GraphExpression.Node C = GraphExpression.Node.of("C");
    private static final GraphExpression.Edge C_TO_A = GraphExpression.Edge.of(C, A);
    private static final GraphExpression.Edge B_TO_C = GraphExpression.Edge.of(B, C);
    private static final GraphExpression.Nodes NODES = GraphExpression.Nodes.of(ImmutableList.of(A, B, C));
    private static final GraphExpression.Edges EDGES = GraphExpression.Edges.of(ImmutableList.of(A_TO_B, B_TO_C, C_TO_A));
    private static final GraphExpression.Graph GRAPH = GraphExpression.Graph.of(NODES, EDGES);

    @Test
    public void testNodeParse() {
        assertThat(Parser.parse("A")).isEqualTo(A);
        GraphExpression listResult = Parser.parse("{A B C}");
        assertThat(listResult).isEqualTo(NODES);
        assertThat(Parser.parse("{}")).isInstanceOf(GraphExpression.Nodes.class); // depends on order of parse functions
    }

    @Test
    public void testEdgeParse() {
        assertThat(Parser.parse("{A to B}")).isEqualTo(A_TO_B);
        GraphExpression listResult = Parser.parse("{{A to B} {B to C} {C to A}}");
        assertThat(listResult).isEqualTo(EDGES);
    }

    @Test
    public void testAlgorithm() {
        assertThat(Parser.parse("BFS")).isEqualTo(GraphExpression.Algorithm.BFS);
    }

    @Test
    public void testGraph() {
        GraphExpression graph = Parser.parse("{graph {A B C} {{A to B} {B to C} {C to A}}}");
        assertThat(graph).isEqualTo(GRAPH);
        assertThat(Parser.parse("{graph {} {}}")).isInstanceOf(GraphExpression.Graph.class);
    }

    @Test
    public void testDemo() {
        String demo = "{do BFS on {graph {A B C} {{A to B} {B to C} {C to A}}} from A to C}";
        GraphExpression.Demo expected = GraphExpression.Demo.of(GraphExpression.Algorithm.BFS, GRAPH, A, C);
        assertThat(Parser.parse(demo)).isEqualTo(expected);
    }
}
