package interpreter;

import model.Node;

import java.util.Stack;

public class DfsSnapshot extends SearchSnapshot<Stack<Node>> {
    public DfsSnapshot() {
        toTraverse = new Stack<>();
        nextNodeProvider = toTraverse::pop;
        nodeAdder = toTraverse::push;
    }
}
