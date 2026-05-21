public class PropertyCard implements Comparable<PropertyCard> {
    private final int rank;

    public PropertyCard(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public int compareTo(PropertyCard other) {
        return Integer.compare(this.rank, other.rank);
    }

    @Override
    public String toString() {
        return String.valueOf(rank);
    }
}
