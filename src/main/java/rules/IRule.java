package rules;

import support.Action;
import support.Board;
import support.Entity;

public interface IRule {
    Action evaluateAction(Board board, Entity currentRobot);

    String getMessage(Entity currentRobot);
}
