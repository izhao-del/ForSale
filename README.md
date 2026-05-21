# For Sale

A console implementation of the **For Sale** auction game.

## Setup

- **4 players**
- **5 rounds** per phase
- **$20,000** starting cash for Phase I bidding ($1,000 per bid)

## Rules

### Phase I — Property Auctions (20-card deck)

- The deck has 20 property cards ranked **1** (lowest) through **20** (highest).
- **5 rounds**; each round **draws 4 new cards** from the shuffled deck and presents them sorted **low to high**.
- All players bid in one continuous auction for the set: each turn, **pass** or bid **exactly $1,000** more.
- Bidding continues until **all other players have passed**; the last player standing has the highest bid.
- **Highest bidder** wins the **highest** card; 2nd-highest bid gets the 2nd-best card, and so on.
- Only players who bid pay what they added ($1,000 per bid).
- The highest bidder starts the next round’s bidding.
- Each player ends Phase I with **5 properties**.

### Phase II — Selling Properties

- Check cards range from **$1,000** to **$10,000** in $1,000 steps; there are **two of each** value.
- **5 rounds**; each round **draws 4 new checks** from the deck until all 20 are used (sorted high to low).
- Each player chooses one property to sell (each property is used once).
- The highest property wins the highest check.
- After all rounds, the player with the **most money from checks** wins.

## Run

Requires a Java JDK (8+).

```bash
javac *.java
java ForSaleGame
```

Leave a player name blank to add an AI opponent.

## Files

| File | Purpose |
|------|---------|
| `ForSaleGame.java` | Main entry, setup |
| `GameConfig.java` | Players, rounds, starting cash |
| `PhaseOne.java` | Property bidding |
| `PhaseTwo.java` | Property vs checks |
| `PropertyCard.java` | Property rank |
| `CheckCard.java` | Check value |
| `Player.java` | Player state |
| `PropertyDeck.java` | Property draw pile (no reuse) |
| `CheckDeck.java` | Check draw pile (no reuse) |
