public interface IRule {
    Action evaluateAction(Board board, Entity currentRobot);
}
