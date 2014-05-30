/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import java.util.ArrayList;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		playGame();
	}

	private void playGame() {
		initGame();
		for (int round = 0; round < N_SCORING_CATEGORIES; round++) {  //play rounds until all the scoring categories are filled
			playRound();
		}
		computeScoresAndWinner();	
	}

	private void initGame() {
		IODialog dialog = getDialog();
		
		nPlayers = dialog.readInt("Enter number of players"); //# of players, put into global variable "nPlayers"
		
		playerNames = new String[nPlayers]; //initialize array named playerNames w [nPlayers value] elements of type String
		//didn't have "String[]" before, is that okay?
		
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i); //ask for player names for # of players entered and put into 0th element, 1st element..etc
		}
		
		display = new YahtzeeDisplay(getGCanvas(), playerNames); //make board w names and call it "display"
		
		categoriesUsed = new boolean[N_CATEGORIES][nPlayers]; //create "categoriesUsed" 2D array (outside of playRound)
		scoreArray = new int [N_CATEGORIES][nPlayers]; //create "scoreArray" 2D array (outside of playRound)
	}
	
	private void playRound() {
		for (int player = 1; player <= nPlayers; player++) {   //since player starts at 1, don't need to - 1 from nPlayers 
			playTurn(player);  //each player gets to play a turn and that's a round
		}
	}

	private void playTurn(int player) {
		display.printMessage(playerNames[player - 1] + "'s turn. Click 'Roll Dice' button to roll the dice.");
		
		display.waitForPlayerToClickRoll(player);
		
		int[] diceArray = new int[N_DICE];  //make dice array. N_DICE is total number of dice spec'ed in YahtzeeConstants
		
		for (int i = 0; i < N_DICE; i++) { //generate random values for N_DICE and display
			diceArray[i] = rgen.nextInt(1, 6);
		}
		display.displayDice(diceArray);
		
	    for (int i = 0; i< 2; i++) { //re-roll loop
	    	display.printMessage("Select the dice you want to re-roll and click 'Roll Again' to re-roll the unwanted dice");
	    	display.waitForPlayerToSelectDice(); //turns on the "Roll Again button"
	    	for (int j = 0; j < N_DICE; j++) {  //looping through each dice in N_DICE
	    		if (display.isDieSelected(j)) {  //if dice is selected
	    			diceArray[j]= rgen.nextInt(1, 6); //random gen between 1 and 6 for it
	    		}
	    	}
	        display.displayDice(diceArray);
	    }
	
	    display.printMessage("Select a category.");  //category selection logic
		int category = 0; 
		while (true) { 
			category = display.waitForPlayerToSelectCategory();  //category value is the result of this method
			if (!categoriesUsed[category-1][player-1]) {  //if category hasn't been used yet, then good - break out of while loop
				break;
			}
			display.printMessage("You cannot choose this category."); //else category has been used
		} 
		categoriesUsed[category - 1][player - 1] = true; //for that cat and player, category is now true/used
		
		
		//int score = 0; 
		//if (isCategoryValid(diceArray, category)) {
		//	score = tallyScore(diceArray, category);  //calculate score
		//}
		//can be written as below
		//int score = isCategoryValid(diceArray, category) ? tallyScore(diceArray, category) : 0;
		//can be writen as below where score is now just what value is in that scoreArray cat for that player
		
		//If categoryIsValid for scoreArray[cat-1][player-1], tallyScore/put score in, else scoreArray = 0 	
		scoreArray[category - 1][player - 1] = isCategoryValid(diceArray, category) ? tallyScore(diceArray, category) : 0;  	
		display.updateScorecard(category, player, scoreArray[category - 1][player - 1]);  //provided method that puts score in correct category on screen
		
		//wait, computed totalScore is no longer showing at the end of the player's turn
		int totalScore = 0;
		for (int i=0; i < scoreArray.length; i++) {
			totalScore += scoreArray[i][player - 1];
			}
		display.updateScorecard(TOTAL, player, totalScore);
	} //playTurn is done!
	
	
	private void computeScoresAndWinner() {
		int highScore = 0;
		ArrayList<Integer> bestPlayers = new ArrayList<Integer>();
		
		for (int player = 1; player <= nPlayers; player++) {  //loop to compute bonuses and grand total score for each player
			int playerScore = tallyUpperScore(player) + tallyUpperBonus(player) + tallyLowerScore(player);
			display.updateScorecard(TOTAL, player, playerScore);
			
			if (playerScore > highScore) {  //determine player with highest score
				highScore = playerScore;
				bestPlayers.clear();
				
			}
			if (playerScore >= highScore) {
				bestPlayers.add(player);
			}
			
		}
		if (bestPlayers.size() == 1) {
			display.printMessage(playerNames[bestPlayers.get(0) - 1] + " wins!");
		} else {
			String tiers = "";
			for (int i = 0; i < bestPlayers.size(); i++) {
				tiers += playerNames[bestPlayers.get(i) - 1];
				if (i < bestPlayers.size() - 1) {
					tiers += " and ";
				}
			}
			display.printMessage(tiers + " tied!"); 
		}
	}
	
	
/* Methods */
	
	private int[] makeHistogram(int [] dice) {
		int[] histogram = new int[6]; //create new array and set the number of elements/buckets in it
		for (int i = 0; i < dice.length; i++)
			histogram[dice[i] - 1]++;
		return histogram;
	}
	
	private boolean isCategoryValid(int [] dice, int category) {
		int [] histogram = makeHistogram(dice);
		switch (category) {
			case ONES:
			case TWOS:
			case THREES:
			case FOURS:
			case FIVES:
			case SIXES: return true;
			case THREE_OF_A_KIND: return containsNOfAKind(histogram, 3);
			case FOUR_OF_A_KIND: return containsNOfAKind(histogram, 4);
			case FULL_HOUSE: return containsFullHouse(histogram);
			case SMALL_STRAIGHT: return containsStraight(histogram, 4); 
			case LARGE_STRAIGHT: return containsStraight(histogram, 5);
			case YAHTZEE: return containsNOfAKind(histogram, 5);
			case CHANCE: return true;
			default: return false;
		}
	}
	
	private boolean containsNOfAKind(int [] histogram, int n) { 
		for (int i = 0; i < histogram.length; i++) {
			if (histogram[i] >= n) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsFullHouse(int [] histogram) {
		boolean ifFoundTwo = false;
		boolean ifFoundThree = false;
		for (int i = 0; i < histogram.length; i++) {
			if (histogram[i] == 2) {
				ifFoundTwo = true;
			}
			if (histogram[i] == 3) {
				ifFoundThree = true;
			}
		}
		return ifFoundTwo && ifFoundThree;
	}
	
	private boolean containsStraight(int [] histogram, int n) {    //n is the size of the straight
		for (int i = 0; i <= histogram.length - n; i++) {        //looping by segment
			boolean runIsGood = true;
			for (int j = 0; j < n; j++) {  //looping IN the segment
				if (histogram[i + j] == 0) {
					runIsGood = false;
					break;
				} 
			}
			if (runIsGood) 
				return true;
		}
		return false;
	}	

	private int tallyScore(int [] dice, int category) {
		switch (category) {
			case ONES: return sumOfDiceValue(dice, 1);
			case TWOS: return sumOfDiceValue(dice, 2); 
			case THREES: return sumOfDiceValue(dice, 3);
			case FOURS: return sumOfDiceValue(dice, 4); 
			case FIVES: return sumOfDiceValue(dice, 5); 
			case SIXES: return sumOfDiceValue(dice, 6); 
			case THREE_OF_A_KIND: return sumScore(dice);
			case FOUR_OF_A_KIND: return sumScore(dice);
			case FULL_HOUSE: return 25;
			case SMALL_STRAIGHT: return 30;
			case LARGE_STRAIGHT: return 40;
			case YAHTZEE: return 50;
			case CHANCE: return sumScore(dice);
			default: return -1;
		}
	}
	
	private int sumOfDiceValue(int [] dice, int n) {	
		int sum = 0;
		for (int i = 0; i < dice.length; i++) {
			if (dice[i] == n) {
				sum += n;
			}	
		}
		return sum;
	}
	
	private int sumScore(int [] dice) {	
		int sum = 0;
		for (int i = 0; i < dice.length; i++) {
			sum += dice[i];
		}
		return sum;
	}

	private int calculateTotalScore(int player) {    
		int totalScore = 0; 
		for (int i = 0; i < scoreArray.length; i++) {
			totalScore += scoreArray[i][player - 1];  //add up all the cats in scoreArray
		}
		int[] nonScoringCategories = {UPPER_SCORE, LOWER_SCORE, TOTAL};  //subtract out nonScoring cats
		for (int i = 0; i < nonScoringCategories.length; i++) {
			totalScore -= scoreArray[nonScoringCategories[i]][player - 1];
		}
		
//		totalScore -= scoreArray[UPPER_SCORE - 1][player - 1];
//		totalScore -= scoreArray[LOWER_SCORE - 1][player - 1];
//		totalScore -= scoreArray[TOTAL - 1][player - 1];
		
		//totalScore = scoreArray[TOTAL - 1][player - 1]; 
		display.updateScorecard(TOTAL, player, totalScore); //display total score
		return totalScore;
	}
		
	private int tallyUpperScore(int player) {		
		int upperScore = 0;
		for (int i = ONES; i <= SIXES; i++) {
			upperScore += scoreArray[i - 1][player - 1];
		}	
		scoreArray[UPPER_SCORE - 1][player - 1] = upperScore; 
		display.updateScorecard(UPPER_SCORE, player, upperScore);
		return upperScore;
	}
	
	private int tallyUpperBonus(int player) {
		int upperScore = scoreArray[UPPER_SCORE - 1][player - 1];
		int upperBonus = upperScore >= 63 ? 35 : 0;
		scoreArray[UPPER_BONUS - 1][player - 1] = upperBonus; 
		display.updateScorecard(UPPER_BONUS, player, upperBonus);
		return upperBonus;
	}
	
	private int tallyLowerScore(int player) {
		int lowerScore = 0;
		for (int i = THREE_OF_A_KIND; i <= CHANCE; i++) {
			lowerScore += scoreArray[i - 1][player - 1];
		}
		scoreArray[LOWER_SCORE - 1][player - 1] = lowerScore; 
		display.updateScorecard(LOWER_SCORE, player, lowerScore);
		return lowerScore;
	}

	
/* Private instance variables */
	private int nPlayers; //variable to store number of players entered
	private String[] playerNames; //array to store player's names
	private YahtzeeDisplay display; 
	private RandomGenerator rgen = new RandomGenerator();
	private boolean[][] categoriesUsed;
	private int[][] scoreArray; //2D score array. 18 elements in scoreArray.

}
