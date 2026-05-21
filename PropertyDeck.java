import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PropertyDeck {
    private final List<PropertyCard> remaining;
    private final Random random = new Random();

    public PropertyDeck(int size) {
        remaining = new ArrayList<>();
        for (int rank = 1; rank <= size; rank++) {
            remaining.add(new PropertyCard(rank));
        }
        Collections.shuffle(remaining, random);
    }

    public List<PropertyCard> deal(int count) {
        if (count > remaining.size()) {
            throw new IllegalStateException("Not enough cards left in deck ("
                    + remaining.size() + " remaining, tried to deal " + count + ")");
        }
        List<PropertyCard> dealt = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(remaining.size());
            dealt.add(remaining.remove(index));
        }
        return dealt;
    }

    public int remainingCount() {
        return remaining.size();
    }

    public boolean isEmpty() {
        return remaining.isEmpty();
    }
}
