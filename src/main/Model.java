package main;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    public int maxTile;
    public int score;
    private Stack<Tile[][]> previousStates;
    private Stack<Integer> previousScores;
    private boolean isSaveNeeded;

    public Model() {
        maxTile = 0;
        score = 0;
        isSaveNeeded = true;
        previousStates = new Stack<>();
        previousScores = new Stack<>();
        resetGameTiles();
    }

    public void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> list = new ArrayList<>();
        for (Tile[] a : gameTiles) {
            for (Tile b : a) {
                if (b.value == 0)
                    list.add(b);
            }
        }
        return list;
    }

    private void addTile() {
        List<Tile> list = getEmptyTiles();
        if (!list.isEmpty())
            list.get((int) (list.size() * Math.random())).value = Math.random() < 0.9 ? 2 : 4;
    }

    private void saveState(Tile[][] currentGameTiles) {
        previousStates.add(getCopyOfArray(currentGameTiles));
        previousScores.add(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.isEmpty() && !previousScores.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    private Tile[][] getCopyOfArray(Tile[][] original) {
        Tile[][] copyGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                copyGameTiles[i][j] = new Tile(original[i][j].value);
            }
        }
        return copyGameTiles;
    }

    private boolean isDifferent(Tile[][] copy) {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (copy[i][j].value != gameTiles[i][j].value) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canMove() {
        int count = 0;

        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (!gameTiles[i][j].isEmpty() && gameTiles[i][j].value != gameTiles[i + 1][j].value)
                    count++;
            }
        }

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH - 1; j++) {
                if (!gameTiles[i][j].isEmpty() && gameTiles[i][j].value != gameTiles[i][j + 1].value)
                    count++;
            }
        }
        return count != 24;
    }

    public void randomMove() {
        switch (((int) (Math.random() * 100)) % 4) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged() {
        return isDifferent(previousStates.peek());
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        if (hasBoardChanged()) {
            int emptyTiles = 0;
            for (Tile[] a : gameTiles) {
                for (Tile b : a) {
                    if (b.isEmpty()) emptyTiles++;
                }
            }
            MoveEfficiency m = new MoveEfficiency(emptyTiles, score, move);
            rollback();
            return m;
        } else {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        }
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::up));
        queue.offer(getMoveEfficiency(this::down));
        queue.peek().getMove().move();

    }

    public void left() {
        saveState(gameTiles);
        boolean isChange = false;
        Tile[][] copyGameTiles = getCopyOfArray(gameTiles);
        for (Tile[] tiles : gameTiles) {
            isChange = compressTiles(tiles) | mergeTiles(tiles);
        }
        if (!isChange) {
            isChange = isDifferent(copyGameTiles);
        }
        if (isChange) {
            addTile();
        }
    }

    public void right() {
        saveState(gameTiles);
        boolean isChange = false;
        Tile[][] copyGameTiles = getCopyOfArray(gameTiles);
        for (Tile[] tiles : gameTiles) {
            for (int i = 0; i < tiles.length / 2; i++) {
                Tile temp = tiles[i];
                tiles[i] = tiles[tiles.length - 1 - i];
                tiles[tiles.length - 1 - i] = temp;
            }
            isChange = compressTiles(tiles) | mergeTiles(tiles);
            for (int i = 0; i < tiles.length / 2; i++) {
                Tile temp = tiles[i];
                tiles[i] = tiles[tiles.length - 1 - i];
                tiles[tiles.length - 1 - i] = temp;
            }
        }
        if (!isChange) {
            isChange = isDifferent(copyGameTiles);
        }
        if (isChange) {
            addTile();
        }
    }

    public void up() {
        saveState(gameTiles);
        boolean isChange = false;
        Tile[][] reversedGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        Tile[][] copyGameTiles = getCopyOfArray(gameTiles);

        for (int i = FIELD_WIDTH - 1; i >= 0; i--) {
            for (int a = 0; a < FIELD_WIDTH; a++) {
                reversedGameTiles[FIELD_WIDTH - 1 - i][a] = gameTiles[a][i];
            }
        }

        for (Tile[] tiles : reversedGameTiles) {
            isChange = compressTiles(tiles) | mergeTiles(tiles);
        }

        for (int a = 0; a < FIELD_WIDTH; a++) {
            for (int i = FIELD_WIDTH - 1; i >= 0; i--) {
                gameTiles[a][FIELD_WIDTH - 1 - i] = reversedGameTiles[i][a];
            }
        }
        if (!isChange) {
            isChange = isDifferent(copyGameTiles);
        }
        if (isChange) {
            addTile();
        }
    }

    public void down() {
        saveState(gameTiles);
        boolean isChange = false;
        Tile[][] reversedGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        Tile[][] copyGameTiles = getCopyOfArray(gameTiles);

        for (int a = 0; a < FIELD_WIDTH; a++) {
            for (int i = FIELD_WIDTH - 1; i >= 0; i--) {
                reversedGameTiles[a][FIELD_WIDTH - 1 - i] = gameTiles[i][a];
            }
        }

        for (Tile[] tiles : reversedGameTiles) {
            isChange = compressTiles(tiles) | mergeTiles(tiles);
        }

        for (int i = FIELD_WIDTH - 1; i >= 0; i--) {
            for (int a = 0; a < FIELD_WIDTH; a++) {
                gameTiles[FIELD_WIDTH - 1 - i][a] = reversedGameTiles[a][i];
            }
        }
        if (!isChange) {
            isChange = isDifferent(copyGameTiles);
        }
        if (isChange) {
            addTile();
        }
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean isChange = false;
        for (int i = 0; i < tiles.length; i++) {
            if (!tiles[i].isEmpty() && i != 0) {
                Tile temp = tiles[i];
                for (int j = i; j > 0; j--) {
                    if (tiles[j - 1].isEmpty()) {
                        tiles[j] = tiles[j - 1];
                        tiles[j - 1] = temp;
                        isChange = true;
                    }
                }
            }
        }
        return isChange;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isChange = false;
        for (int i = 1; i < tiles.length; i++) {
            if (!tiles[i - 1].isEmpty() && tiles[i - 1].value == tiles[i].value) {
                tiles[i - 1].value += tiles[i].value;
                tiles[i].value = 0;
                score += tiles[i - 1].value;
                if (tiles[i - 1].value > maxTile)
                    maxTile = tiles[i - 1].value;
                compressTiles(tiles);
                isChange = true;
            }
        }
        return isChange;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }
}
