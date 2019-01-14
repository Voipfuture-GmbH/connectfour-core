package com.voipfuture.connectfour;

/**
 * The game screen.
 *
 * Used to render the current game state/board.
 * @author tobias.gierke@voipfuture.com
 */
public interface IScreen
{
    /**
     * Shows a message on the screen.
     *
     * @param message
     */
    void showMessage(String message);

    /**
     * Renders the current game state/board.
     *
     * @param state
     */
    void render(GameState state);
}