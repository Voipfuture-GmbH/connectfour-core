package com.voipfuture.fourwins;

import junit.framework.TestCase;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractTestHelper extends TestCase
{
    protected Player player1;
    protected Player player2;
    protected GameState state;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        player1 = createPlayer1();
        player2 = createPlayer2();
    }

    protected Player createPlayer1() {
        return new Player("1",false, Color.RED);
    }

    protected Player createPlayer2() {
        return new Player("2",false,Color.BLUE);
    }

    protected final void assertDraw() {
        final Optional<Board.WinningCondition> win = state.getState();
        assertTrue( win.isPresent() );
        assertTrue( "Expected a draw but got "+win.get(), win.get().isDraw );
    }

    protected final void assertOngoing() {
        final Optional<Board.WinningCondition> win = state.getState();
        assertEquals("Expected ongoing match but got \n"+win, false,win.isPresent() );
    }

    protected final void assertWon(Player player) {
        final Optional<Board.WinningCondition> win = state.getState();
        assertTrue( "Expected player to have won but state is 'ongoing match'",win.isPresent() );
        assertEquals( player, win.get().player() );
    }

    protected final void setupBoard( String s )
    {
        final Board board = createBoard( s, Arrays.asList(player1,player2));
        state = new GameState( board, player1, player2 );
    }

    protected final Board createBoard(String s, List<Player> players) {

        final String[] lines = s.split("\n");
        final int minWidth = Stream.of(lines).mapToInt( x->x.length() ).min().getAsInt();
        final int maxWidth = Stream.of(lines).mapToInt( x->x.length() ).max().getAsInt();
        if ( minWidth != maxWidth ) {
            throw new IllegalArgumentException( "Input string contains lines with differing widths" );
        }
        if ( lines.length < 4 || maxWidth < 4 ) {
            throw new IllegalArgumentException( "Board needs to be at least 4x4 tiles big (was: "+maxWidth+"x"+lines.length+")" );
        }

        final Board b = new Board(maxWidth,lines.length);
        for (int y = 0, linesLength = lines.length ; y < linesLength; y++)
        {
            final String row = lines[y];
            for ( int x = 0 ; x < row.length() ; x++ ) {
                final char c = row.charAt( x );
                if ( c != '.' )
                {
                    if ( !Character.isDigit( c ) )
                    {
                        throw new IllegalArgumentException( "Unsupported character in board: '" + c + "'" );
                    }
                    b.set( x, y, players.get( Integer.parseInt( "" + c )-1 ) );
                }
            }

        }
        return b;
    }
}
