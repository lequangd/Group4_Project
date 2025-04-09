package ca.sheridancollege.project;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * The class that executes the game flow
 *
 * @author Quang Dung Le April 2025
 */

public class UNOGame extends Game {
    private UNODeck deck;
    private ArrayList<UNOCard> discardPile;
    private int currentPlayerIndex;
    private boolean isReversed;
    private UNOCard.Color currentColor;
    private Scanner scanner;
    private boolean gameWon;

    public UNOGame(String name, Scanner scanner) {
        super(name);
        this.scanner = scanner;
        this.deck = new UNODeck();
        this.discardPile = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.isReversed = false;
        this.currentColor = null;
        this.gameWon = false;
    }

    /**
     * Gets the card currently on top of the discard pile.
     * 
     * @return The top UNOCard, or null if the pile is empty.
     */
    public UNOCard getTopCard() {
        if (discardPile.isEmpty()) {
            return null;
        }
        return discardPile.get(discardPile.size() - 1);
    }

    /**
     * Reshuffles the discard pile (except the top card) back into the deck when the
     * deck runs out of cards.
     */
    public void reshuffleDeck() {
        if (discardPile.size() <= 1) {
            System.out.println("Not enough cards in discard pile to reshuffle!");
            return;
        }
        System.out.println("Deck is empty! Reshuffling the discard pile.");
        UNOCard topCard = discardPile.remove(discardPile.size() - 1);
        ArrayList<Card> cardsToReshuffle = new ArrayList<>();
        for (UNOCard unoCard : discardPile) {
            cardsToReshuffle.add(unoCard);
        }
        deck.addCards(cardsToReshuffle);
        deck.shuffle();
        System.out.println("Deck reshuffled with " + deck.getCards().size() + " cards.");
        discardPile.clear();
        discardPile.add(topCard);
    }

    /**
     * Executes the main game flow, including setup and turn management.
     */
    @Override
    public void play() {
        if (getPlayers() == null || getPlayers().size() < 2) {
            System.out.println("Error: Not enough players to start the game.");
            return;
        }

        // Deal cards
        System.out.println("Dealing 7 cards to each player.");
        for (Player player : getPlayers()) {
            if (player instanceof UNOPlayer) {
                UNOPlayer unoPlayer = (UNOPlayer) player;
                for (int i = 0; i < 7; i++)
                    drawCardsForPlayer(unoPlayer, 1, false);
                System.out.println("Dealt cards to " + player.getName());
            }
        }

        // Discard first card
        System.out.println("\nSetting up discard pile.");
        setupFirstDiscardCard();
        if (gameWon)
            return;

        // Game start
        while (!gameWon) {
            UNOPlayer currentPlayer = (UNOPlayer) getPlayers().get(currentPlayerIndex);

            // Check for UNO call penalty opportunity
            checkForUnoPenalty(currentPlayer);
            if (gameWon)
                break;

            displayTurnInfo(currentPlayer);

            UNOCard cardPlayed = currentPlayer.play();
            if (gameWon)
                break;

            // 3d. Check Win Condition
            if (cardPlayed != null && currentPlayer.getHand().isEmpty()) {
                declareWinner();
                break;
            }

            // Apply Card Effect
            boolean turnSkippedByEffect = false;
            if (cardPlayed != null) {
                turnSkippedByEffect = CardEffectHandler.applyEffect(cardPlayed, this, currentPlayer, scanner);
                if (gameWon)
                    break;

                if (cardPlayed.getType() != UNOCard.Type.WILD && cardPlayed.getType() != UNOCard.Type.WILD_DRAW_FOUR) {
                    setCurrentColor(null);
                }
            }

            // 3f. Advance turn
            if (!gameWon) {
                advanceTurn();
                // If the effect was specifically a skip, advance one more time.
                if (turnSkippedByEffect) {
                    System.out.println("Advancing turn again due to skip effect.");
                    advanceTurn();
                }
            }

            // Check if deck is empty
            if (!gameWon && deck.isEmpty()) {
                if (discardPile.size() > 1) {
                    reshuffleDeck();
                } else {
                    System.out.println("\nDeck is empty and cannot be reshuffled! Game cannot continue.");
                    gameWon = true;
                    declareWinner();
                }
            }
        }
    }

    /**
     * Sets up the first card
     */
    private void setupFirstDiscardCard() {
        UNOCard firstCard = null;
        do {
            if (deck.isEmpty()) {
                if (discardPile.size() > 1)
                    reshuffleDeck();
                else {
                    gameWon = true;
                    System.out.println("Error: Cannot start game - deck empty."); // Should not happen
                    return;
                }
                if (deck.isEmpty()) {
                    gameWon = true;
                    System.out.println("Error: Cannot start game - deck still empty."); // Should not happen
                    return;
                }
            }
            firstCard = deck.drawCard();
            if (firstCard != null && firstCard.getType() == UNOCard.Type.WILD_DRAW_FOUR) {
                System.out.println("First card is Wild Draw Four, returning to deck and drawing again.");
                ArrayList<Card> cardToReturn = new ArrayList<>();
                cardToReturn.add(firstCard);
                deck.addCards(cardToReturn);
                deck.shuffle();
                firstCard = null;
            }
        } while (firstCard == null);
        System.out.println("First card: " + firstCard);
        discardPile.add(firstCard);
        if (firstCard.getColor() != UNOCard.Color.WILD) {
            this.currentColor = firstCard.getColor();
        } else {
            this.currentColor = null;
        }
        handleFirstCardEffect(firstCard);
    }

    /*
     * Displays the current game state at the start of a player's turn.
     */
    private void displayTurnInfo(UNOPlayer currentPlayer) {
        System.out.println("\n------------------------------");
        System.out.println(currentPlayer.getName() + "'s turn!");
        UNOCard top = getTopCard();
        if (top != null) {
            String topCardDisplay = top.toString();
            if ((top.getType() == UNOCard.Type.WILD || top.getType() == UNOCard.Type.WILD_DRAW_FOUR)
                    && currentColor != null) {
                topCardDisplay += " (Chosen Color: " + currentColor + ")";
            }
            System.out.println("Top card: " + topCardDisplay);
            System.out.println("Cards remaining:");
            for (Player p : getPlayers()) {
                if (p instanceof UNOPlayer) {
                    System.out.println("  - " + p.getName() + ": " + ((UNOPlayer) p).getHand().size());
                }
            }
        } else {
            System.out.println("Error: Discard pile is empty!");
        }
    }

    /**
     * Checks if the last player called UNO correctly
     */
    private void checkForUnoPenalty(UNOPlayer currentPlayer) {
        Player previousPlayer = getPreviousPlayer();
        if (previousPlayer instanceof UNOPlayer) {
            UNOPlayer unoPreviousPlayer = (UNOPlayer) previousPlayer;
            if (unoPreviousPlayer.getHand().size() == 1 && !unoPreviousPlayer.hasCalledUno()) {
                System.out.println("\n!!! " + unoPreviousPlayer.getName() + " has 1 card but forgot to call UNO!");
                System.out.print(currentPlayer.getName() + ", do you want to challenge? (yes/no): ");
                String choice = "";
                while (!choice.equals("yes") && !choice.equals("no")) {
                    choice = scanner.nextLine().trim().toLowerCase();
                    if (!choice.equals("yes") && !choice.equals("no")) {
                        System.out.print("   Please enter 'yes' or 'no': ");
                    }
                }
                if (choice.equals("yes")) {
                    System.out.println(
                            "Challenge successful! " + unoPreviousPlayer.getName() + " draws 2 penalty cards.");
                    drawCardsForPlayer(unoPreviousPlayer, 2, true);
                    unoPreviousPlayer.setCalledUno(true);
                } else {
                    System.out.println("Challenge declined.");
                    unoPreviousPlayer.setCalledUno(true);
                }
            }
        }
    }

    /**
     * Handles the special effects of the first card
     */
    private void handleFirstCardEffect(UNOCard card) {
        System.out.println("Handling effect of first card: " + card);
        UNOPlayer firstPlayer = (UNOPlayer) getPlayers().get(0);
        boolean initialSkip = false;

        switch (card.getType()) {
            case SKIP:
                System.out.println("First card is Skip. Player 1 (" + firstPlayer.getName() + ") is skipped.");
                initialSkip = true;
                break;
            case REVERSE:
                System.out.println("First card is Reverse. Direction reversed.");
                isReversed = true;
                // Reverse the turns immediately
                currentPlayerIndex = (0 - 1 + getPlayers().size()) % getPlayers().size();
                System.out.println("Player " + getPlayers().get(currentPlayerIndex).getName() + " starts.");
                break;
            case DRAW_TWO:
                System.out.println("First card is Draw Two. Player 1 (" + firstPlayer.getName() + ") draws 2 and is skipped.");
                drawCardsForPlayer(firstPlayer, 2, true);
                initialSkip = true;
                break;
            case WILD:
                System.out.println("First card is Wild. Player 1 (" + firstPlayer.getName() + ") chooses the starting color.");
                UNOCard.Color chosen = CardEffectHandler.chooseColor(scanner);
                setCurrentColor(chosen);
                System.out.println("Color set to " + chosen + ".");
                break;
            case WILD_DRAW_FOUR:
                System.out.println("Error: WD4 effect.");
                gameWon = true;
                break; // Should not happen
            default:
                System.out.println(firstPlayer.getName() + "'s turn.");
                break;
        }
        // Perform the skip after the effect if needed
        if (initialSkip && !gameWon) {
            advanceTurn();
        }
    }

    /*
     * Advances the turn to the next player based on the current direction.
     */
    public void advanceTurn() {
        if (isReversed) {
            currentPlayerIndex = (currentPlayerIndex - 1 + getPlayers().size()) % getPlayers().size();
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % getPlayers().size();
        }
        ((UNOPlayer) getPlayers().get(currentPlayerIndex)).setCalledUno(false);
    }

    /*
     * Draws cards for a player,
     * reshuffles
     * and handles game end conditions.
     */
    public void drawCardsForPlayer(UNOPlayer player, int count, boolean showMessages) {
        if (count <= 0 || gameWon)
            return;
        if (showMessages)
            System.out.println("   " + player.getName() + " must draw " + count + " card(s).");
        for (int i = 0; i < count; i++) {
            if (deck.isEmpty()) {
                if (discardPile.size() > 1)
                    reshuffleDeck();
                else {
                    if (showMessages)
                        System.out.println("Cannot draw card! Deck empty."); // Should not happen
                    gameWon = true;
                    declareWinner();
                    return;
                }
                if (deck.isEmpty()) {
                    if (showMessages)
                        System.out.println("Cannot draw card after reshuffle!"); // Should not happen
                    gameWon = true;
                    declareWinner();
                    return;
                }
            }
            UNOCard drawn = deck.drawCard();
            if (drawn != null) {
                player.drawCard(drawn);
                if (showMessages && count == 1)
                    System.out.println("   Drew: " + drawn);
            } else {
                if (showMessages)
                    System.out.println("Error drawing card despite non-empty deck check."); // Should not happen
                gameWon = true;
                declareWinner();
                return;
            }
        }
        if (showMessages && count > 1)
            System.out.println("   " + player.getName() + " finished drawing " + count + " cards. Now has " + player.getHand().size() + " cards.");
        if (player.getHand().size() != 1)
            player.setCalledUno(false);
    }

    public boolean isReversed() {
        return isReversed;
    }

    public void setReversed(boolean reversed) {
        isReversed = reversed;
    }

    public Player getNextPlayer() {
        int nextIndex;
        if (isReversed) {
            nextIndex = (currentPlayerIndex - 1 + getPlayers().size()) % getPlayers().size();
        } else {
            nextIndex = (currentPlayerIndex + 1) % getPlayers().size();
        }
        return getPlayers().get(nextIndex);
    }

    public Player getPreviousPlayer() {
        if (getPlayers() == null || getPlayers().isEmpty())
            return null;
        int prevIndex;
        if (isReversed) {
            prevIndex = (currentPlayerIndex + 1) % getPlayers().size();
        } else {
            prevIndex = (currentPlayerIndex - 1 + getPlayers().size()) % getPlayers().size();
        }
        if (prevIndex >= 0 && prevIndex < getPlayers().size()) {
            return getPlayers().get(prevIndex);
        }
        return null;
    }

    public UNODeck getDeck() {
        return deck;
    }

    public ArrayList<UNOCard> getDiscardPile() {
        return discardPile;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentColor(UNOCard.Color color) {
        this.currentColor = color;
    }

    public UNOCard.Color getCurrentColor() {
        return currentColor;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    /** Declares the winner or announces abnormal game end. */
    @Override
    public void declareWinner() {
        System.out.println("\n===================================");
        // End if there's an error
        boolean normalWin = false;
        if (!gameWon && getPlayers().size() > 0 && currentPlayerIndex >= 0
                && currentPlayerIndex < getPlayers().size()) {
            UNOPlayer potentialWinner = (UNOPlayer) getPlayers().get(currentPlayerIndex);
            if (potentialWinner.getHand().isEmpty()) {
                System.out.println(potentialWinner.getName() + " has played their last card!");
                System.out.println("CONGRATULATIONS! " + potentialWinner.getName() + " is the WINNER!");
                normalWin = true;
            }
        }

        if (!normalWin) {
            System.out.println("The game has ended."); // Due to error
        }
        System.out.println("===================================");
        this.gameWon = true;
    }
}