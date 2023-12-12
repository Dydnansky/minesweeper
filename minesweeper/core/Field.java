package minesweeper.core;

import java.util.Formatter;
import java.util.Random;

/**
 * Field represents playing field and game logic.
 */
public class Field {
    /**
     * Playing field tiles.
     */
    private final Tile[][] tiles;

    /**
     * Field row count. Rows are indexed from 0 to (rowCount - 1).
     */
    private final int rowCount;

    /**
     * Column count. Columns are indexed from 0 to (columnCount - 1).
     */
    private final int columnCount;

    /**
     * Mine count.
     */
    private final int mineCount;

    /**
     * Game state.
     */
    private GameState state = GameState.PLAYING;

    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * Constructor.
     *
     * @param rowCount    row count
     * @param columnCount column count
     * @param mineCount   mine count
     */
    public Field(int rowCount, int columnCount, int mineCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.mineCount = mineCount;
        tiles = new Tile[rowCount][columnCount];

        //generate the field content
        generate();
//        System.out.println(this);
//
//        openTile(5, 5);
////        openTile(4, 3);
////        openTile(3, 3);
////        openTile(5, 3);
////        markTile(7, 4);
//        System.out.println(this);


    }

    public int getRemainingMineCount(){
        return mineCount-getNumberOf(Tile.State.MARKED);

    }

    public String toString() {
        Formatter f = new Formatter();
        f.format("%n");
        f.format("%3s", "");
        for (int i = 0; i < columnCount; i++) {
            f.format("%4s", i);
        }
        f.format("%n");

        for (int i = 0; i < rowCount; i++) {
            f.format("%3s", (char) (i + 65));
            for (int j = 0; j < columnCount; j++) {
                f.format("%4s", tiles[i][j]);
            }
            f.format("%n");
        }
        return f.toString();
    }


    /**
     * Opens tile at specified indeces.
     *
     * @param row    row number
     * @param column column number
     */
    public void openTile(int row, int column) {
        Tile tile = tiles[row][column];
        if (tile.getState() == Tile.State.CLOSED) {
            tile.setState(Tile.State.OPEN);

            if (tile instanceof Mine) {
                state = GameState.FAILED;
                return;
            }
            openAdjacentTiles(row, column);
            if (isSolved()) {
                state = GameState.SOLVED;
                return;
            }
        }
    }

    private void openAdjacentTiles(int row, int column) {
        if (tiles[row][column] instanceof Clue) {
            if (((Clue) tiles[row][column]).getValue() == 0) {
                openLeft(row, column);
                openRight(row, column);
                openUp(row, column);
                openDown(row, column);
            }
        }
    }

    private void openDown(int row, int column) {
        if (row < rowCount - 1) {
            if (tiles[row + 1][column].getState() == Tile.State.CLOSED) {
                tiles[row + 1][column].setState(Tile.State.OPEN);
                openAdjacentTiles(row + 1, column);
            }
        }
    }

    private void openRight(int row, int column) {
        if (column < columnCount - 1) {
            if (tiles[row][column + 1].getState() == Tile.State.CLOSED) {
                tiles[row][column + 1].setState(Tile.State.OPEN);
                openAdjacentTiles(row, column + 1);
            }
        }
    }

    private void openUp(int row, int column) {
        if (row > 0) {
            if (tiles[row - 1][column].getState() == Tile.State.CLOSED) {
                tiles[row - 1][column].setState(Tile.State.OPEN);
                openAdjacentTiles(row - 1, column);
            }
        }
    }

    private void openLeft(int row, int column) {
        if (column > 0) {
            if (tiles[row][column - 1].getState() == Tile.State.CLOSED) {
                tiles[row][column - 1].setState(Tile.State.OPEN);
                openAdjacentTiles(row, column - 1);
            }
        }
    }

    /**
     * Marks tile at specified indeces.
     *
     * @param row    row number
     * @param column column number
     */
    public void markTile(int row, int column) {
        Tile tile = tiles[row][column];
        if (tile.getState() == Tile.State.CLOSED) {
            tile.setState(Tile.State.MARKED);
        } else if (tile.getState() == Tile.State.MARKED) {
            tile.setState(Tile.State.CLOSED);
        }
    }

    /**
     * Generates playing field.
     */
    private void generate() {
        generateMines();
        generateClues();
    }

    private void generateClues() {
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                if (tiles[row][column] == null) {
                    int countOfNeighborMines = countAdjacentMines(row, column);
                    tiles[row][column] = new Clue(countOfNeighborMines);
                }
            }
        }
    }

    private void generateMines() {
        Random random = new Random();
        int numberOfInsertedMines = 0;
        while (numberOfInsertedMines < mineCount) {
            int randomRow = random.nextInt(rowCount);
            int randomCol = random.nextInt(columnCount);
            if (tiles[randomRow][randomCol] == null) {
                tiles[randomRow][randomCol] = new Mine();
                numberOfInsertedMines++;
            }
        }
    }

    /**
     * Returns true if game is solved, false otherwise.
     *
     * @return true if game is solved, false otherwise
     */
    private boolean isSolved() {
        int c = getNumberOf(Tile.State.OPEN);
        if (((columnCount * rowCount) - c) == mineCount) {
            return true;
        }
        return false;
    }

    /**
     * Returns number of adjacent mines for a tile at specified position in the field.
     *
     * @param row    row number.
     * @param column column number.
     * @return number of adjacent mines.
     */
    private int countAdjacentMines(int row, int column) {
        int count = 0;
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            int actRow = row + rowOffset;
            if (actRow >= 0 && actRow < rowCount) {
                for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                    int actColumn = column + columnOffset;
                    if (actColumn >= 0 && actColumn < columnCount) {
                        if (tiles[actRow][actColumn] instanceof Mine) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    public GameState getState() {
        return state;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getMineCount() {
        return mineCount;
    }

    public Tile getTile(int row, int column) {
        return tiles[row][column];
    }

    private int getNumberOf(Tile.State state) {
        int count = 0;
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                if (tiles[r][c].getState() == state) {
                    count++;
                }
            }
        }
        return count;
    }
}
