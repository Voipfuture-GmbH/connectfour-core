package com.voipfuture.fourwins;

import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.voipfuture.fourwins.IInputProvider.InputEvent.EventType.NEW_GAME;
import static com.voipfuture.fourwins.IInputProvider.InputEvent.EventType.PLAYER_METADATA_CHANGED;
import static com.voipfuture.fourwins.IInputProvider.InputEvent.EventType.START_EVENT;
import static com.voipfuture.fourwins.IInputProvider.InputEvent.EventType.STOP_EVENT;

/**
 * A {@link IInputProvider} that wraps input providers for computer players and human players and
 * automatically dispatches requests to the right one depending on the type of player that has to move.
 *
 * <b>Note that computer players do not need to returna {@link com.voipfuture.fourwins.IInputProvider.NewGameEvent}
 * when the {@link GameState#getState() game state} is draw or win/loss, this is automatically handled by this class.</b>
 *
 * @author tobias.gierke@voipfuture.com
 */
public class DelegatingInputProvider implements IInputProvider
{
    private static UnloadingClassLoader classLoader;
    private static final Map<Player,IInputProvider> computerPlayers = new HashMap<>();

    private static final class UnloadingClassLoader extends ClassLoader
    {
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException
        {
            byte[] bt = loadClassData(name);
            return defineClass(name, bt, 0, bt.length);
        }

        private byte[] loadClassData(String className) throws ClassNotFoundException
        {
            final InputStream is = DelegatingInputProvider.class.getClassLoader().getResourceAsStream(className.replace(".", "/")+".class");
            if ( is == null ) {
                throw new ClassNotFoundException( "Could not find class '"+className+"' on classpath" );
            }
            try
            {
                return is.readAllBytes();
            }
            catch (IOException e)
            {
                throw new ClassNotFoundException( "Failed to load '"+className+"' from classpath",e);
            }
        }
    }

    private final IInputProvider humanInput;

    private boolean autoplay;

    /**
     * Schedules all algorithm implementations for reload.
     */
    public static void reloadAlgorithms()
    {
        System.out.println("Algorithm implementations will be reloaded.");
        computerPlayers.clear();
        classLoader = new UnloadingClassLoader();
    }

    /**
     * Create instance.
     *
     * @param humanInput provides human moves
     */
    public DelegatingInputProvider(IInputProvider humanInput)
    {
        Validate.notNull( humanInput, "humanInput must not be null" );
        this.humanInput = humanInput;
        reloadAlgorithms();
    }

    private static IInputProvider loadInputProvider(Player player)
    {
        final Object instance;
        try
        {
            System.out.println("Trying to load algorithm '"+player.algorithm()+"' ...");
            final Class<?> clazz = classLoader.loadClass( player.algorithm() );
            instance = clazz.getDeclaredConstructor( null ).newInstance( null );
        }
        catch (Exception e)
        {
            System.err.println("Failed to load algorithm '"+player.algorithm()+"' from classpath");
            throw new RuntimeException(e);
        }
        return (IInputProvider) instance;
    }

    private static IInputProvider getInputProvider(Player player)
    {
        if ( ! player.isComputer() ) {
            throw new IllegalArgumentException( "Only applicable to computer players" );
        }
        return computerPlayers.computeIfAbsent( player,p ->
        {
            p.totalMovesAnalyzed = 0;
            p.totalMoveTimeSeconds = 0;
            return loadInputProvider( p );
        });
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
            reloadAlgorithms();
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

            System.out.print("'"+currentPlayer.name()+"' is thinking ("+currentPlayer.maxThinkDepth()+" half-moves look-ahead) ...");
            final Optional<InputEvent> result = getInputProvider( currentPlayer ).readInput(gameState);
            System.out.println( "done. Average speed is "+(currentPlayer.totalMovesAnalyzed / currentPlayer.totalMoveTimeSeconds)+" moves/s");
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