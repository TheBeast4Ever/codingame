import rules.EfficiencyRate;
import support.*;

import java.util.*;

public class Player {
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Board board = new Board(in);

        Map<Integer, Action> myPreviousActionsByRobot = new HashMap<Integer, Action>();
        Collection<Coord> emptyHoles = new ArrayList<Coord>();

        // game loop
        while (true) {
            board.update(in);

            ActionDecider decider = new ActionDecider();
            Entity[] robots = board.myTeam.robots.toArray(new Entity[board.myTeam.robots.size()]);
            Map<Integer, Action> actionsToPlay = new HashMap<Integer, Action>();

            for(int i=0;i<5;i++) {
                Entity currentRobot = robots[i];
                if (myPreviousActionsByRobot.containsKey(currentRobot.id)) {
                    currentRobot.previousAction = myPreviousActionsByRobot.get(currentRobot.id);
                    if (currentRobot.previousAction.command.equals("DIG")) {
                        if (currentRobot.item.equals(EntityType.AMADEUSIUM)) {
                            board.hub.pub(new Message("ORE-FOUND", "Ore found (" + currentRobot.pos + ")", currentRobot.pos,1));
                        } else if (currentRobot.item.equals(EntityType.NOTHING)) {
                            emptyHoles.add(currentRobot.pos);
                        }
                    }
                }
            }

            board.myUnsuccessfulHoles = emptyHoles;

            for(int i=0;i<5;i++) {
                Entity currentRobot = robots[i];

                if (currentRobot.isAlive()) {
                    List<Action> possibleActions = decider.computeEligibleActionsRankedByEfficiency(board, currentRobot);
                    Optional<Action> bestActionToPerform = possibleActions.stream()
                            .filter(a->!actionsToPlay.containsValue(a) || a.efficiencyRate== EfficiencyRate.MAXIMUM).findFirst();
                    if (bestActionToPerform.isPresent()) {
                        myPreviousActionsByRobot.put(currentRobot.id, bestActionToPerform.get());
                        actionsToPlay.put(currentRobot.id, bestActionToPerform.get());
                        System.err.println("Robot " + currentRobot.id + " perform action with efficiency " + bestActionToPerform.get().efficiencyRate);
                        System.out.println(bestActionToPerform.get());
                    } else  {
                        System.err.println("Strange for #" + currentRobot.id);
                        myPreviousActionsByRobot.remove(currentRobot.id);
                        System.out.println(Action.none());
                    }
                } else {
                    myPreviousActionsByRobot.remove(currentRobot.id);
                    System.out.println(Action.none());
                }
            }
        }
    }
}
