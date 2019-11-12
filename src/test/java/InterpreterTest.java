import org.junit.Test;

public class InterpreterTest {

    @Test
    public void testBfs() {
        Demo bfs =  Parser.parse("{do BFS on {graph {A B C} {{A to B} {B to C} {C to A}}} from A to C}");
        Interpreter.interpret(bfs);
    }

    @Test
    public void testDfs() {
        Demo dfs =  Parser.parse("{do DFS on {graph {A B C} {{A to B} {B to C} {C to A}}} from A to C}");
        Interpreter.interpret(dfs);
    }
}
