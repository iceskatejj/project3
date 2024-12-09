package core;

import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.List;
import java.util.Random;

public class World {

    // build your own world!
    private static final int START = 0;
    private static final int SEED = 1;
    private static final int PLAY = 2;
    private static final int WIN = 3;
    private static final int LOSE = 4;


    private static final int UP = 10;
    private static final int RIGHT = 11;
    private static final int DOWN = 12;
    private static final int LEFT = 13;

    private static final int SAVE = 14;


    private static final int WIDTH = 80;
    private static final int HEIGHT = 56;
    private static final int ROOM_MIN = 5;
    private static final int ROOM_MAX = 30;

    private Random RANDOM;     // random seed
    private TETile[][] world;  // 2d world of TETiles
    private BSPTree bsp;       // BSPTree that stores rooms and hallways
    private Position player;   // Position of the player
    private Position treasure;  // Position of the treasure

    /**
     * Build the world with the given seed. Randomly decides the number of
     * potential rooms as BSPTree leaves, initializes the BSPTree, and
     * build the 2D world of TETiles of connected rooms and hallways.
     */
    public World(int width, int height, long seed) {
        RANDOM = new Random(seed);
        world = new TETile[width][height]; //jess -3

        // initial the world background
        for (int x = 0; x < world.length; x += 1) {
            for (int y = 0; y < world[0].length; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        //Randomly decides the number of potential rooms as BSPTree leaves,
        //initializes the BSPTree, which created rooms and hallways
        int roomNum = RandomUtils.uniform(RANDOM, ROOM_MAX - ROOM_MIN + 1) + ROOM_MIN;
        bsp = new BSPTree(width, height - 6, roomNum, RANDOM); //jess -3

        //Build the world with rooms and hallways
        List<Room> rooms = bsp.getRooms();
        for (Room r : rooms) {
            drawRoom(r);
        }
        List<Room> hallways = bsp.getHallways();
        for (Room h : hallways) {
            drawRoom(h);
        }

        addPlayerAndTreasure();
    }


    /**
     * Adds a room to the 2d world.
     */
    private void drawRoom(Room room) {
        for (int y = 0; y < room.height(); y++) {
            drawRowOfRoom(room, y);
        }
    }

    /**
     * Draw a row of room to the 2d world of TETiles. If the row is the first or the
     * last row, adds all TETiles of WALL. Otherwise, adds TETiles of WALL to the first
     * and the last tile and TETiles of FLOOR to the rest tiles.
     *
     */
    private void drawRowOfRoom(Room room, int row) {
        int y = room.yOffset() + row;
        int xLast = room.xOffset() + room.width() - 1;

        //draw top and bottom row of room which are walls
        if (row == 0 || row == room.height() - 1) {
            for (int x = room.xOffset(); x <= xLast; x++) {
                drawTile(new Position(x, y), Tileset.WALL);
            }
        } else {
            //draw middle rows of room with leftwall + floor + rightwall

            drawTile(new Position(room.xOffset(), y), Tileset.WALL);
            for (int x = room.xOffset() + 1; x < xLast; x++) {
                drawTile(new Position(x, y), Tileset.FLOOR);
            }
            drawTile(new Position(xLast, y), Tileset.WALL);
        }
    }

    /**
     * Creates player and treasure in different random rooms.
     */
    private void addPlayerAndTreasure() {
        int roomIndex = RandomUtils.uniform(RANDOM, bsp.getRooms().size());
        Room r = bsp.getRooms().get(roomIndex);
        player = new Position(r.xOffset() + r.width() / 2, r.yOffset() + r.height() / 2);
        int roomIndex2 = RandomUtils.uniform(RANDOM, bsp.getRooms().size());
        while (roomIndex2 == roomIndex) {
            roomIndex2 = RandomUtils.uniform(RANDOM, bsp.getRooms().size());
        }
        r = bsp.getRooms().get(roomIndex2);
        treasure = new Position(r.xOffset() + r.width() / 2, r.yOffset() + r.height() / 2);
        drawTile(player, Tileset.AVATAR);
        drawTile(treasure, Tileset.FLOWER);
    }

    /**
     * Returns the target position based on the player's current position and direction.
     */
    private Position target(int direction) {
        switch (direction) {
            case UP: return new Position(player.x, player.y + 1);
            case RIGHT: return new Position(player.x + 1, player.y);
            case DOWN: return new Position(player.x, player.y - 1);
            case LEFT: return new Position(player.x - 1, player.y);
            default: return player;
        }
    }

    /**
     * Moves the player to the adjacent position it's facing to and returns the game status:
     * if the player encounters the treasure, the user wins;
     * if the player encounters its previous track, the user loses;
     * otherwise the game continues.
     */
    public int movePlayer(int direction) {
        int status;
        Position target = target(direction);
        TETile t = world[target.x][target.y];
        status = PLAY;
        if (!t.equals(Tileset.WALL) && !t.equals(Tileset.LOCKED_DOOR)) {
            drawTile(player, Tileset.GRASS);
            drawTile(target, Tileset.AVATAR);
            player = target;
            if (t.equals(Tileset.FLOWER)) {
                status = WIN;
            } else if (t.equals(Tileset.GRASS)) {
                status = LOSE;
            }
        }

        return status;
    }

    /**
     * Draw a tile to the 2d world with the given Position and TETile, except
     * when the current tile is FLOOR and the tile to be added is WALL to
     * prevent overlapping.
     */
    private void drawTile(Position p, TETile t) {
        if (!t.equals(Tileset.WALL) || !world[p.x][p.y].equals(Tileset.FLOOR)) {
            world[p.x][p.y] = t;
        }
    }

    /**
     * Get the world as an 2d array of TETiles.
     */
    public TETile[][] getWorld() {
        return world;
    }

    /**
     * Returns the position of the player.
     */
    public Position getPlayer() {
        return player;
    }


}
