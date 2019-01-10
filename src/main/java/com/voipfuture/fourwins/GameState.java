package com.voipfuture.fourwins;

import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Game state.
 *
 * Instances of this class hold the game's current state.
 *
 * <ul>
 *     <li>a list of all the players</li>
 *     <li>the player that is to make the next move</li>
 *     <li>statistics about how many times a game ended in a draw,win or loss for each player</li>
 *     <li>the board with all the tiles that have been set so far</li>
 * </ul>
 *
 * @author tobias.gierke@voipfuture.com
 */
public class GameState
{
    public final Board board;
    public final List<Player> players;
    private int currentPlayerIdx = 0;

    private int gameCount;
    private final Map<Player,Integer> winCounts = new HashMap<>();

    /**
     * State at the end of a game.
     *
     * Instances of this class describe the board state at the end of a game.
     * Possible states are either draw (no more moves are possible) or win
     * for a given player.
     *
     * @author tobias.gierke@voipfuture.com
     */
    public static final class WinningCondition
    {
        private final Player player;

        /**
         * <code>true</code> if the game ended in a draw.
         */
        public final boolean isDraw;

        private WinningCondition(Player player) {
            this.player = player;
            this.isDraw = false;
        }

        private WinningCondition(boolean isDraw)
        {
            this.player = null;
            this.isDraw = true;
        }

        /**
         * Returns the player that won the game.
         *
         * @return
         * @throws IllegalStateException if the game ended in a draw
         * @see #isDraw
         */
        public Player player() {
            if ( isDraw ) {
                throw new IllegalStateException( "Must not be called for a draw" );
            }
            return player;
        }

        @Override
        public String toString()
        {
            return isDraw ? "DRAW" : player().name()+" won";
        }
    }

    private static final class Counter
    {
        private Player tile;
        private int count;

        public void reset(Player startingTile) {
            tile = startingTile;
            count = startingTile == null ? 0 : 1;
        }

        @Override
        public String toString()
        {
            return "Counter[ count="+count+", tile="+tile+"]";
        }

        public boolean hasWon(Player currentTile)
        {
            if ( currentTile == null ) {
                reset(null);
                return false;
            }
            if ( tile == null || ! Objects.equals(tile, currentTile ) )
            {
                reset(currentTile);
            }
            else
            {
                count++;
                if ( count == 4 )
                {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Create a new instance.
     *
     * @param board board to use
     * @param player1 First player
     * @param player2 Second player
     */
    public GameState(Board board, Player player1,Player player2)
    {
        Validate.notNull( board, "board must not be null" );
        Validate.notNull( player1, "player1 must not be null" );
        Validate.notNull( player2, "player2 must not be null" );
        this.board = board;
        this.players = List.of(player1,player2);
        this.players.forEach(p -> winCounts.put(p,0) );
        this.currentPlayerIdx = 0;
    }

    /**
     * Returns the player with the given index.
     *
     * @param playerIndex player index, first player has index 0.
     * @return
     */
    public Player player(int playerIndex) {
        return players.get(playerIndex);
    }

    /**
     * Returns whether all players are computer players.
     *
     * @return
     */
    public boolean onlyComputerPlayers()
    {
        return players.stream().allMatch( Player::isComputer );
    }

    /**
     * Returns the number of games played so far.
     * @return
     */
    public int getGameCount()
    {
        return gameCount;
    }

    public Map<Player,Integer> getWinCounts() {
        return new HashMap<>(winCounts);
    }

    private void incWins(Player player)
    {
        winCounts.put(player, winCounts.get(player) +1);
    }

    /**
     * Starts a new game.
     */
    public void startNewGame()
    {
        board.clear();
        final int playerIdx = new Random(System.currentTimeMillis()).nextInt( players.size());
        setCurrentPlayer( players.get(playerIdx) );
    }

    private void setCurrentPlayer(Player player)
    {
       this.currentPlayerIdx = players.indexOf(player);
    }

    /**
     * Returns the player that is to make the current move.
     *
     * @return current player
     */
    public Player currentPlayer()
    {
        return players.get( currentPlayerIdx );
    }

    /**
     * Returns the player that is to move after the current player.
     *
     * @return
     */
    public Player nextPlayer()
    {
        final int idx = players.indexOf( currentPlayer() );
        final int nextIdx = ( idx+1) % players.size();
        return players.get(nextIdx);
    }

    /**
     * Advances the game state to the next player.
     */
    public void advanceToNextPlayer()
    {
        currentPlayerIdx = players.indexOf( nextPlayer() );
    }

    /**
     * Update game statistics after a player has finished moving.
     */
    public void moveFinished()
    {
        final Optional<WinningCondition> condition = getState();
        condition.ifPresent(cond ->
        {
                gameCount++;
                if ( ! cond.isDraw ) {
                    incWins(cond.player() );
                }
        });
    }

    /**
     * Returns the board's state in terms of draw/win/loss.
     *
     * @return board state if it's a draw or win/loss, <code>Optional.empty()</code> if the game is still on-going.
     */
    public Optional<WinningCondition> getState()
    {
        // check rows
        final Counter counter = new Counter();
        for ( int y = 0 ; y < board.height ; y++ )
        {
            counter.reset( board.get(0,y) );
            for ( int x = 1 ; x < board.width ; x++ )
            {
                final Player currentTile = board.get(x,y);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new WinningCondition( currentTile ) );
                }
            }
        }

        // check columns
        for ( int x = 0 ; x < board.width ; x++ )
        {
            counter.reset( board.get(x,0) );
            for ( int y = 1 ; y < board.height ; y++ )
            {
                final Player currentTile = board.get(x,y);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new WinningCondition( currentTile ) );
                }
            }
        }

        // check diagonals right-down
        for ( int y = 0 ; y < board.height ; y++ )
        {
            counter.reset( board.get(0,y ) );
            for ( int y0 = y+1, x0 = 1 ; y0 < board.height && x0 < board.width ; y0++,x0++ )
            {
                final Player currentTile = board.get(x0,y0);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new WinningCondition( currentTile ) );
                }
            }
        }

        for ( int x = 1 ; x < board.width ; x++ )
        {
            counter.reset( board.get(x,0 ) );
            for ( int y0 = 1, x0 = x+1 ; y0 < board.height && x0 < board.width ; y0++,x0++ )
            {
                final Player currentTile = board.get(x0,y0);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new WinningCondition( currentTile ) );
                }
            }
        }

        // check diagonals left-down
        for ( int y = 0 ; y < board.height; y++ )
        {
            counter.reset( board.get(board.width-1, y ) );
            for ( int y0 = y+1, x0 = board.width-2 ; y0 < board.height && x0 >= 0 ; y0++,x0-- )
            {
                final Player currentTile = board.get(x0,y0);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new WinningCondition( currentTile ) );
                }
            }
        }

        for ( int x = board.width-2 ; x >= 0 ; x-- )
        {
            counter.reset( board.get(x,0 ) );
            for ( int y0 = 1 , x0 = x-1 ; y0 < board.height && x0 >= 0 ; y0++,x0-- )
            {
                final Player currentTile = board.get(x0,y0);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new WinningCondition( currentTile ) );
                }
            }
        }

        if ( board.isFull() )
        {
            return Optional.of( new WinningCondition(true) );
        }
        return Optional.empty();
    }

    /**
     * Returns whether the game is over (either because of a draw or win/loss).
     * @return
     */
    public boolean isGameOver() {
        return getState().isPresent();
    }
}