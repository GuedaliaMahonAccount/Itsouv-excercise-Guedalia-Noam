public class FigTree extends Tree {


    FigTree(int height, Season season) {
        super(height, season, season == Season.WINTER ? null :
                               season == Season.FALL ? Color.YELLOW : Color.GREEN);
    }



    @Override
    public String toString() {
        String s = "Fig tree.";
        if (season == Season.SUMMER) {
            s = s+ " I give fruit.";
        }
        s += " My height is: " + height;
        if (season == Season.WINTER) {
            s = s+ " and I have no leaves";
        } else {
            s = s+ " and my color is: " + leavesColor;
        }
        return s;
    }

    @Override
    public void changeSeason() {
        season = season.next();
        switch (season) {
            case WINTER:
                height = height + 20;
                leavesColor = null;
                break;
            case SPRING:
                height = height + 30;
                leavesColor = Color.GREEN;
                break;
            case SUMMER:
                height = height + 30;
                break;
            case FALL:
                height = height + 20;
                leavesColor = Color.YELLOW;
                break;
        }
    }
}
