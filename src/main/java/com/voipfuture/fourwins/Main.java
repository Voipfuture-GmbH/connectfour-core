package com.voipfuture.fourwins;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.*;

/**
 * Application entry point.
 *
 * @author tobias.gierke@voipfuture.com
 */
public class Main
{
    private Board b;
    private GameState gameState;
    private SwingScreen screen;
    private Controller controller;

    public static void main(String[] args)
    {
        new Main().run();
    }

    public void run()
    {
        SwingUtilities.invokeLater( () ->
        {
            b = new Board( 5, 5 );
            gameState = new GameState( b, new Player("Tobi",false, Color.BLUE), new Player("Computer B",true, Color.RED) );
            screen = new SwingScreen(gameState);
            controller = new Controller(new DelegatingInputProvider(screen),screen,gameState);
        });
        new Timer( 16, ev -> controller.tick() ).start();
    }
}
