package com.voipfuture.fourwins;

public class GameStateTest extends AbstractTestHelper
{
    public void testEmptyBoard() {

        final String s = ".....\n"+
                         ".....\n"+
                         ".....\n"+
                         ".....\n"+
                         ".....";
        setupBoard( s);

        final int nonEmptyTileCount = state.board.stream().mapToInt( x -> x == null ? 0 : 1 ).sum();
        assertEquals( 0, nonEmptyTileCount );
        assertFalse( state.board.isFull() );
        assertOngoing();
    }

    public void testFullBoard() {

        final String s = "12121\n"+
                         "21212\n"+
                         "11211\n"+
                         "21112";
        setupBoard( s);

        final int nonEmptyTileCount = state.board.stream().mapToInt( x -> x == null ? 0 : 1 ).sum();
        assertEquals( 20, nonEmptyTileCount );
        assertTrue( state.board.isFull() );
        assertDraw();
    }

    public void testWinRow() {

        String s = ".....\n"+
                         ".....\n"+
                         ".....\n"+
                         ".....\n"+
                         "11111";
        setupBoard( s);
        assertWon( player1 );

        //
        s = ".....\n"+
        ".....\n"+
        ".....\n"+
        "1111.";
        setupBoard( s);
        assertWon( player1);

        //
        s = ".....\n"+
        ".....\n"+
        ".....\n"+
        ".1111";
        setupBoard( s);
        assertWon( player1);

        //
        s = "......\n"+
            "......\n"+
            "......\n"+
            ".1111.";
        setupBoard( s);
        assertWon( player1);

        //
        s = "......\n"+
            "......\n"+
            "......\n"+
            ".11.11";
        setupBoard( s);
        assertOngoing();

        //
        s = ".....\n"+
        ".....\n"+
        ".....\n"+
        "111..";
        setupBoard( s);
        assertOngoing();
    }

    public void testWinColumn() {

        String s = "1....\n"+
                   "1....\n"+
                   "1....\n"+
                   "1....\n"+
                   "1....";
        setupBoard( s);
        assertWon( player1);

        //
        s = "1....\n"+
            "1....\n"+
            "1....\n"+
            "1....\n"+
            ".....";
        setupBoard( s);
        assertWon( player1);

        //
        s = ".....\n"+
            "1....\n"+
            "1....\n"+
            "1....\n"+
            "1....";
        setupBoard( s);
        assertWon( player1);

        //
        s = ".....\n"+
            "1....\n"+
            "1....\n"+
            "1....\n"+
            "1....\n"+
            ".....";
        setupBoard( s);
        assertWon( player1);

        //
        s = "1....\n"+
            "1....\n"+
            ".....\n"+
            "1....\n"+
            "1....\n"+
            ".....";
        setupBoard( s);
        assertOngoing();

        //
        s = ".....\n"+
            "1....\n"+
            "1....\n"+
            "1....\n"+
            ".....";
        setupBoard( s);
        assertOngoing();
    }

    public void testRightDiagonals() {

        //
        String s = "1....\n"+
                   ".1...\n"+
                   "..1..\n"+
                   "...1.\n"+
                   ".....";
        setupBoard( s);
        assertWon( player1);

        //
        s = ".....\n"+
            ".1...\n"+
            "..1..\n"+
            "...1.\n"+
            "....1";
        setupBoard( s);
        assertWon( player1);

        //
        s = ".....\n"+
            "1....\n"+
            ".1...\n"+
            "..1..\n"+
            "...1.";
        setupBoard( s);
        assertWon( player1);

        //
        s = ".1...\n"+
            "..1..\n"+
            "...1.\n"+
            "....1\n"+
            ".....";
        setupBoard( s);
        assertWon( player1);

        //
        s = "..1..\n"+
            "...1.\n"+
            "....1\n"+
            ".....\n"+
            ".....";
        setupBoard( s);
        assertOngoing();

        //
        s = ".....\n"+
            "1....\n"+
            ".1...\n"+
            "..1..\n"+
            ".....";
        setupBoard( s);
        assertOngoing();
    }

    public void testLeftDiagonals() {
        //
        String s = "....1\n"+
                   "...1.\n"+
                   "..1..\n"+
                   ".1...\n"+
                   ".....";
        setupBoard( s);
        assertWon( player1);

        //
        s = "...1.\n"+
            "..1..\n"+
            ".1...\n"+
            "1....\n"+
            ".....";
        setupBoard( s);
        assertWon( player1);

        //
        s = ".....\n"+
            "....1\n"+
            "...1.\n"+
            "..1..\n"+
            ".1...";
        setupBoard( s);
        assertWon( player1);
    }
}
