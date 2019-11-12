# graph-dsl
<!-- At first, we decided which algorith is used on the search. After that, we use parser to parse a list of graph indicating all the nodes of in the graph and edges between each of the nodes that  are connected. Lastly , from demo class we call on DFS or BFS search the graph that was just created and also indicate the start node and then end node. --!>

<graphDSL name> is a DSL for visualizing graphs, traversals, and basic algorithms, designed to help CS students learn the foundations of graph theory.
  
The below EBNF represents all legal programs in this language.

```scheme
EBNF:
<Demo>        ::= {do <Algorithm> on <Graph> from <Node> to <Node>}     <- currently the only top-level action allowed.
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
```

To display the animation, wrap your program in the following code and run it:
```java
Interpreter.interpret(Parser.parse(<your program>));
```

Example:
```java
Interpreter.interpret(Parser.parse("{do BFS on {graph {A B C} {{A to B} {B to C} {C to A}}} from A to C}"));
```
will perform a BFS traversal starting at A and ending at C.

In the animation, the traversal colors the current visited node red, and once it reaches the end, it colors the end node yellow.
