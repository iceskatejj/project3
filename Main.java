package core;

import tileengine.TETile;

public class Main {
    public static void main(String[] args) {

        // build your own world
        if (args.length > 1) {
            System.out.println("Can only have one argument - the input string");
            System.exit(0);
        } else if (args.length == 1) {
            Game game = new Game();
            TETile[][] myWorld = game.playWithInputString(args[0], false);
            System.out.println(TETile.toString(myWorld));
        } else {
            Game game = new Game();
            game.playWithKeyboard();
        }
    }
}

