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
        // rules.add(new RandomMoveFor100FirstRoundsRule());
        rules.add(new KeepPreviousActionRule());
        rules.add(new FollowOreFoundMoveRule());
        rules.add(new BackToHeadQuarterRule());
        rules.add(new DigForOreRule());
        rules.add(new DigHereForPutRadarRule());
        rules.add(new DigHereForPutTrapRule());
        rules.add(new RequestRadarRule());
        // rules.add(new SmartKamikazeRule());
        rules.add(new GoToBestPlaceToPutRadarRule());
        rules.add(new RequestTrapRule());
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
