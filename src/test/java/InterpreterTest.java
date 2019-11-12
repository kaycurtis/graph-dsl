import org.junit.Test;

public class InterpreterTest {
    private static final Demo DEMO = Parser.parse("{do BFS on {graph {A B C} {{A to B} {B to C} {C to A}}} from A to C}");

    @Test
    public void test() {
        Interpreter.interpret(DEMO);
    }
}
