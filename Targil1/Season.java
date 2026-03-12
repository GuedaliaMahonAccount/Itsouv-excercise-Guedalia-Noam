public enum Season {
    WINTER, SPRING, SUMMER, FALL;

    public Season next() {
        Season[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
