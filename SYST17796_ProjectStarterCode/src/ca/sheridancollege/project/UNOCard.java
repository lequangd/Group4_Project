package ca.sheridancollege.project;

/**
 * The class that handles information of the card
 *
 * @author Quang Dung Le April 2025
 */

public class UNOCard extends Card {
    // Enum for standard UNO card colors, including WILD
    public enum Color {
        RED, BLUE, GREEN, YELLOW, WILD
    }

    // Enum for card types (Number cards, Action cards, Wild cards)
    public enum Type {
        NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
    }

    private final Color color;
    private final Type type;
    private final int value;

    public UNOCard(Color color, Type type, int value) {
        this.color = color;
        this.type = type;
        this.value = value;
    }

    public Color getColor() {
        return color;
    }

    public Type getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    // Returns a string name of the card.
    @Override
    public String toString() {
        if (type == Type.NUMBER) {
            return color + " " + value;
        } else if (type == Type.WILD || type == Type.WILD_DRAW_FOUR) {
            return type.toString();
        } else {
            return color + " " + type;
        }
    }
}