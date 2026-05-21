import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class PhaseTwo {
    private final List<Player> players;
    private final Scanner scanner;
    private final List<Boolean> isHuman;

    public PhaseTwo(List<Player> players, Scanner scanner, List<Boolean> isHuman) {
        this.players = players;
        this.scanner = scanner;
        this.isHuman = isHuman;
    }

    public void play() {
        int playerCount = players.size();
        int rounds = GameConfig.ROUNDS;
        CheckDeck deck = new CheckDeck();

        System.out.println("\n=== PHASE II: Sell Your Properties ===");
        System.out.println("Checks range from $1,000 to $10,000 (two of each value, 20 total).");
        System.out.println("Each round draws " + playerCount + " new checks from the deck (no reuse).");
        System.out.println("Highest property wins the highest check.\n");

        for (int round = 1; round <= rounds; round++) {
            List<CheckCard> table = deck.deal(playerCount);
            Collections.sort(table, Comparator.reverseOrder());

            System.out.println("--- Round " + round + " of " + rounds + " ---");
            System.out.println("Drawn from deck (" + deck.remainingCount() + " checks remaining after deal)");
            System.out.println("Checks this round (high to low): " + formatChecks(table));

            int[] chosenRanks = new int[playerCount];
            for (int p = 0; p < playerCount; p++) {
                Player player = players.get(p);
                if (isHuman.get(p)) {
                    chosenRanks[p] = humanChooseProperty(player);
                } else {
                    chosenRanks[p] = aiChooseProperty(player);
                    System.out.println(player.getName() + " selects a property.");
                }
            }

            resolveRound(table, chosenRanks);
        }

        printFinalScores();
    }

    private void resolveRound(List<CheckCard> checks, int[] chosenRanks) {
        List<SaleResult> results = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            results.add(new SaleResult(players.get(i), chosenRanks[i]));
        }
        results.sort(Comparator.comparingInt(SaleResult::rank).reversed());

        System.out.println("\nReveal:");
        for (int i = 0; i < results.size(); i++) {
            SaleResult sale = results.get(i);
            CheckCard check = checks.get(i);
            sale.player().addEarnings(check.getValue());
            System.out.printf("  %s played #%d → wins %s%n",
                    sale.player().getName(), sale.rank(), check);
        }
        System.out.println();
    }

    private int humanChooseProperty(Player player) {
        List<PropertyCard> available = new ArrayList<>(player.getProperties());
        Collections.sort(available);

        while (true) {
            System.out.println("\n" + player.getName() + ", choose a property to sell:");
            for (int i = 0; i < available.size(); i++) {
                System.out.println("  " + (i + 1) + ") Property #" + available.get(i).getRank());
            }
            System.out.print("Enter number: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice < 1 || choice > available.size()) {
                    System.out.println("Pick a number from 1 to " + available.size());
                    continue;
                }
                PropertyCard selected = available.get(choice - 1);
                player.getProperties().remove(selected);
                return selected.getRank();
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private int aiChooseProperty(Player player) {
        List<PropertyCard> available = new ArrayList<>(player.getProperties());
        Collections.sort(available);
        // Play highest remaining property when checks are valuable; otherwise mid-high
        int index = available.size() - 1;
        if (Math.random() < 0.25 && available.size() > 1) {
            index = available.size() - 2;
        }
        PropertyCard selected = available.get(index);
        player.getProperties().remove(selected);
        return selected.getRank();
    }

    private void printFinalScores() {
        System.out.println("=== FINAL SCORES ===");
        List<Player> ranked = new ArrayList<>(players);
        ranked.sort(Comparator.comparingInt(Player::getEarnings).reversed());

        for (int i = 0; i < ranked.size(); i++) {
            Player player = ranked.get(i);
            System.out.printf("%d. %s — $%,d%n", i + 1, player.getName(), player.getEarnings());
        }

        Player winner = ranked.get(0);
        boolean tie = ranked.size() > 1 && ranked.get(1).getEarnings() == winner.getEarnings();
        if (tie) {
            System.out.println("\nIt's a tie!");
        } else {
            System.out.println("\n" + winner.getName() + " wins For Sale!");
        }
    }

    private String formatChecks(List<CheckCard> checks) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < checks.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(checks.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    private static final class SaleResult {
        private final Player player;
        private final int rank;

        SaleResult(Player player, int rank) {
            this.player = player;
            this.rank = rank;
        }

        Player player() {
            return player;
        }

        int rank() {
            return rank;
        }
    }
}
