package model;

import lombok.Value;

@Value(staticConstructor = "of")
public class Demo {
        Algorithm algorithm;
        Graph graph;
        Node start;
        Node end;
        Boolean showDS;
}
