public class CheckCard implements Comparable<CheckCard> {
    private final int value;

    public CheckCard(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int compareTo(CheckCard other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.format("$%,d", value);
    }
}
