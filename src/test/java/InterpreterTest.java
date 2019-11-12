import model.Algorithm;
import model.Demo;
import model.Graph;
import model.Node;
import org.junit.Test;

public class InterpreterTest {

    private static final Graph GRAPH = Parser.parseGraph("{graph {A B C D E F} {{A to B} {A to D} {A to C} {C to E} {D to F}}}");

    @Test
    public void testBfs() {
        Interpreter.interpret(Demo.of(Algorithm.BFS, GRAPH, Node.of("A"), Node.of("F")));
    }

    @Test
    public void testDfs() {
        Interpreter.interpret(Demo.of(Algorithm.DFS, GRAPH, Node.of("A"), Node.of("F")));
    }
}
