//Andrew Hurlbut
//AI/ML


import java.util.*;

public class RationalAgent {


    private Board goalState;
    private Board initialState;
    private Puzzle.SearchStrategy strategy;
    private int max_depth;

    private HashMap<Board, Node> visited = new HashMap<Board, Node>();


    private HashMap<Integer,Integer> cached_tile_positions = new HashMap<Integer,Integer>();
    private HashMap<Board,Integer> cached_boards = new HashMap<Board,Integer>();
    private HashSet<Board> visited_cached_boards = new HashSet<>();


    public RationalAgent(Board initialState, Board goalState, Puzzle.SearchStrategy strategy){
        this.goalState = goalState;
        this.initialState = initialState;
        this.strategy = strategy;
        cache_tile_positions();
        cache_board_states();
    }

    public RationalAgent(Board initialState, Board goalState, Puzzle.SearchStrategy strategy, int depth){
        this.goalState = goalState;
        this.initialState = initialState;
        this.strategy = strategy;
        this.max_depth = depth;
        cache_tile_positions();
        cache_board_states();
    }

    private void cache_tile_positions() {
        int[] goalBoard = goalState.get_board();
        for (int i = 0; i < goalBoard.length; i++) {
            int tileValue = goalBoard[i];
            cached_tile_positions.put(tileValue, i);
        }
    }

    private void cache_board_states(){
       cache_board_states(0,6, goalState);
    }

    private void cache_board_states(int current_depth, int max_depth, Board board) {
        if (current_depth > max_depth) {
            return;
        }

        Integer existing_depth = cached_boards.get(board);
        if (existing_depth == null || current_depth < existing_depth) {
            cached_boards.put(board, current_depth);
        } else {
            return;
        }
        if (visited_cached_boards.contains(board)) {
            return;
        }
        visited_cached_boards.add(board);

        for (Puzzle.Direction direction : board.valid_moves()) {
            Board new_board = board.move(direction);
            cache_board_states(current_depth + 1, max_depth, new_board);
        }

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



    private Node search(Puzzle.SearchStrategy strategy){

        //Comparator I think is easier
        Comparator<Node> comparator;

        //Lambda: parameters -> expression, :: reference method
        switch (strategy) {
            case UCS: //g(n)
                comparator = Comparator.comparingInt(Node::getCost);
                break;
            case GBFS: //h(n)
                comparator = Comparator.comparingInt(n -> heuristic(n.getBoard()));
                break;
            case ASTAR: // g(n) + h(n)
                comparator = Comparator.comparingInt(n -> n.getCost() + heuristic(n.getBoard()));
                break;
            default:
                throw new IllegalArgumentException("Not a valid Strategy: " + strategy);
        }

        PriorityQueue<Node> frontier = new PriorityQueue<>(comparator);
        Set<Board> explored = new HashSet<>();

        Node start_node = new Node(initialState,null,null, 0); //have to pass in cost
        frontier.add(start_node);
        visited.put(initialState,start_node);
        Puzzle.states_explored++;

        while (!frontier.isEmpty()){
            Node current = frontier.poll();

            if (goalTest(current.getBoard())){
                return current;
            }

            if (explored.contains(current.getBoard())){
                continue;
            }
            explored.add(current.getBoard());
            Puzzle.states_explored++;

            for (Puzzle.Direction direction : current.getBoard().valid_moves()){
                Board next_board = current.getBoard().move(direction);
                int next_cost = current.getCost() +1;

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



    //Mostly For debugging
    private int compute_cost(Node node){
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

    //The manhattan distance and the cached board literally always return the same value. Not sure why .
    private int heuristic(Board board) {
        int manhattan = manhattan_distance(board);
        int linearConflict = linear_conflict(board);
        return Math.max(manhattan + 2 * linearConflict, cached_boards(board));
    }


    // 0, 2, 1.
    //Linear conflict, not so easy to just switch the two around.
    //Correct row or correct col but out of order, how many tiles needed to be removed to have no linear conflicts?
    private int linear_conflict(Board board) {
        int linear_conflict = 0;
        int size = board.get_rows();
        int[] board_array = board.get_board();
        int cols = board.get_cols();

        //Rows
        for (int row = 0; row < size; row++) {
            List<Integer> tiles_in_row = new ArrayList<>();
            for (int col = 0; col < cols; col++) {
                int index = row * cols + col;
                int tile = board_array[index];
                if (tile != 0) {
                    int goal_row = getGoalRow(tile);
                    if (goal_row == row) {
                        tiles_in_row.add(tile);
                    }
                }
            }
            linear_conflict += count_conflicts(tiles_in_row, true);
        }

        //Cols
        for (int col = 0; col < cols; col++) {
            List<Integer> tiles_in_col = new ArrayList<>();
            for (int row = 0; row < size; row++) {
                int index = row * cols + col;
                int tile = board_array[index];
                if (tile != 0) {
                    int goal_col = getGoalCol(tile);
                    if (goal_col == col) {
                        tiles_in_col.add(tile);
                    }
                }
            }
            linear_conflict += count_conflicts(tiles_in_col, false);
        }

        return linear_conflict;
    }


    private int count_conflicts(List<Integer> tiles, boolean is_row) {
        int conflicts = 0;
        for (int i = 0; i < tiles.size(); i++) {
            int tile_a = tiles.get(i);
            int goal_pos_a;
            if (is_row) {
                goal_pos_a = getGoalCol(tile_a);
            } else {
                goal_pos_a = getGoalRow(tile_a);
            }

            for (int j = i + 1; j < tiles.size(); j++) {
                int tile_b = tiles.get(j);
                int goal_pos_b;
                if (is_row) {
                    goal_pos_b = getGoalCol(tile_b);
                } else {
                    goal_pos_b = getGoalRow(tile_b);
                }

                if (goal_pos_a > goal_pos_b) {
                    conflicts++;
                }
            }
        }
        return conflicts;
    }




    private int getGoalRow(int tile) {
        int goalIndex = cached_tile_positions.get(tile);
        return goalIndex / goalState.get_cols();
    }

    private int getGoalCol(int tile) {
        int goalIndex = cached_tile_positions.get(tile);
        return goalIndex % goalState.get_cols();
    }


    private int cached_boards(Board board){
        if (cached_boards.containsKey(board)){
            return cached_boards.get(board);
        }
        return 0;
    }




    private int manhattan_distance(Board board){

        int heuristic = 0;
        int[] board_array = board.get_board();

        for (int i = 0; i < initialState.get_board().length; i ++){
            int tile = board_array[i];
            if (tile != 0){
                int goal_index = cached_tile_positions.get(tile);
                int goal_row = goalState.get_row(goal_index);
                int goal_col= goalState.get_col(goal_index);

                int current_row = board.get_row(i);
                int current_col = board.get_col(i);

                heuristic += Math.abs(current_row - goal_row) + Math.abs(current_col - goal_col);
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
                Board newBoard = current.getBoard().move(direction);
                if (!visited.containsKey(newBoard)) {
                    Node newNode = new Node(newBoard, current, direction);
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

        for (Puzzle.Direction direction : currentBoard.valid_moves()) {
            Board newBoard = currentBoard.move(direction);

            if (!visited.containsKey(newBoard)) {
                Node nextNode = new Node(newBoard, node, direction,current_depth);
                Node result = DLS(nextNode, limit - 1 );
                Puzzle.states_explored ++;
                if (result != null) {
                    if (goalTest(result.getBoard())) return result;
                }
            } else {

                Node old_node = visited.get(newBoard);
                int old_cost = old_node.getCost();
                if (old_node.getCost() > current_depth){
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
