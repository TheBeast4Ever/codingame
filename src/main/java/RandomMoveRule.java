import java.util.Random;

public class RandomMoveRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Random rand = new Random();
        int maxX=board.width,maxY= board.height;
        Action action = Action.move(new Coord(rand.nextInt(maxX+1)-1, rand.nextInt(maxY+1)-1));
        action.setEfficiency(10);
        return action;
    }
}
