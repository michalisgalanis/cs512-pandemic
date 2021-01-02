package PLH512.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import PLH512.client.Client.MCST.State;
import PLH512.server.Board;
import PLH512.server.City;

public class Client  
{
    final static int ServerPort = 64240;
    final static String username = "myName";
  
    public static void main(String args[]) throws UnknownHostException, IOException, ClassNotFoundException  
    { 
    	int numberOfPlayers;
    	int myPlayerID;
    	String myUsername;
    	String myRole;
    	
        
        // Getting localhost ip 
        InetAddress ip = InetAddress.getByName("localhost"); 
          
        // Establish the connection 
        Socket s = new Socket(ip, ServerPort); 
        System.out.println("\nConnected to server!");
        
        // Obtaining input and out streams 
        ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream dis = new ObjectInputStream(s.getInputStream());  
        
        // Receiving the playerID from the Server
        myPlayerID = (int)dis.readObject();
        myUsername = "User_" + myPlayerID;
        System.out.println("\nHey! My username is " + myUsername);
        
        // Receiving number of players to initialize the board
        numberOfPlayers = (int)dis.readObject();
        
        // Receiving my role for this game
        myRole = (String)dis.readObject();
        System.out.println("\nHey! My role is " + myRole);
        
        // Sending the username to the Server
        dos.reset();
        dos.writeObject(myUsername);
        
        // Setting up the board
        Board[] currentBoard = {new Board(numberOfPlayers)};
        
        // Creating sendMessage thread 
        Thread sendMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() {
            	
            	boolean timeToTalk = false;
            	
            	//MPOREI NA GINEI WHILE  TRUE ME BREAK GIA SINTHIKI??
                while (currentBoard[0].getGameEnded() == false) 
                { 	
                	timeToTalk = ((currentBoard[0].getWhoIsTalking() == myPlayerID)  && !currentBoard[0].getTalkedForThisTurn(myPlayerID));
                	
                	try {
						TimeUnit.MILLISECONDS.sleep(15);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                	
                    try { 
                        // Executing this part of the code once per round
                        if (timeToTalk)
                        {
                        	
                        	// Initializing variables for current round
                        	
                        	Board myBoard = currentBoard[0];
                        	
                        	String myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
							
                        	ArrayList<String> myHand = myBoard.getHandOf(myPlayerID);
                        	
                        	int[] myColorCount = {0, 0, 0, 0};
                        	
                        	for (int i = 0 ; i < 4 ; i++)
                        		myColorCount[i] =  cardsCounterOfColor(myBoard, myPlayerID, myBoard.getColors(i));
                        	
                        	ArrayList<citiesWithDistancesObj> distanceMap = new ArrayList<citiesWithDistancesObj>();
                        	distanceMap = buildDistanceMap(myBoard, myCurrentCity, distanceMap);
                        	
                        	
							String myAction = "";
							String mySuggestion = "";
							
                        	// Printing out my current hand
                        	System.out.println("\nMy current hand...");
                        	printHand(myHand);
                        	
                        	// Printing out current color count
                        	System.out.println("\nMy hand's color count...");
                        	for (int i = 0 ; i < 4 ; i++)
                        		System.out.println(myBoard.getColors(i) + " cards count: " + myColorCount[i]);
                        	
                        	// Printing out distance map from current city
                        	//System.out.println("\nDistance map from " + myCurrentCity);
                        	//printDistanceMap(distanceMap);
                        	
                        	// ADD YOUR CODE FROM HERE AND ON!! 
							
							MCST mcst = new MCST(myBoard);
							State stateMove;
							for (int i = 0; i < 4; i++){
								stateMove = mcst.findNextMove(myBoard, i);
								myAction = myAction + stateMove.getMyAction();
								myBoard = stateMove.getBoard();
								myBoard.setWhoIsPlaying(stateMove.playerPlaying);
							}

                        	// UP TO HERE!! DON'T FORGET TO EDIT THE "msgToSend"
                        	
                        	// Message type 
                        	// toTextShuttleFlight(0,Atlanta)+"#"+etc
                        	String msgToSend;
                        	if (myBoard.getWhoIsPlaying() == myPlayerID)
                        		msgToSend = myAction;
                        		
                        		//msgToSend = "AP,"+myPlayerID+"#AP,"+myPlayerID+"#AP,"+myPlayerID+"#C,"+myPlayerID+",This was my action#AP,"+myPlayerID+"#C,"+myPlayerID+",This should not be printed..";//"Action";
                            else 
                        		msgToSend = "#C,"+myPlayerID+",This was my recommendation"; //"Recommendation"
                        	
                        	// NO EDIT FROM HERE AND ON (EXEPT FUNCTIONS OUTSIDE OF MAIN() OF COURSE)
                        	
                        	// Writing to Server
                        	dos.flush();
                        	dos.reset();
                        	if (msgToSend != "")
                        		msgToSend = msgToSend.substring(1); // Removing the initial delimeter
                        	dos.writeObject(msgToSend);
                        	System.out.println(myUsername + " : I've just sent my " + msgToSend);
                        	currentBoard[0].setTalkedForThisTurn(true, myPlayerID);
                        }
                    } catch (IOException e) { 
                        e.printStackTrace(); 
					}
                } 
            } 
        }); 
          
        // Creating readMessage thread 
        Thread readMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
            	
            	
                while (currentBoard[0].getGameEnded() == false) { 
                    try { 
                        
                    	// Reading the current board
                    	//System.out.println("READING!!!");
                    	currentBoard[0] = (Board)dis.readObject();
                    	//System.out.println("READ!!!");
                    	
                    	// Read and print Message to all clients
                    	String prtToScreen = currentBoard[0].getMessageToAllClients();
                    	if (!prtToScreen.equalsIgnoreCase(""))
                    		System.out.println(prtToScreen);
                    	
                    	// Read and print Message this client
                    	prtToScreen = currentBoard[0].getMessageToClient(myPlayerID);
                    	if (!prtToScreen.equalsIgnoreCase(""))
                    		System.out.println(prtToScreen);
                    	
                    } catch (IOException e) { 
                        e.printStackTrace(); 
                    } catch (ClassNotFoundException e) {
						e.printStackTrace();
					} 
                } 
            } 
        }); 
        
        // Starting the threads
        readMessage.start();
        sendMessage.start(); 
        
        // Checking if the game has ended
        while (true) 
        {
        	if (currentBoard[0].getGameEnded() == true) {
        		System.out.println("\nGame has finished. Closing resources.. \n");
        		//scn.close();
            	s.close();
            	System.out.println("Recources closed succesfully. Goodbye!");
            	System.exit(0);
            	break;
        	}
        }
    }
    
    // --> Useful functions <--
    
    public static Board copyBoard (Board boardToCopy)
    {
    	Board copyOfBoard;
    	
    	try {
    	     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	     ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
    	     outputStrm.writeObject(boardToCopy);
    	     ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    	     ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
    	     copyOfBoard = (Board)objInputStream.readObject();
    	     return copyOfBoard;
    	   }
    	   catch (Exception e) {
    	     e.printStackTrace();
    	     return null;
    	   }
    }
    
    public static String getDirectionToMove (String startingCity, String goalCity, ArrayList<citiesWithDistancesObj> distanceMap, Board myBoard)
    {
    	City startingCityObj = myBoard.searchForCity(startingCity);
    	
    	int minDistance = distanceFrom(goalCity, distanceMap);
    	int testDistance = 999;
    	
    	String directionToDrive = null;
    	String testCity = null;
    	
    	for (int i = 0 ; i < startingCityObj.getNeighboursNumber() ; i++)
    	{
    		ArrayList<citiesWithDistancesObj> testDistanceMap = new ArrayList<citiesWithDistancesObj>();
    		testDistanceMap.clear();
    		
    		testCity = startingCityObj.getNeighbour(i);
    		testDistanceMap = buildDistanceMap(myBoard, testCity, testDistanceMap);
    		testDistance = distanceFrom(goalCity, testDistanceMap);
    		
    		if (testDistance < minDistance)
    		{
    			minDistance = testDistance;
    			directionToDrive = testCity;
    		}
    	}
    	return directionToDrive;
    }
    
    
    public static String getMostInfectedInRadius(int radius, ArrayList<citiesWithDistancesObj> distanceMap, Board myBoard)
    {
    	int maxCubes = -1;
    	String mostInfected = null;
    	
    	for (int i = 0 ; i < distanceMap.size() ; i++)
    	{
    		if (distanceMap.get(i).getDistance() <= radius)
    		{
    			City cityToCheck = myBoard.searchForCity(distanceMap.get(i).getName());
    			
    			if (cityToCheck.getMaxCube() > maxCubes)
    			{
    				mostInfected = cityToCheck.getName();
    				maxCubes = cityToCheck.getMaxCube();
    			}
    		}
    	}
    	
    	return mostInfected;
    }
    
    // Count how many card of the color X player X has
    public static int cardsCounterOfColor(Board board, int  playerID, String color)
    {
    	int cardsCounter = 0;
    	
    	for (int i = 0 ; i < board.getHandOf(playerID).size() ; i++)
    		if (board.searchForCity(board.getHandOf(playerID).get(i)).getColour().equals(color))
    			cardsCounter++;
    	
    	return cardsCounter;
    }
    
    public static void printHand(ArrayList<String> handToPrint)
    {
    	for (int i = 0 ; i < handToPrint.size() ; i++)
    		System.out.println(handToPrint.get(i));
    }
    
    public static boolean alredyInDistanceMap(ArrayList<citiesWithDistancesObj> currentMap, String cityName)
    {
    	for (int i = 0 ; i < currentMap.size() ; i++)
    		if (currentMap.get(i).getName().equals(cityName))
    			return true;
    	
    	return false;
    }
    
    public static boolean isInDistanceMap (ArrayList<citiesWithDistancesObj> currentMap, String cityName)
    {
    	for (int i = 0 ; i < currentMap.size() ; i++)
    	{
    		if (currentMap.get(i).getName().equals(cityName))
    			return true;
    	}
    	return false;
    }
    
    public static void printDistanceMap(ArrayList<citiesWithDistancesObj> currentMap)
    {
    	for (int i = 0 ; i < currentMap.size() ; i++)
    		System.out.println("Distance from " + currentMap.get(i).getName() + ": " + currentMap.get(i).getDistance());
    }
    
    public static int distanceFrom(String cityToFind, ArrayList<citiesWithDistancesObj> currentDistanceMap)
    {
    	int result = -1;
    	
    	for (int i = 0 ; i < currentDistanceMap.size() ; i++)
    		if (currentDistanceMap.get(i).getName().equals(cityToFind))
    			result = currentDistanceMap.get(i).getDistance();
    	
    	return result;
    }
    
    public static int numberOfCitiesWithDistance(int distance, ArrayList<citiesWithDistancesObj> currentDistanceMap)
    {
    	int count = 0;
    	
    	for (int i = 0 ; i < currentDistanceMap.size() ; i++)
    		if (currentDistanceMap.get(i).getDistance() == distance)
    			count++;
    	
    	return count;
	}
	
	public static ArrayList<citiesWithDistancesObj> citiesWithDistance(int distance, ArrayList<citiesWithDistancesObj> currentDistanceMap) {
    	ArrayList<citiesWithDistancesObj> cities = new ArrayList<>();
    	
    	for (int i = 0 ; i < currentDistanceMap.size() ; i++)
    		if (currentDistanceMap.get(i).getDistance() == distance)
				cities.add(currentDistanceMap.get(i));
    	
    	return cities;
	}
	
	public static int distanceFromNearestResearchStation(ArrayList<citiesWithDistancesObj> currentDistanceMap) {
		int distance = 0;
		while (citiesWithDistance(distance, currentDistanceMap).size() != 0){
			ArrayList<citiesWithDistancesObj> tempDistanceMap = citiesWithDistance(distance, currentDistanceMap);
			for (int i = 0; i < tempDistanceMap.size(); i++)
				if (tempDistanceMap.get(i).getCityObj().getHasReseachStation())
					break;
			
			distance++;
		}
		return distance;
	}
    
    public static ArrayList<citiesWithDistancesObj> buildDistanceMap(Board myBoard, String currentCityName, ArrayList<citiesWithDistancesObj> currentMap)
    {
    	currentMap.clear();
    	currentMap.add(new citiesWithDistancesObj(currentCityName, myBoard.searchForCity(currentCityName), 0));

    	for (int n = 0 ; n < 15 ; n++)
    	{
        	for (int i = 0 ; i < currentMap.size() ; i++)
        	{
        		if (currentMap.get(i).getDistance() == (n-1))
        		{
        			for (int j = 0 ; j < currentMap.get(i).getCityObj().getNeighboursNumber() ; j++)
        			{
        				String nameOfNeighbor = currentMap.get(i).getCityObj().getNeighbour(j);
        				
        				if (!(alredyInDistanceMap(currentMap, nameOfNeighbor)))
        					currentMap.add(new citiesWithDistancesObj(nameOfNeighbor, myBoard.searchForCity(nameOfNeighbor), n));
        			}
        		}
        	}
    	}
    	
    	return currentMap;
    }
	
	public static int totalNumberOfInfections(Board myBoard){
		int totalCubes = 0;
		for (int j = 0; j < myBoard.getCitiesCount(); j++){
			City city = myBoard.searchForCity(j);
			int cityCubes = city.getBlackCubes() + city.getBlueCubes() + city.getRedCubes() + city.getYellowCubes();
			totalCubes += cityCubes;
		}
		return totalCubes;
	}
	
    // --> Coding functions <--
    
    public static String toTextDriveTo(int playerID, String destination)
    {
    	return "#DT,"+playerID+","+destination;
    }
    	
    public static String toTextDirectFlight(int playerID, String destination)
    {
    	return "#DF,"+playerID+","+destination;
    }
    
    public static String toTextCharterFlight(int playerID, String destination)
    {
    	return "#CF,"+playerID+","+destination;
    }
    
    public static String toTextShuttleFlight(int playerID, String destination)
    {
    	return "#SF,"+playerID+","+destination;
    }
    
    public static String toTextBuildRS(int playerID, String destination)
    {
    	return "#BRS,"+playerID+","+destination;
    }
    
    public static String toTextRemoveRS(int playerID, String destination)
    {
    	return "#RRS,"+playerID+","+destination;
    }
    
    public static String toTextTreatDisease(int playerID, String destination, String color)
    {
    	return "#TD,"+playerID+","+destination+","+color;
    }
    
    public static String toTextCureDisease(int playerID, String color)
    {
    	return "#CD1,"+playerID+","+color;
    }
    
    public static String toTextCureDisease(int playerID, String color, String card1, String card2, String card3, String card4)
    {
    	return "#CD2,"+playerID+","+color+","+card1+","+card2+","+card3+","+card4;
    }
    
    
    public static String toTextActionPass(int playerID)
    {
    	return "#AP,"+playerID;
    }
    
    public static String toTextChatMessage(int playerID, String messageToSend)
    {
    	return "#C,"+playerID+","+messageToSend;
    }
    
    public static String toTextPlayGG(int playerID, String cityToBuild)
    {
    	return "#PGG,"+playerID+","+cityToBuild;
    }
    
    public static String toTextPlayQN(int playerID)
    {
    	return "#PQN,"+playerID;
    }
    public static String toTextPlayA(int playerID, int playerToMove, String cityToMoveTo)
    {
    	return "#PA,"+playerID+","+playerToMove+","+cityToMoveTo;
    }
    public static String toTextPlayF(int playerID)
    {
    	return "#PF,"+playerID;
    }
    public static String toTextPlayRP(int playerID, String cityCardToRemove)
    {
    	return "#PRP,"+playerID+","+cityCardToRemove;
    }
    public static String toTextOpExpTravel(int playerID, String destination, String colorToThrow)
    {
    	return "#OET,"+playerID+","+destination+","+colorToThrow;
    }

	static class MCST {
		/* Tree Subclass */
		class Tree{
			// Class Variables
			Node root;

			public Tree(){
				root = new Node();
			}
		}

		
		/* Node Subclass */
		class Node{
			// Class Variables
			State state;
			Node parent;
			List<Node> childArray;


			public Node(){
				state = new State();
				childArray = new ArrayList<>();
			}
			// Other Methods
			Node getRandomChildNode(){
				Random rand = new Random();
				return childArray.get(rand.nextInt(childArray.size())); /* Needs to be checked */
			}
		}

		/* State Subclass */
		class State{
			// Class Variables
			Board board;
			int playerPlaying;
			int numberOfActions;
			String myAction;
			int visitCount;
			double value;

			public State(){
				board = new Board(numberOfPlayers);
			}

			public Board getBoard() {
				return board;
			}

			public void setBoard(Board board) {
				this.board = board;
			}

			public String getMyAction() {
				return myAction;
			}

			public void setMyAction(String myAction) {
				this.myAction = myAction;
			}

			// Other Methods
			List<State> getAllPossibleStates() {
				// constructs a list of all possible states from current state
				List<State> possibleStates = new ArrayList<>();
				Board myBoard;
				State myState;
				int cards_needed_for_cure;

				String myCurrentCity = board.getPawnsLocations(playerPlaying);
				City myCurrentCityObj = board.searchForCity(myCurrentCity);
				int[] myColorCount = {0, 0, 0, 0};
				for (int i = 0 ; i < 4 ; i++) myColorCount[i] =  cardsCounterOfColor(board, playerPlaying, board.getColors(i));
				int childsnumberOfActions = numberOfActions;
				int childsPlayerPlaying = playerPlaying;
				if(numberOfActions == 4) {
					childsnumberOfActions = 0;
					// changedPlayer = true;
					if (playerPlaying == numberOfPlayers - 1) 
						childsPlayerPlaying = 0;
					else 
						childsPlayerPlaying++;
				}
				else {
					childsnumberOfActions++;
				}

				// Discover a Cure
				cards_needed_for_cure = (board.getRoleOf(playerPlaying).equals("Scientist")) ? 3 : 4;
				String colorToCure = null;
			
				for (int i = 0; i < myColorCount.length; i++){
					if (myColorCount[i] > cards_needed_for_cure){
						if (i == 0) colorToCure = "Black";
						else if (i == 1) colorToCure = "Yellow";
						else if (i == 2) colorToCure = "Blue";
						else if (i == 3) colorToCure = "Red";
						myBoard = copyBoard(board);
						myBoard.cureDisease(childsPlayerPlaying, colorToCure);
						myState = new State();
						myState.board = myBoard;
						myState.myAction =  toTextCureDisease(childsPlayerPlaying, colorToCure);
						myState.numberOfActions = childsnumberOfActions;
						myState.playerPlaying = childsPlayerPlaying;
						possibleStates.add(myState);
					}
				}

				// Treat disease
				String colorToTreat = null;
				if (myCurrentCityObj.getBlackCubes() > 0){
					colorToTreat = "Black";
					myBoard = copyBoard(board);
					myBoard.treatDisease(childsPlayerPlaying, myCurrentCity, colorToTreat);
					myState = new State();
					myState.board = myBoard;
					myState.myAction =  toTextTreatDisease(childsPlayerPlaying, myCurrentCity, colorToTreat);
					myState.numberOfActions = childsnumberOfActions;
					myState.playerPlaying = childsPlayerPlaying;
					possibleStates.add(myState);
				}

				if ( myCurrentCityObj.getYellowCubes() > 0){
					colorToTreat = "Yellow";
					myBoard = copyBoard(board);
					myBoard.treatDisease(childsPlayerPlaying, myCurrentCity, colorToTreat);
					myState = new State();
					myState.board = myBoard;
					myState.myAction = toTextTreatDisease(childsPlayerPlaying, myCurrentCity, colorToTreat);
					myState.numberOfActions = childsnumberOfActions;
					myState.playerPlaying = childsPlayerPlaying;
					possibleStates.add(myState);
				}

				if (myCurrentCityObj.getBlueCubes() > 0){
					colorToTreat = "Blue";
					myBoard = copyBoard(board);
					myBoard.treatDisease(childsPlayerPlaying, myCurrentCity, colorToTreat);
					myState = new State();
					myState.board = myBoard;
					myState.myAction = toTextTreatDisease(childsPlayerPlaying, myCurrentCity, colorToTreat);
					myState.numberOfActions = childsnumberOfActions;
					myState.playerPlaying = childsPlayerPlaying;
					possibleStates.add(myState);
				}

				if (myCurrentCityObj.getRedCubes() > 0){
					colorToTreat = "Red";
					myBoard = copyBoard(board);
					myBoard.treatDisease(childsPlayerPlaying, myCurrentCity, colorToTreat);
					myState = new State();
					myState.board = myBoard;
					myState.myAction = toTextTreatDisease(childsPlayerPlaying, myCurrentCity, colorToTreat);
					myState.numberOfActions = childsnumberOfActions;
					myState.playerPlaying = childsPlayerPlaying;
					possibleStates.add(myState);
				}

				// Drive Ferry
				for (int i = 0; i < myCurrentCityObj.getNeighboursNumber(); i++){
					String neighboor = myCurrentCityObj.getNeighbour(i);
					myBoard = copyBoard(board);
					myBoard.driveTo(childsPlayerPlaying, neighboor);
					myState = new State();
					myState.board = myBoard;
					myState.myAction = toTextDriveTo(childsPlayerPlaying, neighboor);
					myState.numberOfActions = childsnumberOfActions;
					myState.playerPlaying = childsPlayerPlaying;
					possibleStates.add(myState);
				}

				// Direct Flight
				ArrayList<String> cards = board.getHandOf(playerPlaying);
				for (int i = 0; i < cards.size(); i++){
					myBoard = copyBoard(board);
					myBoard.directFlight(childsPlayerPlaying, cards.get(i));
					myState = new State();
					myState.board = myBoard;
					myState.myAction = toTextDirectFlight(childsPlayerPlaying, cards.get(i));
					myState.numberOfActions = childsnumberOfActions;
					myState.playerPlaying = childsPlayerPlaying;
					possibleStates.add(myState);
				}

				// Charter Flight
				String location = "";
				for (int i = 0; i < cards.size(); i++){
					if (myCurrentCity.equals(cards.get(i))){
						for (int j = 0; j < board.getCitiesCount(); j++){
							location = board.searchForCity(j).getName();
							if (location.equals(myCurrentCity)) continue;
							myBoard = copyBoard(board);
							myBoard.charterFlight(childsPlayerPlaying, location);
							myState = new State();
							myState.board = myBoard;
							myState.myAction = toTextCharterFlight(childsPlayerPlaying, location);
							myState.numberOfActions = childsnumberOfActions;
							myState.playerPlaying = childsPlayerPlaying;
							possibleStates.add(myState);
						}
					}
				}

				// Shuttle Flight
				if (myCurrentCityObj.getHasReseachStation()){
					ArrayList<String> locations = board.getRSLocations();
					for (int i = 0; i < locations.size(); i++){
						if(myCurrentCity.equals(locations.get(i)))
							continue;
						myBoard = copyBoard(board);
						myBoard.shuttleFlight(childsPlayerPlaying, locations.get(i));
						myState = new State();
						myState.board = myBoard;
						myState.myAction = toTextShuttleFlight(childsPlayerPlaying, locations.get(i));
						myState.numberOfActions = childsnumberOfActions;
						myState.playerPlaying = childsPlayerPlaying;
						possibleStates.add(myState);
					}   
				}

				// Build a Research Station
				for (int i = 0; i < cards.size(); i++){
					if (myCurrentCity.equals(cards.get(i))){
						myBoard = copyBoard(board);
						myBoard.buildRS(childsPlayerPlaying, myCurrentCity);
						myState = new State();
						myState.board = myBoard;
						myState.myAction = toTextBuildRS(childsPlayerPlaying, myCurrentCity);
						myState.numberOfActions = childsnumberOfActions;
						myState.playerPlaying = childsPlayerPlaying;
						possibleStates.add(myState);
					}
				}
				return possibleStates;
			}
			
			public double heuristic(){
				double weighted_hdsurv = 0.5 * hdsurv();
				double weighted_hdcure =  0.5 * hdcure();
				double weighted_hcards = 1 * hcards();
				double weighted_hdisc = 0.5 * hdisc();
				double weighted_hinf = 0.6 * hinf();
				double weighted_hdist = 0.6 * hdist();
				double weighted_hcures = 24 * hcures();
				double weighted_htotal =  weighted_hdsurv + weighted_hdcure + weighted_hcards + weighted_hdisc + weighted_hinf + weighted_hdist + weighted_hcures;
				return weighted_htotal;	
			}
			
			// public double heuristic(){
			// 	return 0.5 * hdsurv() + 0.5 * hdcure() + 1 * hcards() * 0.5 * hdisc() + 0.6 * hinf() + 0.6 * hdist() + 24 * hcures();
			// }
	
			/**
			 *  1.  Measures the distance to all the cities times the number of disease cubes (infection) present
			 * 		in them, these are then averaged over the total number of disease cubes in the game.
			 */
			public double hdsurv(){
				int sumSurv = 0;
				for (int i = 0 ; i < numberOfPlayers; i++){
					ArrayList<citiesWithDistancesObj> currentMap = new ArrayList<>();
					currentMap = buildDistanceMap(board, board.getPawnsLocations(i), currentMap);
					int sumCubesInCity, sumWeightedInf = 0, sumInf = 0;
					City city;
					for (int j = 0; j < currentMap.size(); j++){
						city = currentMap.get(j).getCityObj();
						sumCubesInCity = city.getBlackCubes() + city.getBlueCubes() + city.getRedCubes() + city.getYellowCubes();
						sumWeightedInf += currentMap.get(j).getDistance() * sumCubesInCity;
						sumInf += sumCubesInCity;
					}
					sumSurv += sumWeightedInf / sumInf;
				}
				return sumSurv;
			}
	
			/**
			 *  2. 	Calculates the distance to the closest city with a research station in it.
			 */ 
			public double hdcure(){
				int allDistances = 0;
				for(int i = 0; i < numberOfPlayers; i++){
					ArrayList<citiesWithDistancesObj> currentMap = new ArrayList<>();
					currentMap = buildDistanceMap(board, board.getPawnsLocations(i), currentMap);
					allDistances += distanceFromNearestResearchStation(currentMap);
				}
				return allDistances;
			}
			
			/**
			 *  3. 	The value of the players’ hands is calculated, as the minimum number of cards 
			 * 		missing to discover a cure for each disease color among the players’ hands 
			 * 		(R being 4 for the Scientist and 5 for every other player).
			 */
			public double hcards(){ 
				int colorSize = 4;
				int sumCards = 0;
				for(int i = 0; i < colorSize; i++){
					int active = board.getCured(i) ? 0 : 1 ; // active cures
					
					ArrayList<String> cards;
					for (int j = 0; j < numberOfPlayers; j++){
						cards = board.getHandOf(j);
						int count = 0; 
						for(int k = 0; k < cards.size(); k++){
							if (board.getColors(i).equals(board.searchForCity(cards.get(k)).getColour())) // Count matching color cards
								count++;
						}
						int cardsNeededForCure;
						if(board.getRoleOf(j) == "Scientist")
							cardsNeededForCure = board.getCardsNeededForCure() - 1;
						else
							cardsNeededForCure = board.getCardsNeededForCure();
						sumCards += active * (cardsNeededForCure - count);
					}
				}
				return sumCards;
			}	
			
			/** 
			 *  4.	The value of the cards in the discard is calculated, as the sum of the number 
			 *		of discarded cards for each of the active diseases still missing a cure.
			 */
			public double hdisc(){ //NEEDS CHECKING
				// Simpler Approach
				int discardedPlayerCards = board.getCitiesCount() + board.getNumberOfEpidemicCards() - board.getPlayersDeck().size();
				return discardedPlayerCards;

				// Alternative Implementation ()
				/* int uncuredDeseases = 0;
				board.getDiscardedPile()
				for (int i = 0; i < 4; i++){
					if (!board.getCured(i))
						uncuredDeseases++;
				}
				return discardedPlayerCards * uncuredDeseases; */
			}
	
			/**
			 *  5. 	Calculates the total number of infections.
			 */	
			public double hinf(){
				return totalNumberOfInfections(board);
			}
	
			/**
			 *  6. 	Calculates the average distance required to move from each city to another city
			 * 		with a linearly decreasing value associated with the number of turns remaining 
			 * 		(taken from the amount of cards still remaining in the player deck).
			 */
			public double hdist(){
				float sum = 0;
				for(int i = 0; i < board.getCitiesCount(); i++){
					ArrayList<citiesWithDistancesObj> currentMap = new ArrayList<>();
					currentMap = buildDistanceMap(board, board.searchForCity(i).getName(), currentMap);
					for(int j = 0; j < currentMap.size(); j++){
						sum += (float) (currentMap.get(j).getDistance() * board.getPlayersDeck().size()) / (float) (board.getCitiesCount() * (board.getCitiesCount()-1) * (board.getCitiesCount() + board.getNumberOfEpidemicCards()));
					}
				}
				return (double)sum;
			}
			
			/**
			 *  7. 	Counts the number of active diseases which are still lacking a cure.
			 */
			public double hcures(){
				int uncuredDeseases = 0;
				for (int i = 0; i < 4; i++){
					if (!board.getCured(i))
						uncuredDeseases++;
				}
				return uncuredDeseases;
			}

		}

		// Class Variables
		int numberOfPlayers;
		
		// Constructor
		public MCST(Board board){
			/* Calculate Number of Players */
			numberOfPlayers = 0;
			while (numberOfPlayers < 4){
				if (board.getUsernames(numberOfPlayers).equals("")) 
					break;
				numberOfPlayers++;
			}
		}
		
		// Other Methods
		State findNextMove(Board board, int numOfActions) {
			System.out.println("FIND NEXT MOVE STARTED.");
			Tree tree = new Tree();
			tree.root.state.board = board;
			tree.root.state.playerPlaying = board.getWhoIsPlaying();
			tree.root.state.numberOfActions = numOfActions;

			// Time-based termination loop
			long currentTime = System.currentTimeMillis();
			long endTime = currentTime + 3000;
			//while(System.currentTimeMillis() < endTime){
			for(int j =0 ;j < 10; j++){
				/* Phase 1 - Selection */
				Node selectedNode = selectNode(tree.root);
	
				/* Phase 2 - Expansion */
				expandNode(selectedNode);
	
				/* Phase 3 - Simulation */
				Node nodeToExplore = selectedNode;
				if (selectedNode.childArray.size() > 0) 
					nodeToExplore = selectedNode.getRandomChildNode();
				
				double playoutResult = simulateRandomPlayout(nodeToExplore);
	
				/* Phase 4 - Update */
				backPropogation(nodeToExplore, playoutResult);
			}
			/* Select final move */
			double maxValue = Double.MIN_VALUE;
			State maxState = tree.root.state;
				/* Returns max value */
			for (int i = 0; i < tree.root.childArray.size(); i++){
				State tempState = tree.root.childArray.get(i).state;
				if (tempState.value > maxValue){
					maxState = tempState;
					maxValue = tempState.value;
				}
			}
			return maxState;
		}

		Node selectNode(Node rootNode) {
			Node node = rootNode;
			while (node.childArray.size() != 0) node = findBestNodeWithUCB(node);
			return node;
		}

		Node findBestNodeWithUCB(Node node) {
			int parentVisits = node.state.visitCount;
			double[] ucbValues = new double[node.childArray.size()];
			double maxUcbValue = Integer.MIN_VALUE;
			int maxUcbIdx = 0;
			for (int i = 0; i < node.childArray.size(); i++) {
				ucbValues[i] = ucbValue(parentVisits, node.childArray.get(i).state.value, node.childArray.get(i).state.visitCount);
				maxUcbValue = Math.max(maxUcbValue, ucbValues[i]);
				maxUcbIdx = i;
			}
			return node.childArray.get(maxUcbIdx);
		}

		double ucbValue(int parentVisits, double value, int childVisits) {
			if (childVisits == 0) return Integer.MAX_VALUE;
			return (( 1 / (value * (double) childVisits)) + 2 * Math.sqrt(Math.log(parentVisits) / (double) childVisits));
		}

		void expandNode(Node node) {
			// Create all child states
			List<State> possibleStates = node.state.getAllPossibleStates();
			// Create node for each state and make connection with parent
			for (int i = 0; i < possibleStates.size(); i++){
				State state = possibleStates.get(i);
				Node newNode = new Node();
				newNode.state = state;
				newNode.parent = node;
				node.childArray.add(newNode);
	
				// if (myBoard.checkIfWon()) break;

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

		void backPropogation(Node nodeToExplore, double playoutResult) {
			Node tempNode = nodeToExplore;
			while (tempNode != null) {
				tempNode.state.visitCount++;
				tempNode.state.value += playoutResult;
				tempNode = tempNode.parent;
			}
		}

		double simulateRandomPlayout(Node node) {
			Node tempNode = node;
			State tempState = tempNode.state;
			int rolloutNum = 100, totalHeur = 0;
			/* Final State */
			if (tempState.board.getGameEnded()) {
				/* Reward Win */
				if (tempState.board.checkIfWon())
					return Integer.MAX_VALUE;
				/* Punish Defeat */
				else
					return Integer.MIN_VALUE;
			}
			for(int i = 0; i < rolloutNum; i++){
				boolean changedPlayer = false;
				State newState = tempState;
				int firstPlayer = tempState.playerPlaying;
				double tempHeur = 0;

				while (!(newState.playerPlaying == tempState.playerPlaying && changedPlayer)) {
					List<State> possibleStates = newState.getAllPossibleStates();
					Random rand = new Random();
					newState = possibleStates.get(rand.nextInt(possibleStates.size()));
					
					// Node newNode = new Node();
					// newNode.state = newState;
					Board myBoard = newState.board;
					
					if (myBoard.checkIfWon()) break;

					if(newState.playerPlaying != firstPlayer) {
						changedPlayer = true;
					}
					// if (myBoard.getWhoIsPlaying() == numberOfPlayers - 1) 
					// 	// myBoard.setWhoIsPlaying(0);
					// 	newState.playerPlaying = 0;
					// 	 // Back to first player
					// else 
					// 	newState.playerPlaying++;
					// 	// myBoard.setWhoIsPlaying(myBoard.getWhoIsPlaying() + 1);
				}
				tempHeur = newState.heuristic();
				totalHeur += tempHeur;
			}
			return totalHeur / rolloutNum;
		}
	}
}