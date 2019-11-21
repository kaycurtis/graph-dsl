package interpreter;

import model.Node;

import java.util.LinkedList;
import java.util.Queue;

public class BfsSnapshot extends SearchSnapshot<Queue<Node>> {
    public BfsSnapshot() {
        toTraverse = new LinkedList<>();
        nextNodeProvider = toTraverse::remove;
        nodeAdder = toTraverse::add;
    }
}
