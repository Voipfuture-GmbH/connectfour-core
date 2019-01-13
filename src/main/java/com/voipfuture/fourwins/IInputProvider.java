package com.voipfuture.fourwins;

import org.apache.commons.lang3.Validate;

import java.util.Optional;

/**
 * Provides user input to control the game.
 *
 * Input providers maintain a queue of input events and return one event at a time when {@link #readInput(GameState) asked}.
 *
 * @author tobias.gierke@voipfuture.com
 * @see Controller#tick()
 */
public interface IInputProvider
{
    /**
     * Abstract base class for game input events.
     *
     * @author tobias.gierke@voipfuture.com
     */
    abstract class InputEvent
    {
        public final EventType type;

        /**
         * Event type.
         * @author tobias.gierke@voipfuture.com
         */
        enum EventType
        {
            /** The event describes a player move. */
            MOVE,
            /** The event signals that a new game should be started. */
            NEW_GAME,
            /** The event signals that player meta-data has changed*/
            PLAYER_METADATA_CHANGED,
            /** The event signals that a computer-only game should start/continue. */
            START_EVENT,
            /** The event signals that a computer-only game should halt. */
            STOP_EVENT;
        };

        private InputEvent(EventType t) {
            this.type = t;
        }

        /**
         * Returns whether this event has a given type.
         *
         * @param t type to check for
         * @return
         */
        public final boolean hasType( EventType t) {
            return t.equals(this.type);
        }
    }

    /**
     * A player move event.
     */
    final class MoveEvent extends InputEvent
    {
        public final Player player;
        public final int column;

        /**
         * Create instance.
         *
         * @param player Player making the move
         * @param column Board column where the tile should be inserted (first column has index 0)
         */
        public MoveEvent(Player player, int column)
        {
            super(EventType.MOVE);
            Validate.notNull( player, "player must not be null" );
            Validate.isTrue(column >= 0, "Column must be >= 0" );
            this.player = player;
            this.column = column;
        }
    }

    /**
     * A "start a new game" event.
     */
    final class NewGameEvent extends InputEvent
    {
        public NewGameEvent()
        {
            super(EventType.NEW_GAME);
        }
    }

    /**
     * Event send after player configuration has changed.
     */
    final class PlayerMetadataChangedEvent extends InputEvent
    {
        public PlayerMetadataChangedEvent()
        {
            super(EventType.PLAYER_METADATA_CHANGED );
        }
    }

    /**
     * Event send to start a computer-computer match.
     */
    final class StartEvent extends InputEvent
    {
        public StartEvent()
        {
            super(EventType.START_EVENT );
        }
    }

    /**
     * Event send to halt a computer-computer match.
     */
    final class StopEvent extends InputEvent
    {
        public StopEvent()
        {
            super(EventType.STOP_EVENT);
        }
    }

    /**
     * Returns the user input (if any).
     *
     * @param gameState The current game state
     * @return the input of the {@link GameState#currentPlayer() current player}
     */
    Optional<InputEvent> readInput(GameState gameState);

    /**
     * Discard any input events that might still be queue.
     *
     * Only applicable to input providers that keep a queue of input events.
     */
    default void clearInputQueue() {
    }
}