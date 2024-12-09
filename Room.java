package core;

public class Room {
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;

    private int[] xCoords; // contains bounds of room on x-axis
    private int[] yCoords; // contains bounds of room on y-axis
    private int[] center; // contains x and y coords of center
    //private Position player;

    Room(int x, int y, int w, int h) {
        xOffset = x;
        yOffset = y;
        width = w;
        height = h;
        xCoords = new int[] {x, x + w};
        yCoords = new int[] {y, y + w};
        center = new int[] {(xCoords[1] - xCoords[0]) / 2, (yCoords[1] - yCoords[0]) / 2};
    }

    public int xOffset() {
        return xOffset;
    }

    public int yOffset() {
        return yOffset;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
