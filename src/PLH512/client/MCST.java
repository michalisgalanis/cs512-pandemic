package PLH512.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import PLH512.server.Board;

public class MCST {

    /* Class Variables */
    static final int WIN_SCORE = 10;
    private int level;
    private int opponent;

    /* Setters & Getters */
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


    /* Class functions */
    public Board findNextMove(Board board, int playerNo) {
        Tree tree = new Tree();
        Node rootNode = tree.getRoot();
        rootNode.getState().setBoard(board);
        
        while(true){
            /* Phase 1 - Selection */
            Node selectedNode = selectNode(rootNode);

            /* Phase 2 - Expansion */
            expandNode(selectedNode);

            /* Phase 3 - Simulation */
            Node nodeToExplore = selectedNode;
            if (selectedNode.getChildArray().size() > 0) 
                nodeToExplore = selectedNode.getRandomChildNode();
            
            int playoutResult = simulateRandomPlayout(nodeToExplore);

            /* Phase 4 - Update */
            backPropogation(nodeToExplore, playoutResult);
        }
    }

    private Node selectNode(Node rootNode) {
        Node node = rootNode;
        while (node.getChildArray().size() != 0) {
            node = findBestNodeWithUCB(node);
        }
        return node;
    }

    public static Node findBestNodeWithUCB(Node node) {
        int parentVisits = node.getState().getVisitCount();
        double[] ucbValues = new double[node.getChildArray().size()];
        double maxUcbValue = Integer.MIN_VALUE;
        int maxUcbIdx = 0;
        for (int i = 0; i < node.getChildArray().size(); i++) {
            ucbValues[i] = ucbValue(parentVisits, node.getChildArray().get(i).state.getValue(), node.getChildArray().get(i).state.getVisitCount());
            maxUcbValue = Math.max(maxUcbValue, ucbValues[i]);
            maxUcbIdx = i;
        }
        return node.getChildArray().get(maxUcbIdx);
    }
    
    public static double ucbValue(int parentVisits, double value, int childVisits) {
        if (childVisits == 0) 
            return Integer.MAX_VALUE;

        return ((value / (double) childVisits) + 2 * Math.sqrt(Math.log(parentVisits) / (double) childVisits));
    }

    private void expandNode(Node node) {
        List<State> possibleStates = node.getState().getAllPossibleStates();
        for (int i = 0; i < possibleStates.size(); i++){
            State state = possibleStates.get(i);
            Node newNode = new Node();
            newNode.setState(state);
            newNode.setParent(node);

            newNode.getState().setNumberOfActions(node.getState().getNumberOfActions() + 1);
            
            node.getChildArray().add(newNode);
            Board myBoard = newNode.getState().getBoard();

            if (myBoard.checkIfWon()) 
                break;

            /* Calculate Number of Players */
            int numberOfPlayers = 0;
            while (true){
                if (myBoard.getUsernames(numberOfPlayers).equals("")) 
                    break;
                numberOfPlayers++;
            }
            
            if(node.getState().getNumberOfActions() == 4){
                newNode.getState().setNumberOfActions(0);
                if (myPlayerId == numberOfPlayers - 1) 
                    newNode.getState().setPlayerNo(0);
                else 
                    newNode.getState().setPlayerNo(myPlayerId + 1);
            }
            else{
                newNode.getState().setNumberOfActions(node.getState().getNumberOfActions()++);
                newNode.getState().setPlayerNo(myPlayerId);
            }
                
            if (myBoard.getWhoIsPlaying() == numberOfPlayers - 1) 
                myBoard.setWhoIsPlaying(0);
                 // Back to first player
            else 
                myBoard.setWhoIsPlaying(myBoard.getWhoIsPlaying() + 1);
                
			// myBoard.drawCards(myBoard.getWhoIsPlaying(), 2);
			// System.out.println("");
			
			// if (!myBoard.getIsQuietNight())
			// 	myBoard.infectCities(myBoard.getInfectionRate(), 1);
			// else 
			// 	myBoard.setIsQuietNight(false);
			// System.out.println("");
			
			// myBoard.resetTalkedForThisTurn();
        }
    }

    private void backPropogation(Node nodeToExplore, int playoutResult) {
        Node tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.getState().incrementVisit();
            tempNode.getState().incrementValue(playoutResult);
            tempNode = tempNode.getParent();
        }
    }

    private int simulateRandomPlayout(Node node) {
        Node tempNode = node;
        State tempState = tempNode.getState();
        
        /* Final State */
        if (tempState.getBoard().getGameEnded()) {
            /* Reward Win */
            if (tempState.getBoard().checkIfWon())
                return Integer.MAX_VALUE;
            /* Punish Defeat */
            else
                return Integer.MIN_VALUE;
        }
        boolean changedPlayer = false;
        State newState = tempState;
        
        while (!(newState.playerNo == tempState.playerNo && changedPlayer)) {
            List<State> possibleStates = tempNode.getState().getAllPossibleStates();
            Random rand = new Random();
            newState = possibleStates.get(rand.nextInt(possibleStates.size()));
            Node newNode = new Node();
            newNode.setState(newState);
            newState.setNumberOfActions(tempState.numberOfActions);
            newState.setPlayerNo(tempState.playerNo);
            Board myBoard = newState.getBoard();
            
            if (myBoard.checkIfWon()) 
                break;
            /* Calculate Number of Players */
            int numberOfPlayers = 0;
            while (true){
                if (myBoard.getUsernames(numberOfPlayers).equals("")) 
                    break;
                numberOfPlayers++;
            }
            
            if(newState.getNumberOfActions() == 4){
                newState.setNumberOfActions(0);
                changedPlayer = true;
                if (newState.playerNo == numberOfPlayers - 1) 
                newState.setPlayerNo(0);
                else 
                newState.setPlayerNo(newState.playerNo + 1);
            }
            else{
                newState.setNumberOfActions(newState.getNumberOfActions() + 1);
                newState.setPlayerNo(newState.playerNo);
            }
                
            if (myBoard.getWhoIsPlaying() == numberOfPlayers - 1) 
                myBoard.setWhoIsPlaying(0);
                 // Back to first player
            else 
                myBoard.setWhoIsPlaying(myBoard.getWhoIsPlaying() + 1);
        }
        return heuristic(newState);
    } 

    /* Tree Subclass */
    public class Tree{

        private Node root;
        
        public Node getRoot(){
            return root;
        }
    }

    /* Node Subclass */
    public class Node {
        private State state;
        private Node parent;
        private List<Node> childArray;

        public State getState(){
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public List<Node> getChildArray() {
            return childArray;
        }

        public Node getRandomChildNode(){
            Random rand = new Random();
            return childArray.get(rand.nextInt(getChildArray().size())); /* Need to be checked */
        }

    }

    public class State {
        private Board board;
        private int playerNo;
        private int numberOfActions;

        /* Algorithm variables */
        private int visitCount;
        private double value;
    
        /* Setters & Getters */
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

        public double getValue(){
            return value;
        }

        public void setValue(double value){
            this.value = value;
        }

        public int getNumberOfActions() {
            return numberOfActions;
        }

        public void setNumberOfActions(int numberOfActions) {
            this.numberOfActions = numberOfActions;
        }

        public void increaseNumberOfActions(){
            this.numberOfActions++;
        }

        public void incrementVisit(){
            this.visitCount++;
        }

        public void incrementValue(int value){
            this.value += value;
        }

        /* Algorithm Functions */
        public List<State> getAllPossibleStates() {
            /* constructs a list of all possible states from current state */
            List<State> possibleStates = new ArrayList<>();
            Board myBoard;
            State myState;
            int cards_needed_for_cure;
            /* Discover a Cure */
            if (board.getRoleOf(myPlayerID).equals("Scientist"))
                cards_needed_for_cure = 3;
            else
                cards_needed_for_cure = 4;

            if (myColorCount[0] > cards_needed_for_cure){
                colorToCure = "Black";
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.cureDisease(myPlayerID, colorToCure);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }
            if (myColorCount[1] > cards_needed_for_cure){
                colorToCure = "Yellow";
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.cureDisease(myPlayerID, colorToCure);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }
            if (myColorCount[2] > cards_needed_for_cure){
                colorToCure = "Blue";
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.cureDisease(myPlayerID, colorToCure);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }
            if (myColorCount[3] > cards_needed_for_cure){
                colorToCure = "Red";
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.cureDisease(myPlayerID, colorToCure);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }

            /* Treat disease */
            if (myCurrentCityObj.getBlackCubes() > 0){
                colorToTreat = "Black";
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }                
            if ( myCurrentCityObj.getYellowCubes() > 0){
                colorToTreat = "Yellow";
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }
            if (myCurrentCityObj.getBlueCubes() > 0){
                colorToTreat = "Blue";
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }      			
            if (myCurrentCityObj.getRedCubes() > 0){
                colorToTreat = "Red";
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }

            /* Drive/Ferry */
            for (int i = 0; i < myCurrentCityObj.getNeighboursNumber(); i++){
                String neighboor = myCurrentCityObj.getNeighbour(i);
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.driveTo(myPlayerID, neighboor);
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }    
            
            /* Direct Flight */
            ArrayList<String> cards = board.getHandOf(myPlayerID);
            for (int i = 0; i < cards.size(); i++){
                myBoard = new Board();
                myBoard = copyBoard(board);
                myBoard.(myPlayerID, cards.get(i));
                myState = new State();
                myState.setBoard(myBoard);
                possibleStates.add(myState);
            }

            /* Charter Flight */
            for (int i = 0; i < cards.size(); i++){
                if (myCurrentCity.equals(cards.get(i))){
                    for (int j = 0; j < 48; j++){
                        location = board.searchForCity(j).getName();
                        if (location.equals(myCurrentCity)) continue;
                        myBoard = new Board();
                        myBoard = copyBoard(board);
                        myBoard.charterFlight(myPlayerID, location);
                        myState = new State();
                        myState.setBoard(myBoard);
                        possibleStates.add(myState);
                    }
                }
            }

            /* Shuttle Flight */
            if (myCurrentCityObj.hasReseachStation()){
                ArrayList<String> locations = board.getRSLocations();
                for (int i = 0; i < locations.size(); i++){
                    myBoard = new Board();
                    myBoard = copyBoard(board);
                    myBoard.shuttleFlight(myPlayerID, locations.get(i));
                    myState = new State();
                    myState.setBoard(myBoard);
                    possibleStates.add(myState);
                }   
            }
            
            /* Build a Research Station */
            for (int i = 0; i < cards.size(); i++){
                if (myCurrentCity.equals(cards.get(i))){
                    myBoard = new Board();
                    myBoard = copyBoard(board);
                    myBoard.buildRS(myPlayerID, myCurrentCity);
                    myState = new State();
                    myState.setBoard(myBoard);
                    possibleStates.add(myState);
                }
            }
        }

    }
}