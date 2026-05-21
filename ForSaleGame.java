import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ForSaleGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║           FOR SALE                   ║");
        System.out.println("║   Bid on properties, sell for cash   ║");
        System.out.println("╚══════════════════════════════════════╝");

        System.out.println("\n4 players · 5 rounds · "
                + GameConfig.formatMoney(GameConfig.STARTING_CASH) + " starting cash\n");

        List<Player> players = new ArrayList<>();
        List<Boolean> isHuman = new ArrayList<>();

        for (int i = 0; i < GameConfig.PLAYERS; i++) {
            System.out.print("Player " + (i + 1) + " name (blank = AI): ");
            String name = scanner.nextLine().trim();
            boolean human = !name.isEmpty();
            if (!human) {
                name = "AI " + (i + 1);
            }
            players.add(new Player(name, GameConfig.STARTING_CASH));
            isHuman.add(human);
        }

        PhaseOne phaseOne = new PhaseOne(players, scanner, isHuman);
        phaseOne.play();

        PhaseTwo phaseTwo = new PhaseTwo(players, scanner, isHuman);
        phaseTwo.play();

        scanner.close();
    }
}
