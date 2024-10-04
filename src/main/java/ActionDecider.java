import java.util.ArrayList;
import java.util.List;

public class ActionDecider {
    private List<IRule> rules = new ArrayList<IRule>();
    
    public ActionDecider() {
        // Add rules here
        rules.add(new RandomMoveRule());
        rules.add(new BackToHeadQuarterRule());
        rules.add(new DigHereForOreRule());
        rules.add(new DigHereForPutRadarRule());
        rules.add(new RequestRadarRule());
        rules.add(new RequestTrapRule());
    }
    
    public Action identifyBestActionToPerform(Board board, Entity allyRobot) {
        Action bestPossibleAction = Action.none();
        int bestEfficiency=0;
        for (IRule rule:rules) {
            Action currentAction = rule.evaluateAction(board, allyRobot);
            if (currentAction.getEfficiency()>bestEfficiency) {
                bestPossibleAction = currentAction;
                bestEfficiency = currentAction.getEfficiency();
            }
        }
        return bestPossibleAction;
    }
}
