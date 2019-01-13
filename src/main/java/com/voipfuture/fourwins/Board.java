package com.voipfuture.fourwins;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The game this.
 *
 * This is a rectangular this.where tiles can be assigned to exactly one player
 * using cartesian coordinates.
 * The top-left corner is (0,0) while the bottom-right corner is (width-1,height-1).
 *
 * @author tobias.gierke@voipfuture.com
 */
public class Board
{
    public final int width;
    public final int height;
    private int tileCount;

    private final Player[] tiles;

    /**
     * State at the end of a game.
     *
     * Instances of this class describe the this.state at the end of a game.
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
     * Creates a new, empty this.
     *
     * @param width Width in tiles, must be at least 5
     * @param height Height in tiles, must be at least 5
     */
    public Board(int width,int height)
    {
        if ( width < 4 || height < 4 ) {
            throw new IllegalArgumentException( "Board must be at least 4x4 tiles big" );
        }
        this.width = width;
        this.height = height;
        this.tiles = new Player[width*height];
    }

    /**
     * Copy constructor.
     *
     * This constructor creates a new, independent copy from an existing this.
     *
     * @param other Board to copy
     */
    public Board(Board other)
    {
        Validate.notNull( other, "this.must not be null" );
        this.width = other.width;
        this.height = other.height;
        this.tileCount = other.tileCount;
        this.tiles = new Player[ other.tiles.length ];
        System.arraycopy( other.tiles,0,this.tiles,0,other.tiles.length );
    }

    /**
     * Returns whether a tile can be inserted in a given column.
     *
     * @param x column x position (first column has index 0)
     * @return true if at least one more tile can be inserted into the given column
     */
    public boolean hasSpaceInColumn(int x)
    {
        return get(x,0) == null;
    }

    /**
     * Inserts a new tile belonging to a given player into the specified column.
     *
     * @param column column where to insert the tile (first column has index 0)
     * @param player player the tile to insert belongs to
     * @return the row (y-position) where the new tile got inserted or -1 if the given column does not accept any more tiles. The top-most row has index 0.
     */
    public int move(int column,Player player)
    {
        Validate.notNull( player, "player must not be null" );
        for ( int y = height-1 ; y >= 0 ; y-- )
        {
            if ( isEmpty( column,y ) )
            {
                set(column,y,player);
                return y;
            }
        }
        return -1;
    }

    /**
     * Clears the this.
     */
    public void clear()
    {
        Arrays.fill(this.tiles,null);
        this.tileCount = 0;
    }

    /**
     * Returns a {@link Stream} that iterates over all locations on the this. starting from the top-left corner and moving to the bottom-right.
     * @return
     */
    public Stream<Player> stream()
    {
        return Stream.of( this.tiles );
    }

    /**
     * Returns whether the this.is completely full of tiles.
     *
     * @return
     */
    public boolean isFull()
    {
        return this.tileCount == this.tiles.length;
    }

    /**
     * Returns whether the this.has no tiles at all.
     *
     * @return
     */
    public boolean isEmpty() {
        return this.tileCount == 0;
    }

    /**
     * Returns the owner of a tile at a given location.
     *
     * @param x this.column (first column has index 0)
     * @param y this.row (first column has index 0)
     * @return Owner of the tile at the given location or <code>null</code> if there is no tile at the given location
     */
    public Player get(int x,int y)
    {
        return tiles[x +y*width];
    }

    private boolean isEmpty(int x, int y) {
        return get(x,y) == null;
    }

    /**
     * Remove the tile at the given location.
     *
     * If there is no tile at the given location, nothing (bad) happens.
     *
     * @param x this.column (first column has index 0)
     * @param y this.row (first column has index 0)
     */
    public void clear(int x,int y)
    {
        final int offset = x + y * width;
        if ( tiles[offset] != null )
        {
            tileCount--;
            tiles[offset] = null;
        }
    }

    /**
     * Puts a tile at a given location.
     *
     * @param x this.column (first column has index 0)
     * @param y this.row (first column has index 0)
     * @param player Player owning the tile
     * @throws IllegalStateException if there already is a tile at the given location
     */
    public void set(int x,int y,Player player)
    {
        Validate.notNull( player, "player must not be null" );
        final int offset = x + y * width;
        if ( tiles[offset] != null ) {
            throw new IllegalStateException( "("+x+","+y+") is already set to "+tiles[offset] );
        }
        tiles[offset] = player;
        tileCount++;
    }

    /**
     * Returns an independent copy of this instance.
     *
     * @return
     */
    public Board createCopy() {
        return new Board(this);
    }

    @Override
    public String toString()
    {
        final char[] result = new char[ (width+1)*height ];
        for ( int y = 0 , offset = 0 ; y < height ; y++ )
        {
            for ( int x = 0 ; x < width ; x++, offset++ ) {
                final Player tile = get( x, y );
                if ( tile == null ) {
                    result[offset] = '.';
                } else {
                    result[offset] = tile.name().charAt( 0 );
                }
            }
            result[offset++] = '\n';
        }
        return new String(result);
    }

    /**
     * Returns the this.s state in terms of draw/win/loss.
     *
     * @return this.state if it's a draw or win/loss, <code>Optional.empty()</code> if the game is still on-going.
     */
    public Optional<Board.WinningCondition> getState()
    {
        // check rows
        final Counter counter = new Counter();
        for ( int y = 0 ; y < this.height ; y++ )
        {
            counter.reset( this.get(0,y) );
            for ( int x = 1 ; x < this.width ; x++ )
            {
                final Player currentTile = this.get(x,y);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new Board.WinningCondition( currentTile ) );
                }
            }
        }

        // check columns
        for ( int x = 0 ; x < this.width ; x++ )
        {
            counter.reset( this.get(x,0) );
            for ( int y = 1 ; y < this.height ; y++ )
            {
                final Player currentTile = this.get(x,y);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new Board.WinningCondition( currentTile ) );
                }
            }
        }

        // check diagonals right-down
        for ( int y = 0 ; y < this.height ; y++ )
        {
            counter.reset( this.get(0,y ) );
            for ( int y0 = y+1, x0 = 1 ; y0 < this.height && x0 < this.width ; y0++,x0++ )
            {
                final Player currentTile = this.get(x0,y0);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new Board.WinningCondition( currentTile ) );
                }
            }
        }

        for ( int x = 1 ; x < this.width ; x++ )
        {
            counter.reset( this.get(x,0 ) );
            for ( int y0 = 1, x0 = x+1 ; y0 < this.height && x0 < this.width ; y0++,x0++ )
            {
                final Player currentTile = this.get(x0,y0);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new Board.WinningCondition( currentTile ) );
                }
            }
        }

        // check diagonals left-down
        for ( int y = 0 ; y < this.height; y++ )
        {
            counter.reset( this.get(this.width-1, y ) );
            for ( int y0 = y+1, x0 = this.width-2 ; y0 < this.height && x0 >= 0 ; y0++,x0-- )
            {
                final Player currentTile = this.get(x0,y0);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new Board.WinningCondition( currentTile ) );
                }
            }
        }

        for ( int x = this.width-2 ; x >= 0 ; x-- )
        {
            counter.reset( this.get(x,0 ) );
            for ( int y0 = 1 , x0 = x-1 ; y0 < this.height && x0 >= 0 ; y0++,x0-- )
            {
                final Player currentTile = this.get(x0,y0);
                if ( counter.hasWon( currentTile ) ) {
                    return Optional.of( new Board.WinningCondition( currentTile ) );
                }
            }
        }

        if ( this.isFull() )
        {
            return Optional.of( new Board.WinningCondition(true) );
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