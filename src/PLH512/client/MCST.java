package PLH512.client;

import java.util.ArrayList;
import java.util.List;

import PLH512.server.Board;


public class MCST {

    // Class Variables
    static final int WIN_SCORE = 10;
    private int level;
    private int opponent;
    
    // Setters & Getters
    public static int getWinScore() {
        return WIN_SCORE;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public int getOpponent() {
        return opponent;
    }
    public void setOpponent(int opponent) {
        this.opponent = opponent;
    }

    // Tree Subclass
    public class Tree{

        private Node root;
       
    }

    // Node Subclass
    public class Node {
        private State state;1
        private Node parent;
        private List<Node> childArray;

        public class State {
            private Board board;
            private int playerNo;

            // Algorithm variables
            private int visitCount;
            private double winScore;
        
            // Setters & Getters
            public Board getBoard(){
                return board;
            }
            public void setBoard(Board board){
                this.board = board;
            }

            public int getPlayerNo(){
                return playerNo;
            }
            public void setPlayerNo(int playerNo){
                this.playerNo = playerNo;
            }
        
            public int getVisitCount(){
                return visitCount;
            }
            public void setVisitCount(int visitCount){
                this.visitCount = visitCount;
            }

            public double getWinScore(){
                return winScore;
            }
            public void setWinScore(double winScore){
                this.winScore = winScore;
            }

            // Algorithm Functions
            public List<State> getAllPossibleStates() {
                // constructs a list of all possible states from current state
                List<State> possibleStates = new ArrayList<>();
                State newState = new State();

                if (myColorCount[0] > 4){
                    colorToCure = "Black";
                    myBoard = copyBoard(board);
                    myBoard.cureDisease(myPlayerID, colorToCure);
                    myState = new State();
                    myState.setBoard(myBoard);
                    possibleStates.add(myState);
                }
                if (myColorCount[1] > 4){
                    colorToCure = "Yellow";
                    myBoard = copyBoard(board);
                    board.cureDisease(myPlayerID, colorToCure);
                }
                if (myColorCount[2] > 4){
                    colorToCure = "Blue";
                    myBoard = copyBoard(board);
                    board.cureDisease(myPlayerID, colorToCure);
                }
                if (myColorCount[3] > 4){
                    colorToCure = "Red";
                    myBoard = copyBoard(board);
                    board.cureDisease(myPlayerID, colorToCure);
                }   
            }
            public void randomPlay() {
                /* get a list of all possible positions on the board and 
                play a random move */
            }
        }

    }


}