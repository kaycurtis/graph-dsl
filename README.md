# graph-dsl

This DSL is consisted of three main parts, a parser, an interpreter, and some intermediate object structure that represent either a graph component, an algorithm, or an algorithm application.

Parser: 
- used simple regex matching to parse a string using the below EBNF rules.
- our DSL is not using tools like JavaCC and Antler since we find that simple regex is sufficient for our DSL as it doesn’t need to deal with issues like recursive calls.
- output an object of a structure that represents node, edge, graph, or a function call.


Interpreter:
- first convert our intermediate objects into a valid object input for our animation-rendering tools.
- then utilize the GraphStream library to render an animation illustrating the whole process of graph traversal or other graph function calls.


Intermediate object structure:
- Node:  represented by a string as the name of the Node.
- Edge:  represented by two Nodes and a weight.
- Graph: consists of a list of Nodes and a list of Edges.
- Algorithm: DFS, BFS, or Dijkstra
- Demo: consists of two Nodes to indicate start and end, a Graph, and an Algorithm. Upon being interpreted, Demo provides essential input data for GraphStream tools to render a demo animation.


EBNF:

     <Demo>        ::= {do <Algorithm> on <Graph> from <Node> to <Node>}
     <Graph>       ::= {graph {<Nodes>} {<Edges>}}
     <Node>        ::= <string>
     <Edge>        ::= {<Node> to <Node>}
                     | {<Node> to <Node> <number>}
     <Nodes>       ::=
                     | <Node> <Nodes>
     <Edges>       ::=
                     | <Edge> <Edges>
     <Algorithm>   ::= DFS
                     | BFS
                     | Dijkstra's


The following is an example of creating a simple graph with 6 nodes and 4 edges:
```
Graph GRAPH = Parser.parseGraph("{graph {A B C D E F} {{A to B} {A to D} {A to C} {C to E} {D to F}}}");

```
The following is an example of performing BFS from node A to node C on a simple graph with 6 nodes 4 edges:
```
Demo demo1 = "{do BFS on {graph {A B C D E F} {{A to B} {A to D} {A to C} {C to E} {D to F}}} from A to C}";
Interpreter.interpret(demo1);
```


