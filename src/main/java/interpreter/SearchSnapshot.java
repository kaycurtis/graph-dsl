package interpreter;

import lombok.Data;
import model.Node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Data
public abstract class SearchSnapshot<T extends Collection<Node>> implements Snapshot {
    Set<Node> visited = new HashSet<>();
    T toTraverse;
    Supplier<Node> nextNodeProvider;
    Consumer<Node> nodeAdder;
    Node current;
}
