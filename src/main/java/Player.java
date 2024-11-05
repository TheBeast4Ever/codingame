import support.*;

import java.util.*;

public class Player {
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Board board = new Board(in);

        // game loop
        while (true) {
            board.update(in);

            ActionDecider decider = new ActionDecider();
            Entity[] robots = board.myTeam.robots.toArray(new Entity[board.myTeam.robots.size()]);
            Map<Integer, Action> actionsToPlay = new HashMap<Integer, Action>();
            for(int i=0;i<5;i++) {
                Entity currentRobot =  robots[i];
                if (currentRobot.isAlive()) {
                    List<Action> possibleActions = decider.getPossibleActionsSortedByEfficiency(board, currentRobot);
                    //System.err.println("Robot " + currentRobot.id + " has " + possibleActions.size() + " possible actions");
                    Optional<Action> bestActionToPerform = possibleActions.stream().filter(a->!actionsToPlay.containsValue(a) || a.efficiency>=90).findFirst();
                    if (bestActionToPerform.isPresent()) {
                        if (currentRobot.action == null || bestActionToPerform.get().efficiency>currentRobot.action.efficiency) {
                            currentRobot.action = bestActionToPerform.get();
                        }
                        actionsToPlay.put(currentRobot.id, bestActionToPerform.get());
                        System.err.println("Robot " + currentRobot.id + " perform action with efficiency " + bestActionToPerform.get().efficiency);
                        System.out.println(bestActionToPerform.get());
                    } else  {
                        System.err.println("Strange for #" + currentRobot.id);
                        System.out.println(Action.none());
                    }
                } else {
                    System.out.println(Action.none());
                }
            }
        }
    }
}
