//Andrew Hurlbut
//AI/ML

import java.util.ArrayList;

public class Puzzle {

    //COMMAND TO RUN: time java Puzzle `cat 4x4Puzzles/4x4.48.txt` -astar -stats



    //STATISTICS:
    public static int num_moves = 0;
    public static int states_explored = 0;
    public static int max_queue_size = 0;

    enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    enum SearchStrategy {
        BFS, DFS, ID, DLS, UCS, GBFS, ASTAR
    }

    public static void print_stats(SearchStrategy s){

        System.out.println("Moves = " + num_moves);
        System.out.println("States explored = " + states_explored);
        if (s.equals(SearchStrategy.BFS)){
            System.out.println("Max queue size = " + max_queue_size);
        }
        System.out.println("Branching Factor: " + String.format("%.3f", branching_factor()));

    }

    public static double branching_factor(){
        return Math.pow(states_explored, 1.0 /(num_moves));
    }


    public static void main(String[] args) {
        int rows = 4;
        int columns = 4;
        ArrayList<Integer> tiles = new ArrayList<>();
        boolean stats = false;

        boolean verbose = false;
        ArrayList<Direction> moves = new ArrayList<>();

        SearchStrategy searchStrategy = SearchStrategy.BFS;
        int DLS_limit = 25;



        outerLoop:
        for (int i = 0; i < args.length; i ++){
            String arg = args[i];

            switch (arg){
                case "-rows": rows = Integer.parseInt(args[++i]); continue outerLoop;
                case "-columns": columns = Integer.parseInt(args[++i]); continue outerLoop;
                case "-top": Board.top_left_to_Win = true; continue outerLoop;
                case "-bottom": Board.top_left_to_Win = false; continue outerLoop;
                case "-verbose": verbose = true; continue outerLoop;
                case "-limit": DLS_limit = Integer.parseInt(args[++i]); continue outerLoop;
                case "-stats": stats = true; continue outerLoop;
                case "-size": int size = Integer.parseInt(args[++i]); rows = size; columns = size; continue outerLoop;

                //moves
                case "UP": moves.add(Direction.UP); continue outerLoop;
                case "DOWN": moves.add(Direction.DOWN); continue outerLoop;
                case "LEFT": moves.add(Direction.LEFT); continue outerLoop;
                case "RIGHT": moves.add(Direction.RIGHT); continue outerLoop;

                //Solving strategy
                case "-bfs": searchStrategy = SearchStrategy.BFS; continue outerLoop;
                case "-dfs": searchStrategy = SearchStrategy.DFS; continue outerLoop;
                case "-dls": searchStrategy = SearchStrategy.DLS; continue outerLoop;
                case "-id": searchStrategy = SearchStrategy.ID; continue outerLoop;
                case "-ucs": searchStrategy = SearchStrategy.UCS; continue outerLoop;
                case "-gbfs": searchStrategy = SearchStrategy.GBFS; continue outerLoop;
                case "-astar": searchStrategy = SearchStrategy.ASTAR; continue outerLoop;

                //Reading in tiles
                default:
                    try{
                        if (arg.equals(".")){
                            tiles.add(0); continue outerLoop;
                        }
                        int tile = Integer.parseInt(arg);
                        tiles.add(tile);
                    } catch (Exception e){
                        System.err.println(args[i] + " is not a valid tile or parameter");
                    }
            }
        }

        int[] board_nums = new int[columns*rows];
        for (int i = 0; i <rows*columns && tiles.size() > i; i ++){
            board_nums[i] = tiles.get(i);
        }

        try{
            Board board = new Board(rows, columns, board_nums);

            int[] goal_board = new int[rows*columns];
            if (Board.top_left_to_Win){
                goal_board[0] = 0;
                for (int i = 1; i < goal_board.length; i ++){
                    goal_board[i] = i;
                }
            } else {
                goal_board[goal_board.length - 1] = 0;
                for (int i = 0; i < goal_board.length -1; i ++){
                    goal_board[i] = i+1;
                }
            }

            Board goal_state = new Board(rows, columns, goal_board);
            RationalAgent agent = new RationalAgent(board,goal_state,searchStrategy, DLS_limit);

            try {
                moves = agent.solve();
                if (moves == null){
                    System.out.println("No solution");
                    System.exit(0);
                }
            }catch(Exception e){
                System.out.println(e.getMessage());
            }

            if (verbose){
                board.print();
            }

            for (Direction direction : moves){
                board = board.move(direction);
                System.out.println(direction.name());
                if (verbose){
                    board.print();
                }
            }

            if (board.is_solved()){
                System.out.println("\nSolved");
            } else {
                System.out.println("\nNot Solved");
            }

            if (stats){
                print_stats(searchStrategy);
            }

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
