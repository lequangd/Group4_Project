package ca.sheridancollege.project;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Concrete implementation of UNOManager handling setup rules
 *
 * @author Quang Dung Le April 2025
 */
public class UNOCardManager extends UNOManager {
    public UNOCardManager(Scanner scanner, ArrayList<Player> playerList) {
        super(scanner, playerList);
    }

    /**
     * Implements the abstract method to apply card effects
     * 
     * @param cardPlayed
     * @param playerWhoPlayed
     * @return true if the effect causes a skip, false otherwise.
     */
    @Override
    protected boolean applyCardEffect(UNOCard cardPlayed, UNOPlayer playerWhoPlayed) {
        return CardEffectHandler.applyEffect(cardPlayed, this, playerWhoPlayed, scanner);
    }

    /**
     * Implements the abstract method to handle the first card's effect
     * 
     * @param card The first card placed on the discard pile.
     */
    @Override
    protected void handleFirstCardEffect(UNOCard card) {
        System.out.println("Handling effect of first card: " + card);
        // Ensure players list isn't empty before accessing index 0
        if (this.players == null || this.players.isEmpty()) {
            System.out.println("Error: No players available for first card effect.");
            gameWon = true; // Game cannot proceed
            abnormalEnd = true;
            return;
        }
        UNOPlayer firstPlayer = (UNOPlayer) this.players.get(0);
        boolean initialSkip = false;

        switch (card.getType()) {
            case SKIP:
                System.out.println("First card is Skip. Player 1 (" + firstPlayer.getName() + ") is skipped.");
                initialSkip = true; // Signal skip
                break;
            case REVERSE:
                System.out.println("First card is Reverse. Direction reversed.");
                setIsReversed(true);
                // Player to dealer's left (Player N-1 if 0 is dealer) starts
                this.currentPlayerIndex = (0 - 1 + getPlayerCount()) % getPlayerCount();
                System.out.println("Player " + players.get(currentPlayerIndex).getName() + " starts.");
                break;
            case DRAW_TWO:
                System.out.println("First card is Draw Two. Player 1 (" + firstPlayer.getName() + ") draws 2 and is skipped.");
                drawCardsForPlayer(firstPlayer, 2, true);
                initialSkip = true; // Signal skip
                break;
            case WILD:
                System.out.println("First card is Wild. Player 1 (" + firstPlayer.getName() + ") chooses the starting color.");
                UNOCard.Color chosen = CardEffectHandler.chooseColor(scanner);
                setCurrentColor(chosen);
                System.out.println("Color set to " + chosen + ".");
                break;
            case WILD_DRAW_FOUR:
                System.out.println("Error: WD4 effect triggered on first card. This shouldn't happen.");
                gameWon = true;
                abnormalEnd = true;
                break;
            default: // NUMBER
                System.out.println(firstPlayer.getName() + "'s turn.");
                break;
        }
        // Perform the skip after the effect if needed
        if (initialSkip && !gameWon) advanceTurn();
    }
}