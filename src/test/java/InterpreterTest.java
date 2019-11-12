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
    
    // not sure if this is doing the right thing ???
    @Test
    public void testBfsBig() {
        Interpreter.interpret(Demo.of(Algorithm.BFS, GRAPH2, Node.of("A"), Node.of("E")));
    }

    // not sure if this is doing the right thing ???
    @Test
    public void testDfsBig() {
        Interpreter.interpret(Demo.of(Algorithm.DFS, GRAPH2, Node.of("A"), Node.of("E")));
    }

    @Test
    public void testBfsCycle() {
        Interpreter.interpret(Demo.of(Algorithm.BFS, GRAPH3, Node.of("A"), Node.of("B")));
    }

    // TODO infinite loop if cycle for DFS (???)
    @Test
    public void testDfsCycle() {
        Interpreter.interpret(Demo.of(Algorithm.DFS, GRAPH3, Node.of("A"), Node.of("B")));
    }

    // TODO: infinite loop if the end node doesn't exist
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

    @Test
    public void testDfsGraphDuplicateEdge() {
        // todo - should duplicate nodes/edges be checked for in parsing or interpreting?
        Graph BAD_GRAPH = Parser.parseGraph("{graph {A B C} {{A to B} {B to C} {B to C}}}");
        try {
            Interpreter.interpret(Demo.of(Algorithm.DFS, BAD_GRAPH, Node.of("A"), Node.of("E")));
            fail("Duplicate edges are not allowed");
        } catch (Exception changeThisLaterTODO) { //right now its catching a graphstream exception
        }
    }

}
