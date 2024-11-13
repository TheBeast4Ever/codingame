package support;

import rules.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionDecider {
    private List<IRule> rules = new ArrayList<IRule>();
    
    public ActionDecider() {
        // Add rules here
        //rules.add(new KeepPreviousActionRule());
        rules.add(new DigRandomRule());
        rules.add(new FollowOreFoundWithoutRadarRule());
        rules.add(new FollowOreFoundWithRadarRule());
        rules.add(new RequestRadarRule());
        rules.add(new DigForRadarRule());
        rules.add(new RequestTrapRule());
        rules.add(new DigForTrapRule());
        rules.add(new BackToHeadQuarterRule());
    }

    public List<Action> computeEligibleActionsRankedByEfficiency(Board board, Entity allyRobot) {
        List<Action> actionsList = new ArrayList<>();
        for (IRule rule:rules) {
            Action actionComputed = rule.evaluateAction(board, allyRobot);
            if (actionComputed.efficiencyRate.getValue()>EfficiencyRate.USELESS.getValue()) {
                actionsList.add(actionComputed);
            }
        }
        Collections.sort(actionsList);
        Collections.reverse(actionsList);
        return actionsList;
    }
}
