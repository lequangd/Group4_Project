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
     * Applies the effect of a played card
     *
     * @param cardPlayed
     * @param game
     * @param playerWhoPlayed
     * @param sc
     * @return true if the effect resulted in skipping the *next* player's turn, false otherwise.
     */
    public static boolean applyEffect(UNOCard cardPlayed, UNOGame game, UNOPlayer playerWhoPlayed, Scanner sc) {
        boolean turnSkipped = false;

        switch (cardPlayed.getType()) {
            case SKIP:
                turnSkipped = applySkipEffect(); // Returns true, skip
                break;
            case REVERSE:
                // Returns true ONLY if it acts like a skip (2 players)
                turnSkipped = applyReverseEffect(game);
                break;
            case DRAW_TWO:
                turnSkipped = applyDrawTwoEffect(game); // Returns true, skip
                break;
            case WILD:
                applyWildEffect(game, playerWhoPlayed, sc); // Doesn't skip
                break;
            case WILD_DRAW_FOUR:
                // Returns true only if the next player is skipped
                turnSkipped = applyWildDrawFourEffect(game, playerWhoPlayed, sc);
                break;
            default:
                break;
        }
        if (game.isGameWon()) {
            return true;
        } // Prevent turn advancement if game ended during effect
        return turnSkipped;
    }

    private static boolean applySkipEffect() {
        System.out.println("-> Effect: Skip card played! Skipping next player.");
        return true; // A skip happened
    }

    private static boolean applyReverseEffect(UNOGame game) {
        System.out.println("-> Effect: Reverse card played!");
        int playerCount = game.getPlayers().size();
        if (playerCount == 2) {
            System.out.println("   (In 2-player game, Reverse acts like Skip!)");
            return true; // A skip happened
        } else {
            System.out.println("   Reversing direction of play.");
            game.setReversed(!game.isReversed());
            return false; // Doesn't skip in 3+ players
        }
    }

    private static void applyWildEffect(UNOGame game, UNOPlayer currentPlayer, Scanner sc) {
        System.out.println("-> Effect: Wild card played!");
        UNOCard.Color chosenColor = chooseColor(sc);
        game.setCurrentColor(chosenColor);
        System.out.println("   " + currentPlayer.getName() + " chose " + chosenColor + ".");
    }

    private static boolean applyDrawTwoEffect(UNOGame game) {
        System.out.println("-> Effect: Draw Two card played!");
        UNOPlayer nextPlayer = (UNOPlayer) game.getNextPlayer();
        System.out.println("   " + nextPlayer.getName() + " must draw 2 cards and is skipped.");
        game.drawCardsForPlayer(nextPlayer, 2, true); // Draw the cards
        if (game.isGameWon())
            return true; // Check if game ended
        return true; // A skip happened
    }

    private static boolean applyWildDrawFourEffect(UNOGame game, UNOPlayer playerWhoPlayedWD4, Scanner sc) {
        System.out.println("-> Effect: Wild Draw Four card played!");
        UNOPlayer challenger = (UNOPlayer) game.getNextPlayer(); // Challenger is the next player
        boolean skipNextPlayer = false;

        boolean playedIllegally = checkWD4(game, playerWhoPlayedWD4);

        System.out.print("   " + challenger.getName() + ", do you want to challenge the Wild Draw Four? (yes/no): ");
        String choice = "";
        while (!choice.equals("yes") && !choice.equals("no")) {
            choice = sc.nextLine().trim().toLowerCase();
            if (!choice.equals("yes") && !choice.equals("no"))
                System.out.print("   Please enter 'yes' or 'no': ");
        }

        if (choice.equals("yes")) {
            System.out.println("   Challenge initiated!");
            System.out.println("   " + playerWhoPlayedWD4.getName() + " reveals hand:");
            playerWhoPlayedWD4.displayHand();

            if (playedIllegally) {
                // Challenged successfully.
                System.out.println("   Challenge Won!");
                System.out.println("   " + playerWhoPlayedWD4.getName() + " draws 4 cards.");
                game.drawCardsForPlayer(playerWhoPlayedWD4, 4, true);
                skipNextPlayer = false; // No skip, effect cancelled
            } else {
                // Challenge Failed! Challenger draws 6 and IS skipped.
                System.out.println("   Challenge Lost!");
                System.out.println("   " + challenger.getName() + " draws 6 cards and is skipped!");
                game.drawCardsForPlayer(challenger, 6, true);
                if (game.isGameWon())
                    return true; // Check end game
                skipNextPlayer = true; // Challenger is skipped
                System.out.println("\n   " + playerWhoPlayedWD4.getName() + ", choose the color after failed challenge:");
                UNOCard.Color chosenColor = chooseColor(sc);
                game.setCurrentColor(chosenColor);
                System.out.println("   Color set to " + chosenColor + ".");
            }
        } else {
            // No challenge
            System.out.println("   No challenge made.");
            System.out.println("   " + challenger.getName() + " draws 4 cards and is skipped.");
            game.drawCardsForPlayer(challenger, 4, true); // Challenger draws 4
            skipNextPlayer = true; // Challenger is skipped
            System.out.println("\n   " + playerWhoPlayedWD4.getName() + ", choose the color:");
            UNOCard.Color chosenColor = chooseColor(sc);
            game.setCurrentColor(chosenColor);
            System.out.println("   Color set to " + chosenColor + ".");
        }
        return skipNextPlayer;
    }

    // Check if Wild Draw 4 was played legally
    private static boolean checkWD4(UNOGame game, UNOPlayer playerWhoPlayedWD4) {
        boolean playedIllegally = false;
        UNOCard cardBeforeWD4 = null;
        if (game.getDiscardPile().size() >= 2)
            cardBeforeWD4 = game.getDiscardPile().get(game.getDiscardPile().size() - 2);
        if (cardBeforeWD4 != null) {
            UNOCard.Color requiredColor = cardBeforeWD4.getColor();
            if (requiredColor == UNOCard.Color.WILD)
                requiredColor = game.getCurrentColor();
            if (requiredColor != null && requiredColor != UNOCard.Color.WILD) {
                for (UNOCard cardInHand : playerWhoPlayedWD4.getHand()) {
                    if (cardInHand.getColor() == requiredColor) {
                        playedIllegally = true;
                        break;
                    }
                }
            }
        }
        return playedIllegally;
    }

    public static UNOCard.Color chooseColor(Scanner sc) {
        while (true) {
            System.out.print("Choose a color - Enter 1(Red), 2(Green), 3(Blue), 4(Yellow): ");
            try {
                int choice = sc.nextInt();
                if (sc.hasNextLine())
                    sc.nextLine();
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
                        System.out.println("Invalid choice.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
                if (sc.hasNextLine())
                    sc.nextLine();
            }
        }
    }
}