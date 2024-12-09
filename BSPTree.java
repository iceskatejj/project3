package core;

import java.util.*;

import utils.RandomUtils;

public class BSPTree {
    private static final int ROOM_SPLIT_MIN = 8;
    private static final int ROOM_SIZE_MIN = 5;

    private Random RANDOM;              // random seed
    private Leaf root;                  // the root leaf
    private ArrayList<Room> rooms;     // list of rooms
    private ArrayList<Room> hallways;  // list of hallways

    /**
     * Generates the BSPTree and initializes all member variables.
     * Randomly split the area into given number of different pieces (leaves)
     * for potiential rooms. Create rooms and hallways corresponding to each leaf.
     */
    BSPTree(int width, int height, int leafNum, Random random) {
        //initialize
        RANDOM = random;
        rooms = new ArrayList<>();
        hallways = new ArrayList<>();
        ArrayDeque<Leaf> leafDeque = new ArrayDeque<>();

        //Splits the root to leaves with breadth first search order.
        root = new Leaf(0, 0, width, height);
        leafDeque.add(root);
        leafNum--;
        while (leafNum > 0) {
            Leaf currLeaf = leafDeque.remove();
            if (split(currLeaf)) {
                leafDeque.add(currLeaf.left);
                leafDeque.add(currLeaf.right);
                leafNum--;
            }
        }

        //create rooms and hallways based on BSPtree
        createRooms(root);
        createHallways(root);
    }

    /**
     * Stores the space of the leaf, the split direction of its children leaves, the
     * children leaves and the randomly generated room.
     */
    class Leaf {
        int x;          // xOffset
        int y;          // yOffset
        int w;          // width
        int h;          // height
        int direction;  // 0: horizontal, 1: vertical, -1:initial
        Room room;
        Leaf left;
        Leaf right;

        Leaf(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            direction = -1;
            room = null;
            left = null;
            right = null;
        }

        /**
         * Creates a room. The size and position of the room is randomly decided as
         * long as it is within the range.
         */
        void createRoom() {

            int width = ROOM_SIZE_MIN;
            int height = ROOM_SIZE_MIN;
            if (w > ROOM_SPLIT_MIN) {
                width = RandomUtils.uniform(RANDOM, w - ROOM_SIZE_MIN - 2) + ROOM_SIZE_MIN;
            }
            if (h > ROOM_SPLIT_MIN) {
                height = RandomUtils.uniform(RANDOM, h - ROOM_SIZE_MIN - 2) + ROOM_SIZE_MIN;
            }
            // The xOffset and yOffset of the room are randomly decided and keep the
            // room within the range of the leaf but also intersecting the center.
            int xOffset, yOffset;

            if (width > w / 2) {
                xOffset = x + RandomUtils.uniform(RANDOM, w - width - 2) + 1;
            } else {
                xOffset = (x + w / 2 - 1) - RandomUtils.uniform(RANDOM, width / 2);
            }

            if (height > h / 2) {
                yOffset = y + RandomUtils.uniform(RANDOM, h - height - 2) + 1;
            } else {
                yOffset = (y + h / 2 - 1) - RandomUtils.uniform(RANDOM, height / 2);
            }
            this.room = new Room(xOffset, yOffset, width, height);
        }
    }

    /**
     * Splits the leaf into two children leaves. Randomly decides the split direction
     * and the width / height of the children leaves. Return true if it is split or
     * false if not.
     */
    private boolean split(Leaf leaf) {
        // leaf is already split
        if (leaf.left != null || leaf.right != null) {
            return false;
        }
        // leaf is too small to split
        if (leaf.w < ROOM_SPLIT_MIN * 2 && leaf.h < ROOM_SPLIT_MIN * 2) {
            return false;
        }
        // decides split direction (horizontal or vertical)
        //int direction = RandomUtils.uniform(RANDOM) < 0.5 ? 0 : 1;
        int direction = leaf.w > leaf.h ? 0 : 1;
        int length = direction == 0 ? leaf.w : leaf.h;
        if (length < ROOM_SPLIT_MIN * 2) {
            direction = (direction + 1) % 2;
            length = direction == 0 ? leaf.w : leaf.h;
        }
        leaf.direction = direction;
        // splits into two leaves with random size no less than the ROOM_SPLIT_MIN
        int split = RandomUtils.uniform(RANDOM, length - ROOM_SPLIT_MIN * 2 + 1) + ROOM_SPLIT_MIN;
        if (direction == 0) {
            leaf.left = new Leaf(leaf.x, leaf.y, split, leaf.h);
            leaf.right = new Leaf(leaf.x + split - 1, leaf.y, leaf.w - split, leaf.h);
        } else {
            leaf.left = new Leaf(leaf.x, leaf.y, leaf.w, split);
            leaf.right = new Leaf(leaf.x, leaf.y + split - 1, leaf.w, leaf.h - split);
        }
        return true;
    }

    /**
     * Recursively creates rooms of the leaves without children. Add the rooms into a list.
     */
    private void createRooms(Leaf leaf) {
        if (leaf.left == null && leaf.right == null) {
            leaf.createRoom();
            if (leaf.room != null) {
                rooms.add(leaf.room);
            }
            return;
        }
        createRooms(leaf.left);
        createRooms(leaf.right);
    }

    /**
     * Recursively creates hallways that connect each two leaves (potential rooms)of the
     * same parent. Each hallway starts from the center of a leaf and goes to the center
     * of the other leaf. Add the hallways into a list.
     */
    private void createHallways(Leaf leaf) {
        if (leaf.left == null && leaf.right == null) {
            return;
        }
        if (leaf.direction == 0) {
            //create horizontal hallway
            int xStart = leaf.left.x + leaf.left.w / 2 - 1;
            int xEnd = leaf.right.x + leaf.right.w / 2 + 1;
            getHallways().add(new Room(xStart, leaf.y + leaf.h / 2 - 1, xEnd - xStart + 1, 3));
        } else {
            //create vertical hallway
            int yStart = leaf.left.y + leaf.left.h / 2 - 1;
            int yEnd = leaf.right.y + leaf.right.h / 2 + 1;
            getHallways().add(new Room(leaf.x + leaf.w / 2 - 1, yStart, 3, yEnd - yStart + 1));
        }
        createHallways(leaf.left);
        createHallways(leaf.right);
    }

    /**
     * Returns rooms.
     */
    public List<Room> getRooms() {
        return rooms;
    }

    /**
     * Returns hallways.
     */
    public List<Room> getHallways() {
        return hallways;
    }

}
