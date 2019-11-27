package interpreter;

import lombok.Data;
import model.Demo;
import model.Edge;
import model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Undirected graph algorithm, so edges need matching reverse edge
// Ignores demo.end() since this algorithm generates a minimum spanning tree

@Data
public class PrimSnapshot implements Snapshot {
    /*
        Prim's algorithm:
            tl;dr Like Dijkstra's but priority queue is not path cost but edge cost
            i.e. keep growing the tree by adding the closest neighbor that's not in the tree yet
    */
    List<Edge> tree = new ArrayList<>();
    List<Node> remainingNodes = new ArrayList<>();
    List<Edge> remainingEdges = new ArrayList<>();
    Map<Node, Boolean> isInTree = new HashMap<>();
    Node current; // guaranteed to be in tree
    Boolean canContinue = true;

    public PrimSnapshot(Demo demo) {
        // System.out.println("demo.getStart(): " + demo.getStart().getName());
        demo.getGraph().getNodes().forEach(node -> {
            // Put the start node into the tree, set current to it;
            // and everything else goes into remainingNodes
            if (node.equals(demo.getStart())) {
                this.current = node;
                // System.out.println("Putting start node into tree");
                this.isInTree.put(node, true);
            } else {
                this.remainingNodes.add(node);
                this.isInTree.put(node, false);
            }
        });
        demo.getGraph().getEdges().forEach(edge -> {
            this.remainingEdges.add(edge);
        });
    }

    public boolean isOver() {
        return this.remainingNodes.size() == 0 || this.remainingEdges.size() == 0 || !this.canContinue;
    }

    /**
     * Returns the minimum edge from the tree to the node
     * returns null if none exists
     */
    private Edge minEdgeFromTree(Node node) {
        // System.out.println("Min edge from tree: node: " + node.getName());
        // Search in the remainingEdges that point to n.
        Edge edge = null;
        double minEdgeLength = Double.POSITIVE_INFINITY;
        // Check all remaining edges. If it comes from the tree to the destination node n,
        // take the minimum such edge and return it.
        // System.out.println("Checking remaining edges");
        // System.out.println("There are " + this.remainingEdges.size() + " remaining edges");
        for (Edge e : this.remainingEdges) {
            // System.out.println("Checking edge: " + e.getStart() + " " + e.getEnd());
            if (this.isInTree.get(e.getStart()) && e.getEnd().equals(node)) {
                // System.out.println("edge goes tree to node");
                if (e.getWeight() < minEdgeLength) {
                    minEdgeLength = e.getWeight();
                    edge = e;
                }
            }
        }
        return edge;
    }


    /**
     * Take a step in Prim's algorithm, greedily adding the next edge and node to the MST
     */
    public void step() {
        // Find the next edge
        Edge nextEdge = getNextEdge();
        if (nextEdge == null) {
            this.canContinue = false;
        } else {
            this.tree.add(nextEdge);
            this.remainingEdges.remove(nextEdge);
            Node nextNode = nextEdge.getEnd();
            this.current = nextNode;
            this.remainingNodes.remove(nextNode);
            this.isInTree.put(nextNode, true);
        }
    }

    /**
     * Returns the next edge in the growing MST (mininum spanning tree)
     */
    private Edge getNextEdge() {
        Edge minEdge = null;
        double minLength = Double.POSITIVE_INFINITY;
        // Search in all remaining nodes.
        for (Node node : this.remainingNodes) {
            Edge minEdgeFromTreeToNode = minEdgeFromTree(node);
            if (minEdgeFromTreeToNode == null) {
                continue;
            } else {
                double distFromTree = minEdgeFromTreeToNode.getWeight();
                if (distFromTree < minLength) {
                    minLength = distFromTree;
                    minEdge = minEdgeFromTreeToNode;
                }
            }
        }
        return minEdge;
    }
}