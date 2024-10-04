import java.util.Scanner;

public class Player {
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Board board = new Board(in);

        // game loop
        while (true) {
            board.update(in);
            int nbofAliveRobots = board.myTeam.robots.size();
            int nbOfDeadRobots = 5-nbofAliveRobots;
            ActionDecider decider = new ActionDecider();
            board.myTeam.robots.stream().filter(currentRobot-> currentRobot.isAlive()).forEach(currentRobot -> {
                System.out.println(decider.identifyBestActionToPerform(board, currentRobot));
            });
            board.myTeam.robots.stream().filter(currentRobot-> !currentRobot.isAlive()).forEach(currentRobot -> {
                System.out.println(Action.none());
            });
        }
    }
}
