//Andrew Hurlbut
//AI/ML

import java.util.ArrayList;
import java.util.Arrays;

public final class Board {

    //make 9 static arrays, and return them for the possible directions
    private static Puzzle.Direction[] upper_left = {Puzzle.Direction.DOWN, Puzzle.Direction.RIGHT};
    private static Puzzle.Direction[] upper_right = {Puzzle.Direction.DOWN, Puzzle.Direction.LEFT};
    private static Puzzle.Direction[] upper_middle = {Puzzle.Direction.DOWN, Puzzle.Direction.LEFT, Puzzle.Direction.RIGHT};
    private static Puzzle.Direction[] lower_left = {Puzzle.Direction.UP, Puzzle.Direction.RIGHT};
    private static Puzzle.Direction[] lower_right = {Puzzle.Direction.UP, Puzzle.Direction.LEFT};
    private static Puzzle.Direction[] lower_middle = {Puzzle.Direction.UP, Puzzle.Direction.LEFT, Puzzle.Direction.RIGHT};
    private static Puzzle.Direction[] right_middle = {Puzzle.Direction.LEFT, Puzzle.Direction.UP, Puzzle.Direction.DOWN};
    private static Puzzle.Direction[] left_middle = {Puzzle.Direction.RIGHT, Puzzle.Direction.UP, Puzzle.Direction.DOWN};
    private static Puzzle.Direction[] all_moves = {Puzzle.Direction.RIGHT, Puzzle.Direction.UP, Puzzle.Direction.DOWN, Puzzle.Direction.LEFT};

    public static boolean top_left_to_Win = false;
    public static boolean check_board = true;

    private final int[] board;
    private final int rows;
    private final int cols;

    private final int empty_index;

    public Board(int rows, int cols, int[] board) {
        this(rows, cols, board, find_empty_index(board));
    }


    public Board(int rows, int cols, int[] board, int empty_index) {
        this.rows = rows;
        this.cols = cols;
        this.empty_index = empty_index;

        if (check_board) {
            if (is_valid_board(board)) {
                this.board = Arrays.copyOf(board, board.length);
            } else {
                throw new IllegalArgumentException("Invalid board");
            }
        } else {
            this.board = Arrays.copyOf(board, board.length);
        }
    }

    private static int find_empty_index(int[] board){
        int empty_index = -1;
        for (int i = 0; i < board.length; i++) {
            if (board[i] == 0) {
                empty_index = i;
                break;
            }
        }

        if (empty_index == -1) {
            throw new IllegalArgumentException("Board must contain an empty space (represented by 0).");
        }
        return empty_index;
    }

    //Getters:
    public int get_rows(){
        return this.rows;
    }
    public int get_cols(){
        return this.cols;
    }
    public int getEmpty_index(){
        return this.empty_index;
    }
    public int[] get_board(){
        return this.board;
    }

    private static boolean is_valid_board(int[] board) {
        boolean[] tiles = new boolean[board.length];

        boolean empty_square = false;

        for (int i = 0; i < board.length; i++) {
            int current = board[i];

            if (current == 0) {
                if (empty_square) {
                    return false;
                } else {
                    empty_square = true;
                }
            } else {
                if (current < 1 || current >= board.length) {
                    return false;
                }

                if (tiles[current]) {
                    return false;
                } else {
                    tiles[current] = true;
                }
            }
        }

        return empty_square;
    }

    public boolean is_solved() {
        if (top_left_to_Win) {
            return get_square(1, 1) == 0;
        } else {
            return get_square(rows, cols) == 0;
        }
    }

    public int get_square(int row, int col) {
        assert row >= 1 && row <= rows : "Row out of bounds";
        assert col >= 1 && col <= cols : "Column out of bounds";

        int array_index = (row - 1) * cols + (col - 1);
        return board[array_index];
    }

    public Puzzle.Direction[] valid_moves(){

        boolean top = false;
        boolean bottom = false;
        boolean left = false;
        boolean right = false;

        int empty_spot = this.empty_index + 1;

        //on the top row: can't move up
        if (empty_spot > 0 && empty_spot <= cols ){
            top = true;
        }
        // on the bottom row: can't move down
        if (empty_spot > (rows - 1)*cols){
            bottom = true;

        }
        // on the right side:
        if ((empty_spot % cols) == 0){
            right = true;
        }
        //  on the left side
        if ((empty_spot % cols) == 1){
            left = true;
        }

        if (top){
            if (left){
                return Board.upper_left;
            } else if (right){
                return Board.upper_right;
            } else {
                return Board.upper_middle;
            }
        } else if (bottom){
            if (right){
                return Board.lower_right;
            } else if (left){
                return Board.lower_left;
            } else {
                return Board.lower_middle;
            }
        } else {
            if (left){
                return Board.left_middle;
            } else if (right){
                return Board.right_middle;
            }
        }
        return Board.all_moves;

    }


    public Board move(Puzzle.Direction direction){

        Puzzle.Direction[] valid_moves= this.valid_moves();
        boolean move_is_valid = false;
        for (Puzzle.Direction d : valid_moves){
            if (d == direction){
                move_is_valid = true;
            }
        }
        if (!move_is_valid){
            throw new IllegalArgumentException("you can't move " + direction.name() + " on this board." );
        }


        int new_empty_index = -1;
        int[] new_board = Arrays.copyOf(this.board, this.board.length);

        //Right
        switch (direction){
            case RIGHT:
                 new_empty_index = this.empty_index + 1;
                 int t1 = new_board[new_empty_index];
                 new_board[this.empty_index] = t1;
                 new_board[new_empty_index] = 0;
                 break;
            case LEFT: //switch this one with the original -1
                new_empty_index = this.empty_index -1;
                int t2 = new_board[new_empty_index];
                new_board[this.empty_index] = t2;
                new_board[new_empty_index] = 0;
                break;
            case DOWN:
                 new_empty_index = this.empty_index + cols;
                 int t3 = new_board[new_empty_index];
                 new_board[this.empty_index] = t3;
                 new_board[new_empty_index] = 0;
                 break;
            case UP:
                new_empty_index = this.empty_index - cols;
                int t4 = new_board[new_empty_index];
                new_board[this.empty_index] = t4;
                new_board[new_empty_index] = 0;
                break;
        }

        return new Board(this.rows, this.cols, new_board, new_empty_index);

    }


    @Override
    public boolean equals(Object other){
        return equals((Board)other);
    }

    public boolean equals(Board other){
        if (other == null) throw new IllegalArgumentException("other board was null");
        if (this.cols != other.cols) return false;
        if (this.rows != other.rows) return false;

        for (int i = 0; i < this.board.length; i++){
            if (this.board[i] != other.board[i]){
                return false;
            }
        }

        return true;
    }


    @Override
    public int hashCode(){
        int hash = 0;
        for(int tile: board){
            hash += tile;
            hash *= board.length;
        }

        return hash;

    }





    public void print() {
        int maxWidth = 0;
        for (int value : board) {
            int width = String.valueOf(value).length();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                int value = get_square(i, j);
                int padding = maxWidth - String.valueOf(value).length();

                for (int k = 0; k < padding; k++) {
                    System.out.print(" ");
                }
                if (value == 0){
                    System.out.print(" ");
                } else {
                    System.out.print(value);
                }


                if (j < cols) {
                    System.out.print(" | ");
                }
            }
            System.out.println();

            if (i < rows) {
                for (int j = 1; j <= cols; j++) {
                    for (int k = 0; k < maxWidth; k++) {
                        System.out.print("-");
                    }
                    if (j < cols) {
                        System.out.print("-+-");
                    }
                }
                System.out.println();
            }
        }
    }
}
