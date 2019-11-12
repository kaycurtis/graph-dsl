package model;

import lombok.Value;

@Value(staticConstructor = "of")
public class Node {
    String name;
}
