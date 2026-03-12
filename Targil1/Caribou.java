public class Caribou extends Animal {

    Caribou(int weight, Season season) {
        super(weight, season, season == Season.WINTER ? Color.WHITE : Color.BROWN);
    }

    @Override
    public String toString() {
        String s = "Caribou:";
        switch (getCurrentSeason()) {
            case WINTER:
                s = s+ " I am migrating south.";
                break;
            case SUMMER:
                s = s+ " I am migrating north.";
                break;
        }
        s = s+ " My weight is: " + weight + " and my color is: " + color;
        return s;
    }


    @Override
    public void changeSeason() {
        setSeason(getCurrentSeason().next());
        switch (getCurrentSeason()) {
            case WINTER:
                color = Color.WHITE;
                break;
            case SPRING:
                color = Color.BROWN;
                break;
            case SUMMER:
                break;
            case FALL:
                break;
        }
    }
}
