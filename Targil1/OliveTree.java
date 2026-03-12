public class OliveTree extends Tree {


    OliveTree(int height, Season season) {
        super(height, season, Color.GREEN);
    }

    @Override
    public String toString() {
        String s = "Olive tree.";
        if (season == Season.FALL) {
            s = s+ " I give fruit.";
        }
        s = s+ " My height is: " + height + " and my color is: " + leavesColor;
        return s;
    }
    @Override
    public void changeSeason() {
        season = season.next();
        switch (season) {
            case WINTER:
                height = height + 5;
                break;
            case SPRING:
                height = height + 10;
                break;
            case SUMMER:
                height = height + 10;
                break;
            case FALL:
                height = height + 5;
                break;
        }
    }
}
