import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.javatuples.Triplet;

import java.util.ArrayList;

public class GraphExpression {
    //TODO populate this 

    private Graph graph;

    public GraphExpression() {
        graph = new MultiGraph("myGraph");
    }

    //    // test push
//    public static void main(String[] args) {
//        System.out.println("Hello World!");
//
//
//        // from graphstream tutorial page
//        Graph graph = new SingleGraph("Tutorial 1");
//
//        graph.addNode("A");
//        graph.addNode("B");
//        graph.addNode("C");
//        graph.addEdge("AB", "A", "B");
//        graph.addEdge("BC", "B", "C");
//        graph.addEdge("CA", "C", "A");
//    }

    public void makeNode(String nodeName) {
        graph.addNode(nodeName);
    }

    // edges are pairs of nodes (start node, end node)
    // or triples of node pairs and weights (start node, end node, weight)
    // if triplet's last field is null, no weight
    public void graph(ArrayList<String> nodes, ArrayList<Triplet<String, String, Double>> edges) {
        for (String n : nodes) {
            graph.addNode(n);
        }

        for (Triplet e : edges) {
            graph.addEdge((String) e.getValue0() + e.getValue1(), (String) e.getValue0(), (String) e.getValue1());
            if (e.getValue2() != null) {
                //set edge's weight
            }
        }
    }

    public void doAlgorithm(String algorithmName) {
        switch (algorithmName) {
            case "DFS":
                doDFS();
                break;
            case "BFS":
                doBFS();
                break;
            case "Dijkstra's":
                doDijkstras();
                break;
        }
    }

    public void doDFS() {

    }

    public void doBFS() {

    }

    public void doDijkstras() {

    }
}
