package com.voipfuture.fourwins.algorithms;

import com.voipfuture.fourwins.GameState;
import com.voipfuture.fourwins.IInputProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A computer player that just does a random (but valid) move.
 * @author tgierke
 */
public class DummyPlayer implements IInputProvider
{
    private final Random random = new Random();

    @Override
    public Optional<InputEvent> readInput(GameState gameState)
    {
        final List<Integer> possibleMoves = new ArrayList<>();
        for ( int col = 0 ; col < gameState.board.width ; col++ )
        {
            if ( gameState.board.hasSpaceInColumn( col ) ) {
                possibleMoves.add( col );
            }
        }
        if ( ! possibleMoves.isEmpty() )
        {
            Collections.shuffle(possibleMoves,random);
            return Optional.of( new MoveEvent( gameState.currentPlayer(), possibleMoves.get( 0 ) ) );
        }
        return Optional.empty();
    }
}