package ca.sheridancollege.project;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * The class that handles the effect of special cards (other than number)
 *
 * @author Quang Dung Le April 2025
 */
public class CardEffectHandler {
    /**
     * Applies the effect of a played card by interacting with the UNOManager.
     *
     * @param cardPlayed
     * @param manager
     * @param playerWhoPlayed
     * @param sc
     * @return true if the effect resulted in skipping the NEXT player's turn, false otherwise.
     */
    public static boolean applyEffect(UNOCard cardPlayed, UNOManager manager, UNOPlayer playerWhoPlayed, Scanner sc) {
        boolean turnSkipped = false; // Assume no skip initially

        switch (cardPlayed.getType()) {
            case SKIP:
                turnSkipped = applySkipEffect(); // Returns true, signals skip
                break;
            case REVERSE:
                // Returns true ONLY if it acts like a skip (2 players)
                turnSkipped = applyReverseEffect(manager);
                break;
            case DRAW_TWO:
                turnSkipped = applyDrawTwoEffect(manager); // returns true
                break;
            case WILD:
                applyWildEffect(manager, playerWhoPlayed, sc); // doesn't skip
                break;
            case WILD_DRAW_FOUR:
                // Returns true only if the next player is ultimately skipped (after challenge)
                turnSkipped = applyWildDrawFourEffect(manager, playerWhoPlayed, sc);
                break;
            default: // NUMBER cards
                break;
        }
        // Check game state after effect applied
        if (manager.isGameWon()) return true;
        return turnSkipped;
    }

    /** Signals that a skip should occur. */
    private static boolean applySkipEffect() {
        System.out.println("Skip card played! Skipping next player.");
        return true; // Signal skip
    }

    /*
     * Applies Reverse effect, potentially signaling a skip in 2-player games.
     */
    private static boolean applyReverseEffect(UNOManager manager) {
        System.out.println("-> Effect: Reverse card played!");
        int playerCount = manager.getPlayerCount();
        if (playerCount == 2) {
            System.out.println("   (In 2-player game, Reverse acts like Skip!)");
            return true; // Signal skip
        } else {
            System.out.println("   Reversing direction of play.");
            manager.setIsReversed(!manager.getIsReversed());
            return false; // Doesn't skip in 3+ players
        }
    }

    /*
     * Applies Wild effect (choosing color).
     */
    private static void applyWildEffect(UNOManager manager, UNOPlayer currentPlayer, Scanner sc) {
        System.out.println("-> Effect: Wild card played!");
        UNOCard.Color chosenColor = chooseColor(sc);
        manager.setCurrentColor(chosenColor);
        System.out.println("   " + currentPlayer.getName() + " chose " + chosenColor + ".");
    }

    /** Applies Draw Two effect (next player draws 2, signals skip). */
    private static boolean applyDrawTwoEffect(UNOManager manager) {
        System.out.println("-> Effect: Draw Two card played!");
        UNOPlayer nextPlayer = (UNOPlayer) manager.getNextPlayer();
        System.out.println("   " + nextPlayer.getName() + " must draw 2 cards and is skipped.");
        manager.drawCardsForPlayer(nextPlayer, 2, true);
        if (manager.isGameWon()) return true;
        return true; // Signal skip
    }

    /**
     * Applies Wild Draw Four effect (challenge, draw 4/6, choose color, signals skip).
     */
    private static boolean applyWildDrawFourEffect(UNOManager manager, UNOPlayer playerWhoPlayedWD4, Scanner sc) {
        System.out.println("-> Effect: Wild Draw Four card played!");
        UNOPlayer challenger = (UNOPlayer) manager.getNextPlayer();
        boolean skipNextPlayer = false; // Flag to indicate skip status

        boolean playedIllegally = checkWD4(manager, playerWhoPlayedWD4);

        System.out.print("   " + challenger.getName() + ", do you want to challenge? (yes/no): ");
        String choice = "";
        while (!choice.equals("yes") && !choice.equals("no")) {
            choice = sc.nextLine().trim().toLowerCase();
            if (!choice.equals("yes") && !choice.equals("no")) System.out.print("   Please enter 'yes' or 'no': ");
        }

        if (choice.equals("yes")) {
            // Challenge
            System.out.println("   Challenge initiated!");
            System.out.println("   " + playerWhoPlayedWD4.getName() + " reveals hand:");
            playerWhoPlayedWD4.displayHand();

            if (playedIllegally) {
                // Challenge Successful! WD4 player draws 4. Turn proceeds normally.
                System.out.println("   Challenge Won!");
                System.out.println("   " + playerWhoPlayedWD4.getName() + " draws 4 cards.");
                manager.drawCardsForPlayer(playerWhoPlayedWD4, 4, true);
                skipNextPlayer = false; // No skip
                System.out.println("\n   " + playerWhoPlayedWD4.getName() + ", choose the color:");
                UNOCard.Color chosenColor = chooseColor(sc);
                manager.setCurrentColor(chosenColor);
                System.out.println("   Color set to " + chosenColor + ".");
                return skipNextPlayer;
            } else {
                // Challenge Failed! Challenger draws 6 and IS skipped.
                System.out.println("   Challenge Lost!");
                System.out.println("   " + challenger.getName() + " draws 6 cards and is skipped!");
                manager.drawCardsForPlayer(challenger, 6, true);
                if (manager.isGameWon()) return true;
                skipNextPlayer = true; // Challenger is skipped
                System.out.println("\n   " + playerWhoPlayedWD4.getName() + ", choose the color:");
                UNOCard.Color chosenColor = chooseColor(sc);
                manager.setCurrentColor(chosenColor);
                System.out.println("   Color set to " + chosenColor + ".");
                return skipNextPlayer;
            }
        } else {
            // No Challenge
            System.out.println("   No challenge made.");
            System.out.println("   " + challenger.getName() + " draws 4 cards and is skipped.");
            manager.drawCardsForPlayer(challenger, 4, true);
            if (manager.isGameWon()) return true;
            skipNextPlayer = true; // Challenger IS skipped
            System.out.println("\n   " + playerWhoPlayedWD4.getName() + ", choose the color:");
            UNOCard.Color chosenColor = chooseColor(sc);
            manager.setCurrentColor(chosenColor);
            System.out.println("   Color set to " + chosenColor + ".");
            return skipNextPlayer;
        }
    }

    /*
     * Check if WD4 was played legally
     */
    private static boolean checkWD4(UNOManager manager, UNOPlayer playerWhoPlayedWD4) {
        boolean playedIllegally = false;
        UNOCard cardBeforeWD4 = null;
        // Access discard pile
        if (manager.getDiscardPile().size() >= 2)
            cardBeforeWD4 = manager.getDiscardPile().get(manager.getDiscardPile().size() - 2);
        if (cardBeforeWD4 != null) {
            UNOCard.Color requiredColor = cardBeforeWD4.getColor();
            // Access current color before WD4 was played
            if (requiredColor == UNOCard.Color.WILD) requiredColor = manager.getCurrentColor();
            if (requiredColor != null && requiredColor != UNOCard.Color.WILD)
                for (UNOCard cardInHand : playerWhoPlayedWD4.getHand())
                    if (cardInHand.getColor() == requiredColor) {
                        playedIllegally = true;
                        break;
                    }
                
            
        }
        return playedIllegally;
    }

    /*
     * Prompts the user to choose a color (R, G, B, Y).
     */
    public static UNOCard.Color chooseColor(Scanner sc) {
        while (true) {
            System.out.print("   Choose a color - Enter 1(Red), 2(Green), 3(Blue), 4(Yellow): ");
            try {
                int choice = sc.nextInt();
                if (sc.hasNextLine()) sc.nextLine();
                switch (choice) {
                    case 1:
                        return UNOCard.Color.RED;
                    case 2:
                        return UNOCard.Color.GREEN;
                    case 3:
                        return UNOCard.Color.BLUE;
                    case 4:
                        return UNOCard.Color.YELLOW;
                    default:
                        System.out.println("   Invalid choice.");
                }
            } catch (InputMismatchException e) {
                System.out.println("   Invalid input.");
                if (sc.hasNextLine())
                    sc.nextLine();
            }
        }
    }
}