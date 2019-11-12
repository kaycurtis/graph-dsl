import lombok.Value;

import java.util.List;

public interface GraphExpression {
    @Value(staticConstructor = "of")
    public class Node implements GraphExpression {
        String name;
    }
    @Value(staticConstructor = "of")
    public class Nodes implements GraphExpression {
        List<Node> nodes;
    }
    @Value(staticConstructor = "of")
    public class Edge implements GraphExpression {
        Node start;
        Node end;
       // add cost after POC
    }
    @Value(staticConstructor = "of")
    public class Edges implements GraphExpression {
        List<Edge> edges;
    }
    @Value(staticConstructor = "of")
    public class Graph implements GraphExpression {
        Nodes nodes;
        Edges edges;
    }
    @Value(staticConstructor = "of")
    public class Demo implements GraphExpression {
        Algorithm algorithm;
        Graph graph;
        Node start;
        Node end;
    }
    enum Algorithm implements GraphExpression {
        BFS, DFS, DIJKSTRAS
    }
}
