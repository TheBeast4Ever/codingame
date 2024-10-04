import java.util.Scanner;

public class Player {
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Board board = new Board(in);

        // game loop
        while (true) {
            board.update(in);
            for (int i = 0; i < 5; i++) {

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                System.out.println("WAIT"); // WAIT|MOVE x y|DIG x y|REQUEST item
            }
        }
    }
}
