import java.util.ArrayList;
import java.util.List;

/**
 * Strategic Phase I bidding: values high cards, conserves cash for future rounds,
 * and tracks whether the player still needs properties across the phase.
 */
public class PhaseOneAI {
    private final List<Player> players;

    public PhaseOneAI(List<Player> players) {
        this.players = players;
    }

    public boolean shouldBid(int playerIndex, int currentRound, List<PropertyCard> table,
                             int[] roundBids, boolean[] passed, int cardsRemainingInDeck) {
        Player player = players.get(playerIndex);
        if (!player.canAfford(GameConfig.BID_INCREMENT)) {
            return false;
        }

        int myBid = roundBids[playerIndex];
        int highestBid = highestBid(roundBids);
        int activeCount = countActive(passed);
        int rankIfStop = bidRank(roundBids, playerIndex);

        int topRank = topCardRank(table);
        double cardValue = topRank / (double) GameConfig.PROPERTY_DECK_SIZE;
        int propsStillNeed = GameConfig.ROUNDS - player.getProperties().size();
        int roundsLeftIncludingThis = GameConfig.ROUNDS - currentRound + 1;

        double collectionPace = (double) propsStillNeed / roundsLeftIncludingThis;
        boolean behindOnCards = collectionPace > 1.15;
        boolean lastRound = currentRound == GameConfig.ROUNDS;

        int targetRank = targetRank(cardValue, behindOnCards, lastRound, player.getCash());
        int maxSpend = maxSpendThisRound(cardValue, player.getCash(), roundsLeftIncludingThis,
                propsStillNeed, cardsRemainingInDeck);
        int reserve = reserveCash(roundsLeftIncludingThis, propsStillNeed);

        if (rankIfStop <= targetRank) {
            return false;
        }

        if (myBid + GameConfig.BID_INCREMENT > maxSpend) {
            return false;
        }

        if (highestBid >= maxSpend && myBid < highestBid) {
            return false;
        }

        int costToLead = highestBid - myBid + GameConfig.BID_INCREMENT;
        if (costToLead > player.getCash() - reserve) {
            return false;
        }

        double desire = cardDesire(cardValue, topRank, table);
        double need = collectionNeed(collectionPace, propsStillNeed, lastRound);
        double pressure = auctionPressure(myBid, highestBid, activeCount, roundBids, playerIndex);

        double bidScore = desire * 0.45 + need * 0.35 + pressure * 0.20;
        double threshold = bidThreshold(targetRank, rankIfStop, myBid, highestBid, activeCount);

        return bidScore >= threshold;
    }

    private int targetRank(double cardValue, boolean behindOnCards, boolean lastRound, int cash) {
        int target;
        if (cardValue >= 0.75) {
            target = 1;
        } else if (cardValue >= 0.55) {
            target = 2;
        } else if (cardValue >= 0.35) {
            target = 3;
        } else {
            target = 4;
        }

        if (behindOnCards || lastRound) {
            target = Math.min(4, target + 1);
        }
        if (cash < 8_000) {
            target = Math.min(4, target + 1);
        }
        if (cash < 4_000) {
            target = 4;
        }
        return target;
    }

    private int maxSpendThisRound(double cardValue, int cash, int roundsLeft,
                                  int propsStillNeed, int cardsRemainingInDeck) {
        int reserve = reserveCash(roundsLeft, propsStillNeed);
        int available = Math.max(0, cash - reserve);

        double share = cardValue * 0.5 + 0.15;
        int budget = (int) (available * share / Math.max(1.0, roundsLeft * 0.85));

        int cap = (int) (1000 + cardValue * 11_000);
        if (propsStillNeed >= roundsLeft) {
            cap = Math.max(cap, 3_500);
        }
        if (cardsRemainingInDeck <= GameConfig.PLAYERS) {
            cap = Math.max(cap, 5_500);
        }

        return Math.min(available, Math.max(GameConfig.BID_INCREMENT, Math.min(cap, budget)));
    }

    private int reserveCash(int roundsLeft, int propsStillNeed) {
        return Math.max(0, (roundsLeft - 1) * (1_400 + propsStillNeed * 350));
    }

    private double cardDesire(double cardValue, int topRank, List<PropertyCard> table) {
        int spread = topRank - bottomCardRank(table);
        double spreadBonus = Math.min(0.12, spread / 45.0);
        return Math.min(1.0, cardValue + spreadBonus);
    }

    private double collectionNeed(double collectionPace, int propsStillNeed, boolean lastRound) {
        if (lastRound && propsStillNeed > 0) {
            return 1.0;
        }
        if (collectionPace >= 1.25) {
            return 0.9;
        }
        if (collectionPace >= 1.0) {
            return 0.6;
        }
        if (propsStillNeed <= 1) {
            return 0.2;
        }
        return 0.35;
    }

    private double auctionPressure(int myBid, int highestBid, int activeCount,
                                   int[] roundBids, int playerIndex) {
        if (activeCount <= 1) {
            return 0.1;
        }
        if (myBid >= highestBid && countHigherBids(myBid, roundBids, playerIndex) == 0) {
            return 0.15;
        }
        if (activeCount == 2) {
            return myBid < highestBid ? 0.7 : 0.2;
        }
        return Math.min(1.0, 0.28 + countHigherBids(myBid, roundBids, playerIndex) * 0.22
                + (highestBid - myBid) / 6000.0);
    }

    private double bidThreshold(int targetRank, int rankIfStop, int myBid, int highestBid, int activeCount) {
        double base = 0.5;
        if (rankIfStop > targetRank) {
            base -= 0.1;
        }
        if (myBid < highestBid) {
            base -= 0.07;
        }
        if (activeCount == 2) {
            base -= 0.04;
        }
        return base;
    }

    private int bidRank(int[] roundBids, int playerIndex) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            order.add(i);
        }
        order.sort((a, b) -> {
            int cmp = Integer.compare(roundBids[b], roundBids[a]);
            return cmp != 0 ? cmp : Integer.compare(a, b);
        });
        return order.indexOf(playerIndex) + 1;
    }

    private int countHigherBids(int myBid, int[] roundBids, int playerIndex) {
        int count = 0;
        for (int i = 0; i < roundBids.length; i++) {
            if (i != playerIndex && roundBids[i] > myBid) {
                count++;
            }
        }
        return count;
    }

    private int topCardRank(List<PropertyCard> table) {
        int max = 0;
        for (PropertyCard card : table) {
            max = Math.max(max, card.getRank());
        }
        return max;
    }

    private int bottomCardRank(List<PropertyCard> table) {
        int min = Integer.MAX_VALUE;
        for (PropertyCard card : table) {
            min = Math.min(min, card.getRank());
        }
        return min;
    }

    private int highestBid(int[] roundBids) {
        int max = 0;
        for (int bid : roundBids) {
            max = Math.max(max, bid);
        }
        return max;
    }

    private int countActive(boolean[] passed) {
        int count = 0;
        for (boolean p : passed) {
            if (!p) {
                count++;
            }
        }
        return count;
    }
}
