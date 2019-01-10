package com.voipfuture.fourwins;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * The game board.
 *
 * This is a rectangular board where tiles can be assigned to exactly one player
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
     * Creates a new, empty board.
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
     * This constructor creates a new, independent copy from an existing board.
     *
     * @param other Board to copy
     */
    public Board(Board other)
    {
        Validate.notNull( other, "board must not be null" );
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
    public boolean hasSpaceInRow(int x)
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
     * Clears the board.
     */
    public void clear()
    {
        Arrays.fill(this.tiles,null);
        this.tileCount = 0;
    }

    /**
     * Returns a {@link Stream} that iterates over all locations on the board, starting from the top-left corner and moving to the bottom-right.
     * @return
     */
    public Stream<Player> stream()
    {
        return Stream.of( this.tiles );
    }

    /**
     * Returns whether the board is completely full of tiles.
     *
     * @return
     */
    public boolean isFull()
    {
        return this.tileCount == this.tiles.length;
    }

    /**
     * Returns whether the board has no tiles at all.
     *
     * @return
     */
    public boolean isEmpty() {
        return this.tileCount == 0;
    }

    /**
     * Returns the owner of a tile at a given location.
     *
     * @param x board column (first column has index 0)
     * @param y board row (first column has index 0)
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
     * @param x board column (first column has index 0)
     * @param y board row (first column has index 0)
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
     * @param x board column (first column has index 0)
     * @param y board row (first column has index 0)
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
}
