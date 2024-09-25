//Andrew Hurlbut
//AI/ML


import java.util.*;

public class RationalAgent {

    private Board goalState;
    private Board initialState;
    private Puzzle.SearchStrategy strategy;
    private int max_depth;

    HashMap<Board, Node> visited = new HashMap<Board, Node>();


    public RationalAgent(Board initialState, Board goalState, Puzzle.SearchStrategy strategy){
        this.goalState = goalState;
        this.initialState = initialState;
        this.strategy = strategy;
    }

    public RationalAgent(Board initialState, Board goalState, Puzzle.SearchStrategy strategy, int depth){
        this.goalState = goalState;
        this.initialState = initialState;
        this.strategy = strategy;
        this.max_depth = depth;
    }


    public ArrayList<Puzzle.Direction> solve() {
        Node final_node = null;
        switch(this.strategy) {
            case BFS:
                final_node = this.BFS();
                break;
            case DFS:
                final_node = this.DFS();
                break;
            case DLS:
                final_node = this.DLS();
                break;
            case ID:
                final_node = this.ID();
                break;
            case UCS:
            case ASTAR:
            case GBFS:
                final_node = this.search(this.strategy);
                break;
        }

        if (final_node == null) {
            return null;
        }
        return final_node.backtrack();
    }




    //With Dijkstras we will not have the problem of finding a more optimal path to a particular node..... will we get that problem with A* and/or GBFS? Seems likely.
        //If this is the case, we want to keep the hashmap so that we can figure out the cost of the node that we have just run into.
        //I dont think I need the explored Hashset if I am already using a hashmap to figure out what I have seen already
            //Try to code it wihtout after I get it working.

    public Node search(Puzzle.SearchStrategy strategy){

        Comparator<Node> comparator; // I figured this would be easier than implemented comparable in node.


        //parameters -> expression

        //TODO: make sure this lambda stuff works as I intend. Trying to get the one with the minimum
        switch (strategy) {
            case UCS: //g(n)
                comparator = Comparator.comparingInt(Node::getCost); // should reference the method getCost in the node board.
                break;
            case GBFS: //h(n)
                comparator = Comparator.comparingInt(n -> heuristic(n.getBoard()));
                break;
            case ASTAR: // g(n) + h(n)
                comparator = Comparator.comparingInt(n -> n.getCost() + heuristic(n.getBoard()));
                break;
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }

        PriorityQueue<Node> frontier = new PriorityQueue<>(comparator);
        Set<Board> explored = new HashSet<>();



        //manhattan distance - distance mepty square moves


        //cache the goal stuff so that we do not have to recompute it every time.



        Node start_node = new Node(initialState,null,null, 0); //have to pass in cost
        frontier.add(start_node);
        visited.put(initialState,start_node);

        while (!frontier.isEmpty()){
            Node current = frontier.poll();
            if (goalTest(current.getBoard())){
                return current;
            }



            if (explored.contains(current.getBoard())){
                continue;
            }
            explored.add(current.getBoard());

            for (Puzzle.Direction direction : current.getBoard().valid_moves()){
                Board next_board = current.getBoard().move(direction);
                int next_cost = current.getCost() +1;
                //maybe the next_cost should be g(n) + h(n)? Not just the level?

                if (explored.contains(next_board)){
                    continue;
                }

                Node already_seen_node = visited.get(next_board);


                if (already_seen_node == null || next_cost < already_seen_node.getCost()){
                    Node new_node = new Node(next_board,current,direction, next_cost);
                    frontier.add(new_node);
                    visited.put(next_board, new_node);
                }
            }

        }
        return null;

    }



    //For Debgging
    public int compute_cost(Node node){
        switch (strategy) {
            case UCS: //g(n)
                return node.getCost();
            case GBFS: //h(n)
                return heuristic(node.getBoard());
            case ASTAR: // g(n) + h(n)
                return node.getCost() + heuristic(node.getBoard());
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }


    //linear conflicts: thingso ut of order to make a goal state
    //starting at the end state and saving all of the boards that are for example 4 away.




    //Work on this Heuristic, don't think it is correct.
    //TOD Include a parameter to change what the Heuristic is.
    //TODO: Review this heuristic stuff
    public int heuristic(Board board) {
        int heuristic = 0;
        int[] board_array = board.get_board();
        int[] goal_array = this.goalState.get_board();
        int cols = board.get_cols();

        for (int i = 0; i < board_array.length; i++) {
            int tile = board_array[i];
            if (tile != 0) {
                int goalIndex = indexOf(goal_array, tile);
                int currentRow = i / cols;
                int currentCol = i % cols;

                int goalRow = goalIndex / cols;
                int goalCol = goalIndex % cols;

                heuristic += Math.abs(currentRow - goalRow) + Math.abs(currentCol - goalCol);
            }
        }
        return heuristic;
    }

    private int indexOf(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }


    private Node BFS (){
        Queue<Node> queue = new LinkedList<>();
        Node startNode = new Node(initialState, null, null);
        queue.add(startNode);
        visited.put(initialState, startNode);

        while (!queue.isEmpty()) {
            //<<<STATS: CHECKING TO SEE WHAT THE MAX QUEUE SIZE IS!>>>
            if (queue.size()  > Puzzle.max_queue_size){
                Puzzle.max_queue_size = queue.size() ;
            }

            Node current = queue.poll();

            if (goalTest(current.getBoard())) {
                return current;
            }

            for (Puzzle.Direction direction : current.getBoard().valid_moves()) {
                Puzzle.states_expanded ++; //<<<STATS: WE ARE EXPANDING THE STATE>>>
                Board newBoard = current.getBoard().move(direction);
                if (!visited.containsKey(newBoard)) {
                    Node newNode = new Node(newBoard, current, direction);
                    Puzzle.states_created ++; //<<<STATS: WE ARE CREATING A NEW NODE HERE>>>
                    visited.put(newBoard, newNode);
                    queue.add(newNode);
                }
            }
        }

        return null;
    }


    private Node DFS (){
        Node node = new Node(initialState, null, null);
        this.max_depth = Integer.MAX_VALUE;
        return DLS(node, Integer.MAX_VALUE);
    }



    private Node DLS (){
        Node node = new Node(initialState, null, null);
        return DLS(node, max_depth);
    }

    private Node DLS(Node node, int limit) {
        if (node == null || limit <= 0) return null;
        int current_depth = this.max_depth - limit;

        Board currentBoard = node.getBoard();
        if (goalTest(currentBoard)) return node;


        if (!visited.containsKey(currentBoard)) {
            visited.put(currentBoard, node);
        }

        Puzzle.states_expanded ++;
        for (Puzzle.Direction direction : currentBoard.valid_moves()) {
            Board newBoard = currentBoard.move(direction);


            if (!visited.containsKey(newBoard)) {
                Node nextNode = new Node(newBoard, node, direction,current_depth);
                Puzzle.states_created ++;
                Node result = DLS(nextNode, limit - 1 );
                Puzzle.states_explored ++;
                if (result != null) {
                    if (goalTest(result.getBoard())) return result;
                }
            } else {

                Node old_node = visited.get(newBoard);
                int old_cost = old_node.getCost();
                if (old_node.getCost() > current_depth){ //this implies that we found a way to get to the node quicker, at which point we want to update it and explore it.
                    old_node.set_cost(current_depth);
                    old_node.set_lastMove(direction);
                    old_node.set_parent(node);

                    Node result = DLS(old_node, limit - 1 );
                    Puzzle.states_explored ++;
                    if (result != null) {
                        if (goalTest(result.getBoard())) return result;
                    }
                }
            }
        }
        return null;
    }




    private Node ID (){
        int limit = 1;
        while (true) {
            visited.clear();
            Node node = new Node(initialState, null, null);
            this.max_depth = limit;
            Node s = DLS(node, limit);
            if (s != null ) {
                return s;
            }
            limit++;
        }
    }






    public boolean goalTest(Board board){
        try {
            return goalState.equals(board);
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
        return false;
    }


}
