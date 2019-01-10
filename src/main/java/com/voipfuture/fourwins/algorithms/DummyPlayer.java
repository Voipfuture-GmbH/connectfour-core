package com.voipfuture.fourwins.algorithms;

import com.voipfuture.fourwins.GameState;
import com.voipfuture.fourwins.IInputProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A computer player that just does a random (but valid) move.
 * @author tgierke
 */
public class DummyPlayer implements IInputProvider
{
    @Override
    public Optional<InputEvent> readInput(GameState gameState)
    {
        final List<Integer> possibleMoves = new ArrayList<>();
        for ( int col = 0 ; col < gameState.board.width ; col++ )
        {
            if ( gameState.board.hasSpaceInRow( col ) ) {
                possibleMoves.add( col );
            }
        }
        if ( ! possibleMoves.isEmpty() )
        {
            final int randomIdx = new Random( System.currentTimeMillis() ).nextInt( possibleMoves.size() );
            return Optional.of( new MoveEvent( gameState.currentPlayer(), possibleMoves.get( randomIdx ) ) );
        }
        return Optional.empty();
    }

    @Override
    public void clearInputQueue()
    {
        // nop
    }
}
