package ca.sheridancollege.project;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * The class that initiates the game and declares the winner.
 * Inherits from the base Game class and implements the game logic for UNO.
 *
 * @author Quang Dung Le April 2025
 */
public class UNOGame extends Game {

    private UNOManager gameManager;
    private Scanner scanner;

    /**
     * Constructor for the UNOGame shell.
     * 
     * @param name
     * @param scanner
     */
    public UNOGame(String name, Scanner scanner) {
        super(name);
        this.scanner = scanner;
        this.gameManager = new UNOCardManager(scanner, getPlayers());
    }

    @Override
    public void play() {
        if (getPlayers() == null || getPlayers().size() < 2) {
            System.out.println("Error: Not enough players to start the game.");
            return;
        }
        // Ensure the manager has the current player list before starting
        gameManager.updatePlayerList(getPlayers());
        gameManager.runGame();
        declareWinner();
    }

    /**
     * Declares the winner based on the final state provided by the game manager.
     */
    @Override
    public void declareWinner() {
        String winnerName = gameManager.getWinnerName();
        boolean abnormalEnd = gameManager.didGameEndAbnormally();

        System.out.println("\n===================================");
        if (winnerName != null) {
            // Assuming a normal win
            System.out.println(winnerName + " has played their last card!");
            System.out.println("CONGRATULATIONS! " + winnerName + " is the WINNER!");
        } else if (abnormalEnd) {
            System.out.println("The game has ended due to an unresolvable state."); // Encountered an error
        } else {
            System.out.println("The game has ended.");
        }
        System.out.println("===================================");
    }

    @Override
    public void setPlayers(ArrayList<Player> players) {
        super.setPlayers(players);
        if (this.gameManager != null)
            this.gameManager.updatePlayerList(players);
        else
            this.gameManager = new UNOCardManager(this.scanner, players);
    }

    /**
     * Gets the current top card of the discard pile
     * 
     * @return The top UNOCard.
     */
    public UNOCard getTopCard() {
        return gameManager.getTopCard();
    }

    /**
     * Gets the currently active chosen color
     * 
     * @return The active UNOCard.Color or null.
     */
    public UNOCard.Color getCurrentColor() {
        return gameManager.getCurrentColor();
    }

    /**
     * Checks if the game has finished
     * 
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameWon() {
        return gameManager.isGameWon();
    }

    /**
     * Requests the manager to draw cards for a specific player.
     * 
     * @param player
     * @param count
     * @param showMessages
     */
    public void drawCardsForPlayer(UNOPlayer player, int count, boolean showMessages) {
        gameManager.drawCardsForPlayer(player, count, showMessages);
    }

    /**
     * Requests the manager to add a card to the central discard pile.
     * 
     * @param card The card played by a player.
     */
    public void addToDiscardPile(UNOCard card) {
        gameManager.addToDiscardPile(card);
    }

}