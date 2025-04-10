package ca.sheridancollege.project;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * The class that handles the flow and information of the player
 *
 * @author Quang Dung Le April 2025
 */

public class UNOPlayer extends Player {
    private ArrayList<UNOCard> hand;
    private UNOGame game;
    private Scanner scanner;
    private boolean calledUno;

    public UNOPlayer(String name, UNOGame game, Scanner scanner) {
        super(name);
        this.hand = new ArrayList<>();
        this.game = game;
        this.scanner = scanner;
        this.calledUno = false;
    }

    /**
     * Handles the logic for a player's turn: display hand, choose action
     * (play/draw),
     * execute action. Called by the UNOGame loop.
     * 
     * @return The card that was played, or null if the player drew or turn ended
     *         otherwise.
     */
    @Override
    public UNOCard play() {
        // Check if game ended before turn starts
        if (game.isGameWon())
            return null;

        // displayHand(); // Show hand at start of turn
        UNOCard topCard = game.getTopCard();
        if (topCard == null) {
            System.out.println("Error: Discard pile empty during player's turn. Cannot play.");
            return null; // Setup failed
        }

        // Find playable cards
        ArrayList<UNOCard> playableCards = findPlayableCards(topCard);
        UNOCard cardPlayed = null; // Track the card played this turn

        if (playableCards.isEmpty()) {
            System.out.println("You have no playable cards. You must draw a card.");
            game.drawCardsForPlayer(this, 1, true); // Draw one card

            // Check if drawn card is playable
            if (!hand.isEmpty()) {
                UNOCard drawnCard = hand.get(hand.size() - 1);
                if (isCardPlayable(drawnCard, topCard)) {
                    System.out.print("The drawn card (" + drawnCard + ") is playable! Play it now? (yes/no): ");
                    String playChoice = "";
                    while (!playChoice.equals("yes") && !playChoice.equals("no")) {
                        playChoice = scanner.nextLine().trim().toLowerCase();
                        if (!playChoice.equals("yes") && !playChoice.equals("no")) {
                            System.out.print("   Please enter 'yes' or 'no': ");
                        }
                    }
                    if (playChoice.equals("yes")) {
                        cardPlayed = playChosenCard(drawnCard); // Play the drawn card
                    } else {
                        System.out.println("You chose not to play the drawn card. Turn ends.");
                    }
                } else {
                    System.out.println("The drawn card is not playable. Turn ends.");
                }
            }

        } else {
            System.out.println("Playable cards are marked with '*'.");
            displayHand(playableCards); // Show hand again, highlighting playable

            int choice = -1;
            boolean validInput = false;
            while (!validInput) {
                System.out.print("Choose a card number to play, or enter 0 to draw: ");
                try {
                    choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 0) {
                        // Player chooses to draw even with playable cards
                        System.out.println("You chose to draw a card.");
                        game.drawCardsForPlayer(this, 1, true);
                        System.out.println("Turn ends after choosing to draw.");
                        validInput = true; // Exit loop, no card played

                    } else if (choice > 0 && choice <= hand.size()) {
                        UNOCard chosenCard = hand.get(choice - 1);
                        if (playableCards.contains(chosenCard)) { // Check if chosen card is valid
                            cardPlayed = playChosenCard(chosenCard); // Play the card
                            validInput = true; // Exit loop, card played
                        } else {
                            System.out.println("Invalid choice. Try again.");
                        }
                    } else {
                        System.out
                                .println("Invalid input. Enter a number from your hand (1-" + hand.size() + ") or 0.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input: Please enter a number.");
                    if (scanner.hasNextLine())
                        scanner.nextLine();
                }
            }
        }

        // Check UNO status AFTER action, only if a card was successfully played
        if (cardPlayed != null) {
            if (hand.size() == 1) {
                System.out.print("Do you want to call UNO? (yes/no): ");
                String choice = "";
                while (!choice.equals("yes") && !choice.equals("no")) {
                    choice = scanner.nextLine().trim().toLowerCase();
                    if (!choice.equals("yes") && !choice.equals("no"))
                        System.out.print("   Please enter 'yes' or 'no': ");
                }
                if (choice.equals("yes")) {
                    System.out.println("UNO! " + getName() + " has one card left!");
                    this.calledUno = true;
                } else {
                    System.out.println("You chose not to call UNO.");
                    this.calledUno = false;
                }
            }
        }

        return cardPlayed;
    }

    /**
     * Gets the player's current hand of cards.
     * 
     * @return ArrayList of UNOCards in the player's hand.
     */
    public ArrayList<UNOCard> getHand() {
        return hand;
    }

    /**
     * Adds a card to the player's hand. Usually called when drawing.
     * Also resets the hasCalledUno flag.
     * 
     * @param card The card to add.
     */
    public void drawCard(UNOCard card) {
        if (card != null) {
            hand.add(card);
            this.calledUno = false;
        }
    }

    /**
     * Removes a specific card from the player's hand
     * 
     * @param card The card to remove.
     */
    public void discardCard(UNOCard card) {
        hand.remove(card);
    }

    /**
     * Checks if the player correctly called UNO on their previous turn if they had
     * one card.
     * 
     * @return true if UNO was called or wasn't required, false if they missed the
     *         call.
     */
    public boolean getCalledUno() {
        return calledUno;
    }

    public void setCalledUno(boolean called) {
        this.calledUno = called;
    }

    /**
     * Plays a chosen card: remove from hand, add to discard pile.
     * 
     * @param cardToPlay
     * @return The card that was played.
     */
    private UNOCard playChosenCard(UNOCard cardToPlay) {
        System.out.println(getName() + " plays: " + cardToPlay);
        discardCard(cardToPlay); // Remove from hand
        game.addToDiscardPile(cardToPlay); // Add to discard pile
        return cardToPlay;
    }

    /**
     * Finds all playable cards
     * 
     * @param topCard
     * @return A list of playable cards, can be empty.
     */
    private ArrayList<UNOCard> findPlayableCards(UNOCard topCard) {
        ArrayList<UNOCard> playable = new ArrayList<>();
        for (UNOCard card : hand)
            if (isCardPlayable(card, topCard))
                playable.add(card);
        return playable;
    }

    public void displayHand() {
        displayHand(null);
    }

    /**
     * Displays the player's hand
     * 
     * @param cardsToHighlight
     */
    private void displayHand(ArrayList<UNOCard> cardsToHighlight) {
        System.out.println("\n" + getName() + "'s Hand (" + hand.size() + " cards):");
        if (hand.isEmpty()) {
            System.out.println("  (Empty Hand)");
        } else {
            for (int i = 0; i < hand.size(); i++) {
                UNOCard card = hand.get(i);
                boolean isPlayable = (cardsToHighlight != null && cardsToHighlight.contains(card));
                System.out.println("  " + (i + 1) + ". " + card + (isPlayable ? " *" : ""));
            }
        }
        System.out.println("--------------------");
    }

    /**
     * Checks if a card is playable
     *
     * @param cardToPlay
     * @param topDiscardCard
     * @return true if the card is playable, false otherwise.
     */
    private boolean isCardPlayable(UNOCard cardToPlay, UNOCard topDiscardCard) {
        if (cardToPlay == null || topDiscardCard == null)
            return false; // Cannot play on empty discard or null card

        UNOCard.Color chosenWildColor = game.getCurrentColor(); // Get color if a Wild was played

        // Rule 1: Wild cards (Regular Wild, WD4) are always playable
        if (cardToPlay.getColor() == UNOCard.Color.WILD)
            return true;

        // Rule 2: Matching the currently chosen Wild color
        if ((topDiscardCard.getType() == UNOCard.Type.WILD || topDiscardCard.getType() == UNOCard.Type.WILD_DRAW_FOUR)
                && chosenWildColor != null)
            return cardToPlay.getColor() == chosenWildColor;

        // Rule 3: Match on the actual top card (if it's NOT wild)
        // Match Color (and top card is not Wild)
        if (cardToPlay.getColor() == topDiscardCard.getColor() && topDiscardCard.getColor() != UNOCard.Color.WILD)
            return true;

        // Match Number (only Number cards)
        if (cardToPlay.getType() == UNOCard.Type.NUMBER && topDiscardCard.getType() == UNOCard.Type.NUMBER)
            if (cardToPlay.getValue() == topDiscardCard.getValue())
                return true;

        // Match Action Type (Skip, Reverse, Draw Two)
        if (cardToPlay.getType() != UNOCard.Type.NUMBER && cardToPlay.getColor() != UNOCard.Color.WILD
                && cardToPlay.getType() == topDiscardCard.getType())
            return true;

        // If no rules above match, it's not playable
        return false;
    }
}