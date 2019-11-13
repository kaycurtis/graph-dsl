import model.Algorithm;
import model.Demo;
import model.Graph;
import model.Node;
import org.junit.Test;

public class InterpreterTest {

    private static final Graph GRAPH = Parser.parseGraph("{graph {A B C D E F} {{A to B} {A to D} {A to C} {C to E} {D to F}}}");
    private static final Graph GRAPH2 = Parser.parseGraph("{graph {A B C D E F G X} {{A to B} {A to C} {B to C} {B to F} {C to D}" +
            " {F to D} {F to A} {D to E} {E to G} {G to A} {A to X} {X to G} {X to D} {E to C} {X to F} {B to G}}}");
    private static final Graph SIMPLE_GRAPH = Parser.parseGraph("{graph {A B C} {{A to B} {B to C} {C to A}}}");
    private static final Graph GRAPH3 = Parser.parseGraph("{graph {A B} {{A to B} {A to A} {B to A}}}");
    private static final Graph GRAPH4 = Parser.parseGraph("{graph {A B C D E F} {{A to B} {A to C} {D to E} {D to F}}}");

    @Test
    public void testBfs() {
        Interpreter.interpret(Demo.of(Algorithm.BFS, GRAPH, Node.of("A"), Node.of("F")));
    }

    @Test
    public void testDfs() {
        Interpreter.interpret(Demo.of(Algorithm.DFS, GRAPH, Node.of("A"), Node.of("F")));
    }
    
    @Test
    public void testBfsBig() {
        Interpreter.interpret(Demo.of(Algorithm.BFS, GRAPH2, Node.of("A"), Node.of("E")));
    }

    @Test
    public void testDfsBig() {
        Interpreter.interpret(Demo.of(Algorithm.DFS, GRAPH2, Node.of("A"), Node.of("E")));
    }

    @Test
    public void testBfsCycle() {
        Interpreter.interpret(Demo.of(Algorithm.BFS, GRAPH3, Node.of("A"), Node.of("B")));
    }

    @Test
    public void testDfsCycle() {
        Interpreter.interpret(Demo.of(Algorithm.DFS, GRAPH3, Node.of("A"), Node.of("B")));
    }

    @Test
    public void testBfsNonexistentEnd() {
        Interpreter.interpret(Demo.of(Algorithm.BFS, SIMPLE_GRAPH, Node.of("A"), Node.of("E")));
    }

    @Test
    public void testDfsNonexistentEnd() {
        Interpreter.interpret(Demo.of(Algorithm.DFS, SIMPLE_GRAPH, Node.of("A"), Node.of("E")));
    }
    
    @Test
    public void testBfsUnreachableEnd() {
        Interpreter.interpret(Demo.of(Algorithm.BFS, GRAPH4, Node.of("A"), Node.of("E")));
    }

    @Test
    public void testDfsUnreachableEnd() {
        Interpreter.interpret(Demo.of(Algorithm.DFS, GRAPH4, Node.of("A"), Node.of("E")));
    }

    @Test(expected = InterpreterException.class)
    public void testDfsGraphDuplicateEdge() {
        Graph BAD_GRAPH = Parser.parseGraph("{graph {A B C} {{A to B} {B to C} {B to C}}}");
        Interpreter.interpret(Demo.of(Algorithm.DFS, BAD_GRAPH, Node.of("A"), Node.of("E")));
    }

    @Test(expected = InterpreterException.class)
    public void edgeNodesNotInGraph() {
        Graph BAD_GRAPH = Parser.parseGraph("{graph {A B C} {{A to E}}}");
        Interpreter.interpret(Demo.of(Algorithm.DFS, BAD_GRAPH, Node.of("A"), Node.of("E")));
    }

    @Test(expected = InterpreterException.class)
    public void duplicateNodes() {
        Graph BAD_GRAPH = Parser.parseGraph("{graph {A B B} {{A to B} {B to A}}}");
        Interpreter.interpret(Demo.of(Algorithm.DFS, BAD_GRAPH, Node.of("A"), Node.of("E")));
    }

    @Test(expected = InterpreterException.class)
    public void startNodeNodeInGraph() {
        Graph BAD_GRAPH = Parser.parseGraph("{graph {A B C} {{A to B} {B to C} {B to A}}}");
        Interpreter.interpret(Demo.of(Algorithm.DFS, BAD_GRAPH, Node.of("E"), Node.of("A")));
    }
}
