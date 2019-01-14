package com.voipfuture.connectfour;

import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        final Optional<Board.WinningCondition> condition = board.getState();
        condition.ifPresent(cond ->
        {
                gameCount++;
                if ( ! cond.isDraw ) {
                    incWins(cond.player() );
                }
        });
    }

    /**
     * Returns the this.s state in terms of draw/win/loss.
     *
     * @return this.state if it's a draw or win/loss, <code>Optional.empty()</code> if the game is still on-going.
     */
    public Optional<Board.WinningCondition> getState() {
        return board.getState();
    }

    /**
     * Returns whether the game is over (either because of a draw or win/loss).
     * @return
     */
    public boolean isGameOver() {
        return board.isGameOver();
    }
}