import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PhaseOne {
    private final List<Player> players;
    private final Scanner scanner;
    private final List<Boolean> isHuman;
    private final PhaseOneAI ai;
    private int startingBidderIndex;
    private int currentRound;
    private List<PropertyCard> currentTable;
    private int cardsRemainingInDeck;

    public PhaseOne(List<Player> players, Scanner scanner, List<Boolean> isHuman) {
        this.players = players;
        this.scanner = scanner;
        this.isHuman = isHuman;
        this.ai = new PhaseOneAI(players);
        this.startingBidderIndex = 0;
    }

    public void play() {
        int playerCount = GameConfig.PLAYERS;
        int rounds = GameConfig.ROUNDS;
        PropertyDeck deck = new PropertyDeck(GameConfig.PROPERTY_DECK_SIZE);

        System.out.println("\n=== PHASE I: Property Auctions ===");
        System.out.println("Deck: " + GameConfig.PROPERTY_DECK_SIZE + " unique properties (rank 1 = lowest, "
                + GameConfig.PROPERTY_DECK_SIZE + " = highest)");
        System.out.println("Players: " + playerCount + " · Rounds: " + rounds);
        System.out.println("Each round: 4 cards are presented. Players bid for the highest card.");
        System.out.println("Highest bidder wins the top card; others receive the rest by bid rank.");
        System.out.println("Starting cash: " + GameConfig.formatMoney(GameConfig.STARTING_CASH));
        System.out.println("Each bid is exactly " + GameConfig.formatMoney(GameConfig.BID_INCREMENT)
                + ". Bidding continues until all other players pass.\n");

        for (int round = 1; round <= rounds; round++) {
            currentRound = round;
            List<PropertyCard> table = deck.deal(playerCount);
            Collections.sort(table);
            currentTable = table;
            cardsRemainingInDeck = deck.remainingCount();

            System.out.println("--- Round " + round + " of " + rounds + " ---");
            System.out.println("Drawn from deck (" + cardsRemainingInDeck + " cards remaining after deal)");
            System.out.println("Cards presented (low to high): " + formatCards(table));
            System.out.println("Bidding for these 4 cards — highest bid wins the highest card.\n");

            int[] roundBids = new int[playerCount];
            int topBidder = runRoundBidding(roundBids);
            assignCardsByBidRank(table, roundBids);
            startingBidderIndex = topBidder;
        }

        if (!deck.isEmpty()) {
            throw new IllegalStateException("Deck should be empty after Phase I");
        }
        printPropertySummary();
    }

    private int runRoundBidding(int[] roundBids) {
        boolean[] passed = new boolean[players.size()];
        int current = startingBidderIndex;

        while (activePlayers(passed) > 1) {
            if (passed[current]) {
                current = nextPlayer(current);
                continue;
            }

            Player player = players.get(current);
            boolean bid;
            if (isHuman.get(current)) {
                System.out.printf("%n%s — cash: %s | Your bid: %s | Highest bid: %s%n",
                        player.getName(), GameConfig.formatMoney(player.getCash()),
                        GameConfig.formatMoney(roundBids[current]),
                        GameConfig.formatMoney(highestBid(roundBids)));
                printOtherBids(roundBids, current);
                bid = readBidOrPass(player);
            } else {
                bid = ai.shouldBid(current, currentRound, currentTable, roundBids, passed,
                        cardsRemainingInDeck);
            }

            if (!bid) {
                passed[current] = true;
                System.out.println(player.getName() + " passes.");
            } else {
                int amount = GameConfig.BID_INCREMENT;
                player.spendCash(amount);
                roundBids[current] += amount;
                System.out.printf("%s bids %s → total bid %s%n",
                        player.getName(), GameConfig.formatMoney(amount),
                        GameConfig.formatMoney(roundBids[current]));
            }

            current = nextPlayer(current);
        }

        return lastActivePlayer(passed);
    }

    private int activePlayers(boolean[] passed) {
        int count = 0;
        for (boolean p : passed) {
            if (!p) {
                count++;
            }
        }
        return count;
    }

    private int lastActivePlayer(boolean[] passed) {
        for (int i = 0; i < passed.length; i++) {
            if (!passed[i]) {
                return i;
            }
        }
        return startingBidderIndex;
    }

    private int highestBid(int[] roundBids) {
        int max = 0;
        for (int bid : roundBids) {
            max = Math.max(max, bid);
        }
        return max;
    }

    private void assignCardsByBidRank(List<PropertyCard> table, int[] roundBids) {
        List<Integer> ranking = rankPlayersByBid(roundBids);
        List<PropertyCard> sorted = new ArrayList<>(table);
        Collections.sort(sorted);

        System.out.println("\n--- Round results (by bid rank) ---");
        for (int i = 0; i < ranking.size(); i++) {
            int playerIndex = ranking.get(i);
            PropertyCard card = sorted.get(sorted.size() - 1 - i);
            Player player = players.get(playerIndex);
            player.addProperty(card);
            String place = i == 0 ? "1st (highest bid)" : (i + 1) + "th";
            System.out.printf("  %s — %s — bid %s → property #%d%n",
                    place, player.getName(), GameConfig.formatMoney(roundBids[playerIndex]),
                    card.getRank());
        }
        System.out.println();
    }

    private List<Integer> rankPlayersByBid(int[] roundBids) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            indices.add(i);
        }
        indices.sort((a, b) -> {
            int cmp = Integer.compare(roundBids[b], roundBids[a]);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(a, b);
        });
        return indices;
    }

    private void printOtherBids(int[] roundBids, int currentPlayer) {
        System.out.println("Other bids this round:");
        for (int i = 0; i < players.size(); i++) {
            if (i != currentPlayer) {
                System.out.println("  " + players.get(i).getName() + ": "
                        + GameConfig.formatMoney(roundBids[i]));
            }
        }
    }

    private boolean readBidOrPass(Player player) {
        while (true) {
            System.out.print("Bid " + GameConfig.formatMoney(GameConfig.BID_INCREMENT)
                    + " more? (1 / bid / yes, or 0 / pass): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (isPassInput(input)) {
                return false;
            }
            if (isBidInput(input)) {
                if (!player.canAfford(GameConfig.BID_INCREMENT)) {
                    System.out.println("You need " + GameConfig.formatMoney(GameConfig.BID_INCREMENT)
                            + " but only have " + GameConfig.formatMoney(player.getCash()) + ".");
                    continue;
                }
                return true;
            }
            System.out.println("Enter 1, bid, or yes to bid; 0 or pass to pass.");
        }
    }

    private boolean isPassInput(String input) {
        return input.equals("0") || input.equals("pass") || input.equals("p")
                || input.equals("no") || input.equals("n");
    }

    private boolean isBidInput(String input) {
        return input.equals("1") || input.equals("bid") || input.equals("b")
                || input.equals("yes") || input.equals("y");
    }

    private int nextPlayer(int index) {
        return (index + 1) % players.size();
    }

    private void printPropertySummary() {
        System.out.println("\n--- Phase I Complete: Properties Won ---");
        for (Player player : players) {
            List<PropertyCard> props = new ArrayList<>(player.getProperties());
            Collections.sort(props);
            System.out.println(player.getName() + ": " + formatCards(props)
                    + " (cash left: " + GameConfig.formatMoney(player.getCash()) + ")");
        }
    }

    private String formatCards(List<PropertyCard> cards) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(cards.get(i).getRank());
        }
        sb.append("]");
        return sb.toString();
    }
}
