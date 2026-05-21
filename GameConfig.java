public final class GameConfig {
    public static final int PLAYERS = 4;
    public static final int ROUNDS = 5;
    public static final int PROPERTY_DECK_SIZE = 20;
    public static final int STARTING_CASH = 20_000;
    public static final int BID_INCREMENT = 1_000;

    private GameConfig() {}

    public static String formatMoney(int amount) {
        return String.format("$%,d", amount);
    }
}
