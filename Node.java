//Andrew Hurlbut
//AI/ML

import java.util.ArrayList;
import java.util.Stack;

public class Node {

    private Board board;
    private Node parent;
    private Puzzle.Direction lastMove;
    private int cost;

    //Should the Node cost be the thing that the dijkstras runs off of.
    //value of node method node --> value

    //create their different methods, one for each of the algorithms.




    public Node (Board board, Node parent, Puzzle.Direction lastMove){
        this.board = board;
        this.parent = parent;
        this.lastMove = lastMove;
    }
    public Node (Board board, Node parent, Puzzle.Direction lastMove, int cost){
        this.board = board;
        this.parent = parent;
        this.lastMove = lastMove;
        this.cost = cost;
    }

    public Board getBoard(){
        return this.board;
    }

    public Node getParent(){
        return this.parent;
    }


    //Need to use these to update the parameters.
    public void set_parent(Node parent){
        this.parent = parent;
    }

    public void set_lastMove(Puzzle.Direction action){
        this.lastMove = action;
    }

    public void set_cost(int cost){
        this.cost = cost;
    }

    public int getCost(){
        return this.cost;
    }

    public void increment_cost(){
        this.cost += 1;
    }
    public void increment_cost(int cost){
        this.cost += cost;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) return true; // Same reference check
        if (!(other instanceof Node)) return false; // Type check
        Node other_node = (Node) other;
        return this.board.equals(other_node.board) && this.cost == other_node.cost && this.parent.equals(other_node.parent);
    }

    @Override
    public int hashCode() {
        return 31 * board.hashCode() + Integer.hashCode(cost);
    }


    public ArrayList<Puzzle.Direction> backtrack(){

        ArrayList<Puzzle.Direction> sequence = new ArrayList<>();
        Stack<Puzzle.Direction> reverse_sequence = new Stack<>();

        Node node = this;
        while (node.lastMove != null){
            Puzzle.num_moves ++;
            reverse_sequence.add(node.lastMove);
            node = node.getParent();

        }

        while (!reverse_sequence.isEmpty()) {
            sequence.add(reverse_sequence.pop());
        }

        return sequence;
    }

}
