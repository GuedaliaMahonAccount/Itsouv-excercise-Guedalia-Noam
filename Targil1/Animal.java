public abstract class Animal implements Seasonable,Comparable{
    protected int weight;
    private Season season;
    protected Color color;

    protected void setSeason(Season season) {
        this.season = season;
    }

    Animal(int weight, Season season, Color color){
        this.weight =weight;
        this.season =season;
        this.color =color;
    }


    @Override
    public Season getCurrentSeason() {
        return season;
    }

    @Override
    public int compareTo(Object o) {
        Animal other = (Animal) o;
        return Integer.compare(this.weight, other.weight);
    }
}
