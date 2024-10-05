package support;

import rules.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionDecider {
    private List<IRule> rules = new ArrayList<IRule>();
    
    public ActionDecider() {
        // Add rules here
        rules.add(new RandomMoveRule());
        rules.add(new RandomMoveFor50FirstRoundsRule());
        rules.add(new FollowOreFoundMoveRule());
        rules.add(new BackToHeadQuarterRule());
        rules.add(new DigHereForOreRule());
        rules.add(new DigHereForPutRadarRule());
        rules.add(new DigHereForPutTrapRule());
        rules.add(new RequestRadarRule());
        rules.add(new RequestTrapRule());
    }
    
    public Action identifyBestActionToPerform(Board board, Entity allyRobot) {
        Action bestPossibleAction = Action.none();
        int bestEfficiency=0;
        for (IRule rule:rules) {
            Action currentAction = rule.evaluateAction(board, allyRobot);
            if (currentAction.efficiency>bestEfficiency) {
                bestPossibleAction = currentAction;
                bestEfficiency = currentAction.efficiency;
            }
        }
        return bestPossibleAction;
    }

    public List<Action> getPossibleActionsSortedByEfficiency(Board board, Entity allyRobot) {
        List<Action> actionsList = new ArrayList<>();
        for (IRule rule:rules) {
            Action currentAction = rule.evaluateAction(board, allyRobot);
            if (currentAction.efficiency>0) {
                actionsList.add(currentAction);
            }
        }
        Collections.reverse(actionsList);
        return actionsList;
    }
}
