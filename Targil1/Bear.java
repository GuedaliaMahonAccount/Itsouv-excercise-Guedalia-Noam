public class Bear extends Animal {

    Bear(int weight, Season season) {
        super(weight, season, Color.BROWN);
    }
    @Override
    public String toString() {
        String s = "Bear.";
        if (getCurrentSeason() == Season.WINTER) {
            s = s+ " I am sleeping.";
        }
        s = s+ " My weight is: " + weight + " and my color is: " + color;
        return s;
    }

    @Override
    public void changeSeason() {
        setSeason(getCurrentSeason().next());
        switch (getCurrentSeason()) {
            case WINTER:
                weight = (int) Math.round(weight * 0.8);
                break;
            case SPRING:
                weight = (int) Math.round(weight * 0.75);
                break;
            case SUMMER:
                weight = (int) Math.round(weight * 4.0 / 3.0);
                break;
            case FALL:
                weight = (int) Math.round(weight * 1.25);
                break;
        }
    }
}
