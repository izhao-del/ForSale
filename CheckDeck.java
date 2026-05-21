import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CheckDeck {
    private final List<CheckCard> remaining;
    private final Random random = new Random();

    public CheckDeck() {
        remaining = new ArrayList<>();
        for (int value = 1_000; value <= 10_000; value += 1_000) {
            remaining.add(new CheckCard(value));
            remaining.add(new CheckCard(value));
        }
        Collections.shuffle(remaining, random);
    }

    public List<CheckCard> deal(int count) {
        if (count > remaining.size()) {
            throw new IllegalStateException("Not enough checks left in deck ("
                    + remaining.size() + " remaining, tried to deal " + count + ")");
        }
        List<CheckCard> dealt = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(remaining.size());
            dealt.add(remaining.remove(index));
        }
        return dealt;
    }

    public int remainingCount() {
        return remaining.size();
    }
}
