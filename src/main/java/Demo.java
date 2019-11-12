import lombok.Value;
import model.Algorithm;
import model.Graph;
import model.Node;

@Value(staticConstructor = "of")
public class Demo {
        Algorithm algorithm;
        Graph graph;
        Node start;
        Node end;
}
