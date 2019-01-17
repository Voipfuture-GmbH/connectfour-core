package com.voipfuture.connectfour;

import com.voipfuture.connectfour.util.InputProviderLoader;
import org.apache.commons.lang3.Validate;

import java.util.Optional;

import static com.voipfuture.connectfour.IInputProvider.InputEvent.EventType.NEW_GAME;
import static com.voipfuture.connectfour.IInputProvider.InputEvent.EventType.PLAYER_METADATA_CHANGED;
import static com.voipfuture.connectfour.IInputProvider.InputEvent.EventType.START_EVENT;
import static com.voipfuture.connectfour.IInputProvider.InputEvent.EventType.STOP_EVENT;

/**
 * A {@link IInputProvider} that wraps input providers for computer players and human players and
 * automatically dispatches requests to the right one depending on the type of player that has to move.
 *
 * <b>Note that computer players do not need to returna {@link com.voipfuture.connectfour.IInputProvider.NewGameEvent}
 * when the {@link GameState#getState() game state} is draw or win/loss, this is automatically handled by this class.</b>
 *
 * @author tobias.gierke@voipfuture.com
 */
public class DelegatingInputProvider implements IInputProvider
{
    private final IInputProvider humanInput;

    private boolean autoplay;

    /**
     * Create instance.
     *
     * @param humanInput provides human moves
     */
    public DelegatingInputProvider(IInputProvider humanInput)
    {
        Validate.notNull( humanInput, "humanInput must not be null" );
        this.humanInput = humanInput;
        InputProviderLoader.reloadAlgorithms();
    }

    private Optional<InputEvent> filterHumanEvents(Optional<InputEvent> input,boolean onlyComputerPlayers)
    {
        if ( ! input.isPresent() ) {
            return input;
        }
        IInputProvider.InputEvent ev = input.get();
        if ( ev.hasType( STOP_EVENT ) )
        {
            System.out.println("Auto-play is now OFF.");
            this.autoplay = false;
            return Optional.empty();
        }
        if ( ev.hasType( START_EVENT ) )
        {
            System.out.println("Auto-play is now ON.");
            this.autoplay = true;
            return Optional.empty();
        }
        if ( ev.hasType( PLAYER_METADATA_CHANGED ) )
        {
            InputProviderLoader.reloadAlgorithms();
            return Optional.empty();
        }
        return input;
    }

    @Override
    public Optional<InputEvent> readInput(GameState gameState)
    {
        final Player currentPlayer = gameState.currentPlayer();
        final boolean onlyComputerPlayers = gameState.players.stream().allMatch( Player::isComputer );

        if ( onlyComputerPlayers ) // we still need to process human inputs to start/stop/restart the game
        {
            final Optional<InputEvent> input = filterHumanEvents( humanInput.readInput( gameState ), onlyComputerPlayers );
            if ( input.isPresent() )
            {
                final IInputProvider.InputEvent ev = input.get();
                if ( ev.hasType( NEW_GAME ) )
                {
                    return input;
                }
            }
        }

        if ( currentPlayer.isComputer() )
        {
            if ( gameState.isGameOver() )
            {
                if ( ! onlyComputerPlayers ) // wait for the slow human to read the message & have a look at the board....
                {
                    try
                    {
                        Thread.sleep(3 * 1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                return Optional.of(new NewGameEvent());
            }

            if ( onlyComputerPlayers && ! autoplay ) {
                return Optional.empty();
            }

            System.out.println("'"+currentPlayer.name()+"' is thinking ("+currentPlayer.maxThinkDepth()+" half-moves look-ahead) ...");
            long time1 = System.currentTimeMillis();
            final Optional<InputEvent> result = InputProviderLoader.getInputProvider( currentPlayer ).readInput(gameState);
            long elapsed = System.currentTimeMillis() - time1;
            System.out.println( "Done. Player "+gameState.currentPlayer().name()+" took "+elapsed+" ms to think, average speed is "+(currentPlayer.totalMovesAnalyzed / currentPlayer.totalMoveTimeSeconds)+" moves/s");
            return result;
        }
        return filterHumanEvents( humanInput.readInput(gameState), onlyComputerPlayers );
    }

    @Override
    public void clearInputQueue()
    {
        humanInput.clearInputQueue();
    }
}