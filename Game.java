package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Random;
public class Game {
    //private enum Status {START, SEED, PLAY};
    private static final int START = 0;
    private static final int SEED = 1;
    private static final int PLAY = 2;
    private static final int WIN = 3;
    private static final int LOSE = 4;

    private static final int QUIT = 5;

    private static final int UP = 10;
    private static final int RIGHT = 11;
    private static final int DOWN = 12;
    private static final int LEFT = 13;
    private static final int SAVE = 14;


    private static final int WIDTH = 80;
    private static final int HEIGHT = 56;

    private Font TITLE_FONT = new Font("Monaco", Font.BOLD, 30);
    private Font SUBTITLE_FONT = new Font("Monaco", Font.BOLD, 20);
    private Font REGULAR_FONT = new Font("Monaco", Font.BOLD, 16);

    private TERenderer ter;
    private int status;

    private int moveStatus;
    private StringBuilder inputs;
    private StringBuilder seedInput;
    private long seed;

    private Boolean continueFromLoadedFile;
    private World myWorld;
    private Random RANDOM;

    private long prevFrameTimestamp;

    public Game() {
        ter = new TERenderer();
    }

    /**
     * * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        char ch;

        // Initialize StdDraw
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.enableDoubleBuffering();
        ter.initialize(WIDTH, HEIGHT, 0, 2);

        // Restart to the main menu when a game ends.
        // Only exit program (directly) when the users enter "Q".

        while (true) {
            //initialize game
            initialize();
            drawGameMenu();

            while (status != PLAY) {
                if (StdDraw.hasNextKeyTyped()) {
                    ch = Character.toUpperCase(StdDraw.nextKeyTyped());
                    parseMenuChoice(ch, true);
                }
            }

            if (!continueFromLoadedFile) {
                //build world and draw world
                myWorld = new World(WIDTH, HEIGHT, seed);
            }

            while (status == PLAY) {
                if (shouldRenderNewFrame()) {
                    drawWorld();
                }
                if (StdDraw.hasNextKeyTyped()) {
                    ch = Character.toUpperCase(StdDraw.nextKeyTyped());
                    parseMovement(ch, true);
                }
            }

            // draw game result and Let users press any key to continue
            while (true) {
                if (status == WIN || status == LOSE) {
                    if (shouldRenderNewFrame()) {
                        drawResult();
                    }
                }

                if (StdDraw.hasNextKeyTyped()) {
                    ch = Character.toUpperCase(StdDraw.nextKeyTyped());
//                    if (ch == 'Q') {
//                        System.exit(0);
//                    }
                    break;
                }
            }
        }
    }

    /**
     * Initializes all member variables and resets the game.
     */
    private void initialize() {
        status = START;
        moveStatus = -1;
        seedInput = new StringBuilder("");
        inputs = new StringBuilder("");
        seed = -1;
        myWorld = null;
        continueFromLoadedFile = false;
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     */
    public TETile[][] playWithInputString(String input, boolean replay) {
        char ch;
        int index = 0;
        System.out.println("Input String: " + input);
        initialize();
        while (input != null && index < input.length() && status != PLAY) {
            ch = Character.toUpperCase(input.charAt(index));
            index += 1;
            parseMenuChoice(ch, false);
        }

        if (!continueFromLoadedFile) {
            myWorld = new World(WIDTH, HEIGHT, seed);
        }

        while (input != null && index < input.length() && status == PLAY) {
            if (replay) {
                drawWorld();
                // try to wait to render frames
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            ch = Character.toUpperCase(input.charAt(index));
            index += 1;
            parseMovement(ch, false);
        }

        return myWorld.getWorld();
    }


    /**
     * Handles inputs from the main menu and the prompt menu.
     *
     */
    private void parseMenuChoice(char ch, boolean keyIn) {

        // Prompts the users to enter the seed
        // Displays the seedInput the users has entered
        // Initializes the world with the seed
        // Loads the game from file
        // Exit program
        if (status == START && ch == 'N') {
            inputs.append(ch);
            status = SEED;
            if (keyIn) {
                drawPromptMenu();
            }
        } else if (status == SEED && Character.isDigit(ch)) {
            seedInput.append(ch);
            inputs.append(ch);
            if (keyIn) {
                drawPromptMenu();
            }
        } else if (status == SEED && ch == 'S') {
            inputs.append(ch);
            seed = parseInputToSeed(seedInput.toString());
            status = PLAY;
        } else if (status == START && ch == 'L') {
            String loadInputs = loadFile();
            TETile[][] w = playWithInputString(loadInputs, false);
            continueFromLoadedFile = true;
            status = PLAY;
        } else if (status == START && ch == 'R') {
            String loadInputs = loadFile(); // replay from file
            TETile[][] w = playWithInputString(loadInputs, true);
            continueFromLoadedFile = true;
            status = PLAY;
        } else if (ch == 'Q') {
            status = QUIT;
            System.exit(0);
        }
    }

    /**
     * Parse the input string to seed
     */
    private long parseInputToSeed(String input) {
        String s = input;

        if (s.length() > 19) {
            seed  = Long.parseLong(s.substring(0, 19));
        } else {
            seed = Long.parseLong(s);
        }
        return seed;
    }

    /**
     * Handles inputs from the game.
     */
    private void parseMovement(char ch, boolean keyIn) {
        if (ch == 'A') {
            moveStatus = myWorld.movePlayer(LEFT);
            inputs.append(ch);
            if (keyIn) {
                if (moveStatus == WIN || moveStatus == LOSE) {
                    status = moveStatus;
                }
            }
        } else if (ch == 'W') {
            moveStatus = myWorld.movePlayer(UP);
            inputs.append(ch);
            if (keyIn) {
                if (moveStatus == WIN || moveStatus == LOSE) {
                    status = moveStatus;
                }
            }
        } else if (ch == 'D') {
            moveStatus = myWorld.movePlayer(RIGHT);
            inputs.append(ch);
            if (keyIn) {
                if (moveStatus == WIN || moveStatus == LOSE) {
                    status = moveStatus;
                }
            }
        } else if (ch == 'S') {
            moveStatus = myWorld.movePlayer(DOWN);
            inputs.append(ch);
            if (keyIn) {
                if (moveStatus == WIN || moveStatus == LOSE) {
                    status = moveStatus;
                }
            }
        } else if (ch == ':') {
            moveStatus = SAVE;
        } else if (ch == 'Q') {
            if (moveStatus == SAVE) {
                saveFile();
                status = QUIT;
                if (keyIn) {
                    System.exit(0);
                }
            }
            moveStatus = -1;
//            status = QUIT;
//            if (keyIn) {
//                System.exit(0);
//            }
        } else {
            moveStatus = -1;
        }
    }

    /**
     * Draws the main menu.
     */
    private void drawGameMenu() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.white);
        StdDraw.setFont(TITLE_FONT);
        StdDraw.text(midWidth, HEIGHT - 10, "CS61B: THE GAME");
        StdDraw.setFont(SUBTITLE_FONT);
        StdDraw.text(midWidth, midHeight, "NEW GAME (N)");
        StdDraw.text(midWidth, midHeight - 2, "LOAD GAME (L)");
        StdDraw.text(midWidth, midHeight - 4, "REPLAY GAME (R)");
        StdDraw.text(midWidth, midHeight - 6, "QUIT (Q)");
        StdDraw.show();
    }

    /**
     * Draws the prompt menu that lets the users enter the seed.
     */
    private void drawPromptMenu() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        String input = inputs.toString();
        input = input.substring(input.indexOf('N') + 1);

        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.white);
        StdDraw.setFont(TITLE_FONT);
        StdDraw.text(midWidth, HEIGHT - 10, "NEW GAME");
        StdDraw.setFont(SUBTITLE_FONT);
        StdDraw.text(midWidth, midHeight, "Enter any number and then press \"S\".");
        StdDraw.setPenColor(Color.yellow);
        StdDraw.text(midWidth, midHeight - 2, input);
        StdDraw.show();
    }

    /**
     * Draws the game with seconds of full map view.
     */
    private void drawWorld() {
        TETile[][] w = myWorld.getWorld();
        ter.renderFrame(w);
        drawInstruction();
        drawHint();
        mousePointer();
        StdDraw.show();
    }

    /**
     * Draws instructions.
     */
    private void drawInstruction() {
        StdDraw.setFont(REGULAR_FONT);
        StdDraw.setPenColor(Color.white);
        StdDraw.textLeft(0, HEIGHT - 1, "A:LEFT W:UP D:RIGHT S:DOWN");
        StdDraw.textRight(WIDTH - 1, HEIGHT - 1, "\":\":SAVE Q:QUIT");
    }


    /**
     * Adds hints to StdDraw.
     */
    private void drawHint() {
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenColor(Color.yellow);
        StdDraw.text(WIDTH / 2, HEIGHT - 5,
                "To win the game, get to the treasure without overlapping your previous track!");
    }

    /**
     * Draws the game result with a full map view.
     */
    private void drawResult() {
        TETile[][] w = myWorld.getWorld();
        ter.renderFrame(w);
        drawInstruction();
        mousePointer();
        StdDraw.setFont(REGULAR_FONT);
        if (status == WIN) {
            StdDraw.setPenColor(Color.CYAN);
            StdDraw.text(WIDTH / 2, HEIGHT - 3, "You found the treasure! You win!");
        } else if (status == LOSE) {
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(WIDTH / 2, HEIGHT - 3, "You stepped on your track! You lose!");
        }
        StdDraw.setPenColor(Color.yellow);
        StdDraw.text(WIDTH / 2, HEIGHT - 5, "Press any key to continue");
        StdDraw.show();
    }

    /**
     * Saves the current inputs to a text file.
     */
    private void saveFile() {
        File f = new File("./save_game.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(inputs.toString());
            os.close();
            fs.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }

    }

    /**
     * Reads the input from the saved text file.
     */
    private String loadFile() {
        File f = new File("./save_game.txt");
        String loadInputs = null;
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                loadInputs = os.readObject().toString();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }

        return loadInputs;
    }

    // monitor mouse moves to display the current tile under mouse pointer
    private void mousePointer() {
        int mx = (int) StdDraw.mouseX();
        int my = (int) StdDraw.mouseY() - 2;
        TETile[][] w = myWorld.getWorld();

        if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {

            if (w[mx][my].equals(Tileset.WALL)) {
                StdDraw.setPenColor(Color.white);
                StdDraw.text(WIDTH / 2, HEIGHT - 1, "Wall");
            } else if (w[mx][my].equals(Tileset.AVATAR)) {
                StdDraw.setPenColor(Color.white);
                StdDraw.text(WIDTH / 2, HEIGHT - 1, "Player");
            } else if (w[mx][my].equals(Tileset.FLOOR)) {
                StdDraw.setPenColor(Color.white);
                StdDraw.text(WIDTH / 2, HEIGHT - 1, "Floor!");
            } else if (w[mx][my].equals(Tileset.FLOWER)) {
                //StdDraw.enableDoubleBuffering();
                StdDraw.setPenColor(Color.white);
                StdDraw.text(WIDTH / 2, HEIGHT - 1, "Treasure");
            } else {
                //StdDraw.enableDoubleBuffering();
                StdDraw.setPenColor(Color.white);
                StdDraw.text(WIDTH / 2, HEIGHT - 1, "Outside");
            }

            //StdDraw.show();
        }
    }

    /**
     * Calculates the delta time with the previous frame.
     * @return the amount of time between the previous frame with the present
     */
    private long frameDeltaTime() {
        return System.currentTimeMillis() - prevFrameTimestamp;
    }

    /**
     * Resets the frame timestamp to the current time in milliseconds.
     */
    private void resetFrameTimer() {
        prevFrameTimestamp = System.currentTimeMillis();
    }


    /**
     * Determines if a new frame should be rendered.
     * This estimates a 60 fps cap on the rendered window.
     */
    public boolean shouldRenderNewFrame() {  //?
        if (frameDeltaTime() > 100) {
            resetFrameTimer();
            return true;
        }
        return false;
    }

}
