import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private int cash;
    private int earnings;
    private final List<PropertyCard> properties = new ArrayList<>();

    public Player(String name, int startingCash) {
        this.name = name;
        this.cash = startingCash;
        this.earnings = 0;
    }

    public String getName() {
        return name;
    }

    public int getCash() {
        return cash;
    }

    public int getEarnings() {
        return earnings;
    }

    public List<PropertyCard> getProperties() {
        return properties;
    }

    public void addProperty(PropertyCard card) {
        properties.add(card);
    }

    public void spendCash(int amount) {
        cash -= amount;
    }

    public void addEarnings(int amount) {
        earnings += amount;
    }

    public boolean canAfford(int bid) {
        return bid >= 0 && bid <= cash;
    }

    @Override
    public String toString() {
        return name;
    }
}
