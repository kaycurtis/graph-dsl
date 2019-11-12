# graph-dsl

This DSL is consisted of three main parts, a parser, an interpreter, and some intermediate class structure that represent either a graph component or a function call.

Parser: 
- used simple regex matching to parse a string using the below EBNF rules.
- our DSL is not using tools like JavaCC and Antler since we find that simple regex is sufficient for our DSL as it doesn’t need to deal with issues like recursive calls.
- output an object of structure that represents node, edge, graph, or a function call.


Interpreter:
- mainly utilize the GraphStream library to render an animation illustrating the whole process of graph traversal or other graph function calls.


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

Graph GRAPH = Parser.parseGraph("{graph {A B C D E F} {{A to B} {A to D} {A to C} {C to E} {D to F}}}");


The following is an example of performing BFS on a graph with 4 edges:

String demo1 = "{do BFS on {graph {A B C D E F} {{A to B} {A to D} {A to C} {C to E} {D to F}}} from A to C}";
Interpreter.interpret(demo1);



