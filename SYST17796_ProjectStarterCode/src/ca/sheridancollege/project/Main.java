package ca.sheridancollege.project;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to UNO!");
        int numPlayers = 0;
        // Get number of players (2-10)
        while (numPlayers < 2 || numPlayers > 10) {
            System.out.print("Enter the number of players (2-10): ");
            try {
                numPlayers = scanner.nextInt();
                if (numPlayers < 2 || numPlayers > 10) {
                    System.out.println("Please enter a number between 2 and 10.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            } finally {
                if (scanner.hasNextLine())
                    scanner.nextLine();
            }
        }

        UNOGame game = new UNOGame("UNO Game", scanner);

        // Create player instances
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter name for Player " + (i + 1) + ": ");
            String name = scanner.nextLine();
            players.add(new UNOPlayer(name, game, scanner));
        }
        game.setPlayers(players);

        // Start the game
        System.out.println("\nStarting Game Setup.");
        game.play();

        // Game finished
        System.out.println("\nGame Over!");
        scanner.close();
    }
}