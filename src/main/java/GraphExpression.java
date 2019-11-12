import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Iterator;

public class GraphExpression {
    // TODO -- where to actually call graph.display() ???

    // TODO -- require that node names be unique (graphstream won't allow it otherwise)

    private Graph graph;
    private int vertices; //??????
    private List<Node> dfslist = new ArrayList<Node>();

    public static void main(String[] args) { // TODO - remove this later
        GraphExpression ge = new GraphExpression();
        ArrayList<String> nodes = new ArrayList<>();
        nodes.add("A");
        nodes.add("B");
        nodes.add("C");
        nodes.add("D");

        Triplet<String, String, Double> edge0 = new Triplet<>("A", "B", null);
        Triplet<String, String, Double> edge1 = new Triplet<>("B", "C", null);
        Triplet<String, String, Double> edge2 = new Triplet<>("D", "C", null);
        Triplet<String, String, Double> edge3 = new Triplet<>("C", "A", null);
        ArrayList<Triplet<String, String, Double>> edges = new ArrayList<>();
        edges.add(edge0);
        edges.add(edge1);
        edges.add(edge2);
        edges.add(edge3);

        ge.graph(nodes, edges);

        ge.doAlgorithm("DFS", "A");
    }

    public GraphExpression() {
        graph = new MultiGraph("myGraph");
        vertices = 0;
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
        vertices++;
    }

    // edges are pairs of nodes (start node, end node)
    // or triples of node pairs and weights (start node, end node, weight)
    // if triplet's last field is null, no weight
    public void graph(ArrayList<String> nodes, ArrayList<Triplet<String, String, Double>> edges) {
        for (String n : nodes) {
            graph.addNode(n);
            vertices++;
        }

        for (Triplet e : edges) {
            graph.addEdge((String) e.getValue0() + e.getValue1(), (String) e.getValue0(), (String) e.getValue1());
            if (e.getValue2() != null) {
                //set edge's weight
            }
        }

       //System.out.println("Displaying graph: REMOVE LATER");
        // graph.display();
    }

    public void doAlgorithm(String algorithmName, String start) {
        Node startNode = graph.getNode(start);

        switch (algorithmName) {
            case "DFS":
                dfslist.add(startNode);
                doDFS(startNode);
                break;
            case "BFS":
                doBFS(startNode);
               
                break;
            case "Dijkstra's":
                //doDijkstras(startNode);
                System.out.println("dijkstra not implemented");
                break;
        }
    }

    // TODO: add directed/undirected later

    private void doDFS(Node startNode) {
        Iterator<Node> i = startNode.getDepthFirstIterator();

        while (i.hasNext()) {
            Node next = i.next();
            dfslist.add(next);
            System.out.println("Found node: " + next.getId());
            doDFS(next);
        }
        
        
        

    }
//
    public void doBFS(Node startNode) {
        Iterator<Node> i = startNode.getBreadthFirstIterator();
        while (i.hasNext()){
            Node next = i.next();
            System.out.println("Found node: " + next.getId());
   
        }
        i.pop();
        doBFS(next);
    }
//
//    public void doDijkstras(Node startNode) {
//
//    }
}
