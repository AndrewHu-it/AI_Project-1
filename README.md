### This project solves the sliding tile puzzle, with Bredth First Search, Depth First Search, Iterative Deepening, Depth Limited Search, Uniform Cost Search, Greedy Best First Search, or A*. The paramters are below.

**Defualt Paramters:** <br>
1. The bottom right square is the winning position.
2. The size of the board is 4x4
3. Will **NOT** print out the statistics.
4. Search Strategy is A*
5. DLS limit is 25


**Board:** <br>
-rows <#> <br>
-columns <#> <br>
.         _represents the empty square <br>_
n       _n is an integer between 1 and board size -1. <br>_

>the order in which the tile numbers appear in the paramters is the order in which they will be entered into the board <br>
>Starting in the upper left corner, and working down to the bottom left corne (row,col) --> (0,0) (0,1) (0,2)...(0,i) (1,0) (1,1)...(1,i)... (i,j)



**Solving Strategy:** <br>
-top     _the top left square for winning state <br>_
-bottom     _the bottom right square for winning state<br>_

-bfs (Breadth First Search)<br>
-dfs (Depth First Search) <br>
-dls (Depth Limited Search) <br>
-id (Iterative Deepening) <br>
-ucs (Uniform Cost Search, Dijkstra's, g(x)) <br>
-gbfs (Greedy Best First Search, h(x)) <br>
-astar (A* f(x) = g(x) + h(x)) <br>


**Printing Options:** <br>
-verbose     _Prints out the board after each move <br>_
-stats <br>


**Commands to run preset sizes:** <br> 
time java Puzzle `cat 4x4Puzzles/4x4.<depth #>.txt` -astar -stats <br>
EX: time java Puzzle `cat 4x4Puzzles/4x4.48.txt` -astar -stats <br>





