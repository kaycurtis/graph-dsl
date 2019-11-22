package interpreter;

import lombok.Data;
import model.Demo;
import model.Edge;
import model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DijkstraSnapshot implements Snapshot {
    Map<Node, Double> dist = new HashMap<>();
    List<Edge> path = new ArrayList<>();
    List<Node> toTraverse = new ArrayList<>();
    Node current;

    public DijkstraSnapshot(Demo demo) {
        demo.getGraph().getNodes().forEach(node -> {
            dist.put(node, Double.MAX_VALUE);
            toTraverse.add(node);
        });
        dist.put(demo.getStart(), 0.0);
    }

    public Node getNext() {
        Node min = toTraverse.stream()
                .min((node1, node2) -> {
                    if (dist.get(node1) == dist.get(node2)) {
                        return 0;
                    }
                    return dist.get(node1) < dist.get(node2) ? -1 : 1;
                }).orElseThrow(() -> new InterpreterException("No next node available"));
        toTraverse.remove(min);
        return min;
    }

    public void insertEdge(Edge edge) {
        path.stream()
                .filter(existingEdge -> existingEdge.getEnd().equals(edge.getEnd()))
                .findFirst()
                .ifPresent(existingEdge -> path.remove(existingEdge));
        path.add(edge);
    }
}
