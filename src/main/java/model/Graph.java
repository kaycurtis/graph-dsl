package model;

import lombok.Value;
import model.Edge;
import model.Node;

import java.util.List;

@Value(staticConstructor = "of")
public class Graph {
    List<Node> nodes;
    List<Edge> edges;
}
