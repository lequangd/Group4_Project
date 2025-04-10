package ca.sheridancollege.project;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Abstract class defining the core structure and shared logic
 *
 * @author Quang Dung Le April 2025
 */
public abstract class UNOManager {
    protected UNODeck deck;
    protected ArrayList<UNOCard> discardPile;
    protected int currentPlayerIndex;
    protected boolean isReversed;
    protected UNOCard.Color currentColor;
    protected Scanner scanner;
    protected boolean gameWon;
    protected ArrayList<Player> players;
    protected String winnerName = null;
    protected boolean abnormalEnd = false;

    public UNOManager(Scanner scanner, ArrayList<Player> playerList) {
        this.scanner = scanner;
        this.players = playerList;
        this.deck = new UNODeck();
        this.discardPile = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.isReversed = false;
        this.currentColor = null;
        this.gameWon = false;
        this.winnerName = null;
        this.abnormalEnd = false;
    }

    /**
     * Updates the internal player list reference
     * 
     * @param playerList The potentially updated list of players.
     */
    public void updatePlayerList(ArrayList<Player> playerList) {
        this.players = playerList;
    }

    /**
     * Contains the main game loop structure. Execute the game turn by turn.
     */
    public void runGame() {
        // Perform initial setup
        if (!setupGame()) {
            this.abnormalEnd = true; // Ensure abnormal flag is set if setup fails
            this.gameWon = true;
            return; // Exit if setup fails
        }

        // Main Game Loop
        while (!gameWon) {
            // Check for valid player index
            if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
                System.out.println("Invalid current player index!");
                this.abnormalEnd = true;
                this.gameWon = true;
                break;
            }
            UNOPlayer currentPlayer = (UNOPlayer) players.get(currentPlayerIndex);

            // Check UNO penalty for previous player
            checkForUnoPenalty(currentPlayer);
            if (gameWon) break; // Exit if game ended during penalty check (error occured)

            // Display current game state
            displayTurnInfo(currentPlayer);

            // Player takes action (play or draw)
            UNOCard cardPlayed = currentPlayer.play();
            if (gameWon) break; // Exit if game ended during play

            // Check for win condition after playing card
            if (cardPlayed != null && currentPlayer.getHand().isEmpty()) {
                this.winnerName = currentPlayer.getName(); // Set winner
                this.gameWon = true;
                break; // Exit loop
            }

            // Apply card effect if a card was played
            boolean turnSkippedByEffect = false;
            if (cardPlayed != null) {
                turnSkippedByEffect = applyCardEffect(cardPlayed, currentPlayer);
                // Reset chosen color if the played card was NOT Wild/WD4
                if (cardPlayed.getType() != UNOCard.Type.WILD && cardPlayed.getType() != UNOCard.Type.WILD_DRAW_FOUR) setCurrentColor(null);
            }

            // Advance turn based on outcome
            if (!gameWon) {
                advanceTurn(); // Advance to next player
                if (turnSkippedByEffect) {
                    System.out.println("...advancing turn again due to skip effect.");
                    advanceTurn(); // Advance second time for skip
                }
            }

            // Check deck status
            if (!gameWon && deck.isEmpty()) {
                if (discardPile.size() > 1) {
                    reshuffleDeck();
                } else {
                    System.out.println("\nERROR: Deck empty!"); // Should not happen
                    this.abnormalEnd = true;
                    this.gameWon = true;
                }
            }
        }
    }

    /**
     * Applies the specific effect of a card
     * 
     * @param cardPlayed
     * @param playerWhoPlayed
     * @return true if the effect causes the next player's turn to be skipped, false otherwise.
     */
    protected abstract boolean applyCardEffect(UNOCard cardPlayed, UNOPlayer playerWhoPlayed);

    /**
     * Handles the specific effect of the first card laid down at the start of the game.
     * 
     * @param firstCard
     */
    protected abstract void handleFirstCardEffect(UNOCard firstCard);

    /**
     * Performs the initial game setup.
     * 
     * @return true if setup was successful, false if an error occurred preventing game start.
     */
    protected boolean setupGame() {
        System.out.println("Dealing 7 cards...");
        if (this.players == null) return false; // No players to deal to, should not happen

        for (Player player : this.players) {
            if (player instanceof UNOPlayer) {
                UNOPlayer unoPlayer = (UNOPlayer) player;
                for (int i = 0; i < 7; i++) {
                    drawCardsForPlayer(unoPlayer, 1, false);
                    // Check for impossible draw condition during deal
                    if (deck.isEmpty() && discardPile.size() <= 1 && i < 6) {
                        System.out.println("Not enough cards to deal initial hands."); // Should not happen
                        return false; // Setup failed
                    }
                }
                System.out.println("Dealt cards to " + player.getName());
            }
        }
        System.out.println("\nSetting up discard pile.");
        if (!setupFirstDiscardCard()) {
            return false; // Setup failed if first card caused error
        }
        return !gameWon; // Return true if setup completed without ending game
    }

    /*
     * Sets up the very first card on the discard pile.
     */
    protected boolean setupFirstDiscardCard() {
        UNOCard firstCard = null;
        do {
            if (deck.isEmpty()) {
                if (discardPile.size() > 1)
                    reshuffleDeck();
                else {
                    gameWon = true;
                    abnormalEnd = true;
                    System.out.println("ERROR: Deck empty."); // Should not happen
                    return false;
                }
                if (deck.isEmpty()) {
                    gameWon = true;
                    abnormalEnd = true;
                    System.out.println("ERROR: Deck still empty."); // Should not happen
                    return false;
                }
            }
            firstCard = deck.drawCard();
            if (firstCard != null && firstCard.getType() == UNOCard.Type.WILD_DRAW_FOUR) {
                System.out.println("First card WD4, returning...");
                ArrayList<Card> cardToReturn = new ArrayList<>();
                cardToReturn.add(firstCard);
                deck.addCards(cardToReturn);
                deck.shuffle();
                firstCard = null;
            }
        } while (firstCard == null);

        System.out.println("First card: " + firstCard);
        discardPile.add(firstCard);
        if (firstCard.getColor() != UNOCard.Color.WILD)
            this.currentColor = firstCard.getColor();
        else
            this.currentColor = null;

        handleFirstCardEffect(firstCard);
        return !gameWon; // Return true if effect handling didn't end game
    }

    /*
     * Reshuffles the discard pile back into the deck.
     */
    protected void reshuffleDeck() {
        if (discardPile.size() <= 1) {
            System.out.println("Not enough cards to reshuffle!");
            return;
        }
        System.out.println("Deck empty! Reshuffling discard pile...");
        UNOCard topCard = discardPile.remove(discardPile.size() - 1);
        ArrayList<Card> cardsToReshuffle = new ArrayList<>();
        for (UNOCard unoCard : discardPile) cardsToReshuffle.add(unoCard);
        deck.addCards(cardsToReshuffle);
        deck.shuffle();
        System.out.println("Deck reshuffled with " + deck.getCards().size() + " cards.");
        discardPile.clear();
        discardPile.add(topCard);
    }

    /*
     * Displays current turn information.
     */
    protected void displayTurnInfo(UNOPlayer currentPlayer) {
        System.out.println("\n------------------------------");
        System.out.println(currentPlayer.getName() + "'s turn!");
        UNOCard top = getTopCard();
        if (top != null) {
            String topCardDisplay = top.toString();
            if ((top.getType() == UNOCard.Type.WILD || top.getType() == UNOCard.Type.WILD_DRAW_FOUR) && currentColor != null)
                topCardDisplay += " (Chosen: " + currentColor + ")";
            System.out.println("Top card: " + topCardDisplay);
            System.out.println("Cards remaining:");
            if (this.players != null)
                for (Player p : this.players)
                    if (p instanceof UNOPlayer)
                        System.out.println("  - " + p.getName() + ": " + ((UNOPlayer) p).getHand().size());
        } else
            System.out.println("ERROR: Discard pile empty!");
    }

    /*
     * Checks if the previous player should be penalized for not calling UNO.
     */
    protected void checkForUnoPenalty(UNOPlayer currentPlayer) {
        Player previousPlayer = getPreviousPlayer();
        if (previousPlayer instanceof UNOPlayer) {
            UNOPlayer unoPrev = (UNOPlayer) previousPlayer;
            if (unoPrev.getHand().size() == 1 && !unoPrev.getCalledUno()) {
                System.out.println("\n!!! " + unoPrev.getName() + " forgot UNO!");
                System.out.print(currentPlayer.getName() + ", challenge? (yes/no): ");
                String choice = "";
                while (!choice.equals("yes") && !choice.equals("no")) {
                    choice = scanner.nextLine().trim().toLowerCase();
                    if (!choice.equals("yes") && !choice.equals("no"))
                        System.out.print("   Enter 'yes' or 'no': ");
                }
                if (choice.equals("yes")) {
                    System.out.println("Challenge success! " + unoPrev.getName() + " draws 2.");
                    drawCardsForPlayer(unoPrev, 2, true);
                    unoPrev.setCalledUno(true);
                } else {
                    System.out.println("Challenge declined.");
                    unoPrev.setCalledUno(true);
                }
            }
        }
    }

    /*
     * Advances the turn index based on direction.
     */
    protected void advanceTurn() {
        if (this.players == null || this.players.isEmpty()) return; // Safety check
        if (isReversed)
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        else
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        // Reset UNO flag for current player
        ((UNOPlayer) players.get(currentPlayerIndex)).setCalledUno(false);
    }

    /*
     * Draws cards for a player, handling reshuffle and game end.
     */
    public void drawCardsForPlayer(UNOPlayer player, int count, boolean showMessages) {
        if (count <= 0 || gameWon)
            return;
        if (showMessages)
            System.out.println("   " + player.getName() + " must draw " + count + " card(s)...");
        for (int i = 0; i < count; i++) {
            if (deck.isEmpty()) {
                if (discardPile.size() > 1)
                    reshuffleDeck();
                else {
                    if (showMessages) System.out.println("ERROR: Deck empty...");
                    gameWon = true;
                    abnormalEnd = true;
                    return;
                }
                if (deck.isEmpty()) {
                    if (showMessages) System.out.println("ERROR: Cannot draw after reshuffle!");
                    gameWon = true;
                    abnormalEnd = true;
                    return;
                }
            }
            UNOCard drawn = deck.drawCard();
            if (drawn != null) {
                player.drawCard(drawn);
                if (showMessages && count == 1)
                    System.out.println("   Drew: " + drawn);
            } else {
                if (showMessages) System.out.println("   Error drawing card...");
                gameWon = true;
                abnormalEnd = true;
                return;
            }
        }
        if (showMessages && count > 1)
            System.out.println(player.getName() + " finished drawing. Now has " + player.getHand().size() + " cards.");
        if (player.getHand().size() != 1) player.setCalledUno(false);
    }

    /*
     * Add card to discard pile
     */
    protected void addToDiscardPile(UNOCard card) {
        if (card != null) this.discardPile.add(card);
    }

    protected UNOCard getTopCard() {
        return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1);
    }

    protected UNOCard.Color getCurrentColor() {
        return currentColor;
    }

    protected void setCurrentColor(UNOCard.Color color) {
        this.currentColor = color;
    }

    protected boolean getIsReversed() {
        return isReversed;
    }

    protected void setIsReversed(boolean reversed) {
        isReversed = reversed;
    }

    protected int getPlayerCount() {
        return players != null ? players.size() : 0;
    }

    protected Player getNextPlayer() {
        if (players == null || players.isEmpty())
            return null;
        int nextIndex;
        if (isReversed)
            nextIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        else
            nextIndex = (currentPlayerIndex + 1) % players.size();
        return players.get(nextIndex);
    }

    protected Player getPreviousPlayer() {
        if (players == null || players.isEmpty())
            return null;
        int prevIndex;
        if (isReversed)
            prevIndex = (currentPlayerIndex + 1) % players.size();
        else
            prevIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        if (prevIndex >= 0 && prevIndex < players.size())
            return players.get(prevIndex);
        return null;
    }

    protected ArrayList<UNOCard> getDiscardPile() {
        return discardPile;
    } 

    protected UNODeck getDeck() {
        return deck;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public boolean didGameEndAbnormally() {
        return abnormalEnd;
    }

    public boolean isGameWon() {
        return gameWon;
    }

}