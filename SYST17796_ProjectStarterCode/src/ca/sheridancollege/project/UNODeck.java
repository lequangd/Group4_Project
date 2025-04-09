package ca.sheridancollege.project;

import java.util.ArrayList;

public class UNODeck extends GroupOfCards {

    public UNODeck() {
        // Standard UNO deck has 108 cards
        super(108);
        cards = new ArrayList<>(108);
        initializeDeck();
        shuffle();
    }

    // Initialize a deck of cards
    private void initializeDeck() {
        cards.clear();
        for (UNOCard.Color color : UNOCard.Color.values()) {
            if (color == UNOCard.Color.WILD)
                continue; // Wild cards handled separately

            // Add number cards (1 zero, 2 each of 1-9 per color)
            cards.add(new UNOCard(color, UNOCard.Type.NUMBER, 0));
            for (int i = 1; i <= 9; i++) {
                cards.add(new UNOCard(color, UNOCard.Type.NUMBER, i));
                cards.add(new UNOCard(color, UNOCard.Type.NUMBER, i));
            }

            // Add special action cards (2 Skip, 2 Reverse, 2 Draw Two per color)
            for (int i = 0; i < 2; i++) {
                cards.add(new UNOCard(color, UNOCard.Type.SKIP, -1));
                cards.add(new UNOCard(color, UNOCard.Type.REVERSE, -1));
                cards.add(new UNOCard(color, UNOCard.Type.DRAW_TWO, -1));
            }
        }

        // Add wild cards (4 Wild, 4 Wild Draw Four)
        for (int i = 0; i < 4; i++) {
            cards.add(new UNOCard(UNOCard.Color.WILD, UNOCard.Type.WILD, -1));
            cards.add(new UNOCard(UNOCard.Color.WILD, UNOCard.Type.WILD_DRAW_FOUR, -1));
        }
    }

    /**
     * Draws a card from the top of the deck.
     *
     * @return The drawn UNOCard, or null if the deck is empty.
     */
    public UNOCard drawCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return (UNOCard) cards.remove(0);
    }

    /**
     * Checks if the deck is currently empty.
     * 
     * @return true if the deck has no cards, false otherwise.
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Adds a collection of cards back to the deck. Used for reshuffling.
     * 
     * @param cardsToAdd
     */
    public void addCards(ArrayList<Card> cardsToAdd) {
        this.cards.addAll(cardsToAdd);
    }
}