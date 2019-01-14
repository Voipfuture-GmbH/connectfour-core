package com.voipfuture.connectfour;

import org.apache.commons.lang3.Validate;

import java.util.Optional;

import static com.voipfuture.connectfour.IInputProvider.InputEvent.EventType.MOVE;
import static com.voipfuture.connectfour.IInputProvider.InputEvent.EventType.NEW_GAME;

/**
 * Game controller.
 *
 * The game controller's {@link #tick()} methods is called every 16 milliseconds,
 * checks for available user input, processes the user input (if available) and re-draws the screen.
 *
 * @author tobias.gierke@voipfuture.com
 */
public class Controller
{
    private final IInputProvider input;
    private final IScreen screen;
    private final GameState gameState;

    /**
     * Create instance.
     *
     * @param input provides user input
     * @param screen screen to render the game state on
     * @param gameState the game state
     */
    public Controller(IInputProvider input, IScreen screen, GameState gameState)
    {
        Validate.notNull( input, "input must not be null" );
        Validate.notNull( screen, "screen must not be null" );
        Validate.notNull( gameState, "gameState must not be null" );
        this.input = input;
        this.screen = screen;
        this.gameState = gameState;
    }

    public void restartGame() {
        input.clearInputQueue();
        gameState.startNewGame();
    }

    /**
     * Run the game.
     *
     * This methods gets called every 16 milliseconds,
     * checks for available user input, processes the user input (if available) and finally re-draws the screen.
     */
    public void tick()
    {
        final Optional<IInputProvider.InputEvent> event = input.readInput(gameState);
        if ( event.isPresent() )
        {
            if ( event.get().hasType( NEW_GAME ) )
            {
                restartGame();
            }
            else
            {
                final boolean gameNotOver = !gameState.isGameOver();
                if ( gameNotOver && event.get().hasType( MOVE ) )
                {
                    final IInputProvider.MoveEvent ev = (IInputProvider.MoveEvent) event.get();
                    if (gameState.board.move(ev.column, ev.player) != -1)
                    {
                        if (gameState.getState().isEmpty()) // neither draw nor win
                        {
                            gameState.advanceToNextPlayer();
                        }
                    }
                    else
                    {
                        screen.showMessage("Cannot insert tile here, column is full already");
                    }
                    gameState.moveFinished();
                }
                else if (gameNotOver)
                {
                    throw new RuntimeException("Unhandled event: "+event.get());
                }
            }
        }
        screen.render( gameState );
    }
}
