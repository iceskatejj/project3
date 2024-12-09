import core.AutograderBuddy;
import edu.princeton.cs.algs4.StdDraw;
import org.junit.jupiter.api.Test;
import tileengine.TERenderer;
import tileengine.TETile;

import static org.junit.jupiter.api.Assertions.fail;

public class WorldGenTests {
    @Test
    public void basicTest() {
        // put different seeds here to test different worlds
        TETile[][] tiles = AutograderBuddy.getWorldFromInput("n1234567890123456789s");

        TERenderer ter = new TERenderer();
        ter.initialize(tiles.length, tiles[0].length);
        ter.renderFrame(tiles);
        StdDraw.pause(5000); // pause for 5 seconds so you can see the output
    }

    @Test
    public void testArray() {
        TETile[][] tiles1 = AutograderBuddy.getWorldFromInput("n5197880843569031643s");
        TETile[][] tiles2 = AutograderBuddy.getWorldFromInput("n455857754086099036s");

        boolean same = true;
        for (int x = 0; x < tiles1.length; x++) {
            for (int y = 0; y < tiles1[0].length; y++) {
                if (tiles1[x][y].equals(tiles2[x][y])) {
                    same = false;
                }
            }
        }
        if (same) {
            fail();
        }

        TERenderer ter1 = new TERenderer();
        ter1.initialize(tiles1.length, tiles1[0].length);
        ter1.renderFrame(tiles1);
        StdDraw.pause(1000);

        TERenderer ter2 = new TERenderer();
        ter2.initialize(tiles2.length, tiles2[0].length);
        ter2.renderFrame(tiles2);
        StdDraw.pause(1000);


    }

    @Test
    public void basicInteractivityTest() {
        // TODO: write a test that uses an input like "n123swasdwasd"
    }

    @Test
    public void basicSaveTest() {
        // TODO: write a test that calls getWorldFromInput twice, with "n123swasd:q" and with "lwasd"
    }
}
