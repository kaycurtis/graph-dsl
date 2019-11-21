package interpreter;

import lombok.Value;
import model.Edge;
import model.Node;

import java.util.Map;

@Value(staticConstructor = "of")
public class GraphStreamGraph {
    Map<Node, org.graphstream.graph.Node> nodes;
    Map<Edge, org.graphstream.graph.Edge> edges;

    public org.graphstream.graph.Node getNode(Node node) {
        return nodes.get(node);
    }

    public org.graphstream.graph.Edge getEdge(Edge edge) {
        return edges.get(edge);
    }
}
