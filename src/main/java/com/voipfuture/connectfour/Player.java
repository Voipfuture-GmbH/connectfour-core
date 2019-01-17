package com.voipfuture.connectfour;

import org.apache.commons.lang3.Validate;

import java.awt.Color;

/**
 * A game player.
 *
 * A player has a name and can either be a human player or a computer player.
 *
 * @author tobias.gierke@voipfuture.com
 */
public class Player
{
    private static long nextID = 0;

    private final long id = nextID++;
    private Color tileColor;
    private String name;
    private boolean isComputer;
    private int maxThinkDepth = 7;
    private String algorithm = "com.voipfuture.connectfour.algorithms.DummyPlayer";

    public long totalMovesAnalyzed;
    public float totalMoveTimeSeconds;

    /**
     * Creates a player with a given name.
     *
     * @param name
     */
    public Player(String name,boolean isComputer,Color tileColor)
    {
        Validate.notBlank( name, "name must not be null or blank");
        Validate.notNull( tileColor, "tileColor must not be null" );
        this.name = name;
        this.isComputer = isComputer;
        this.tileColor = tileColor;
    }

    /**
     * Returns the color this player's tiles should have.
     *
     * @return
     */
    public Color tileColor()
    {
        return tileColor;
    }

    /**
     * Sets the color this player's tiles should have.
     *
     * @param tileColor
     */
    public void setTileColor(Color tileColor)
    {
        Validate.notNull( tileColor, "tileColor must not be null" );
        this.tileColor = tileColor;
    }

    /**
     * (computer players only) Returns the maximum depth to look ahead in half-moves.
     *
     * @return
     * @see #isComputer()
     * @see #setAlgorithm(String)
     * @see #setMaxThinkDepth(int)
     */
    public int maxThinkDepth()
    {
        return maxThinkDepth;
    }

    /**
     * (computer players only) Set the maximum depth to look ahead in half-moves.
     *
     * @param maxThinkDepth
     * @see #isComputer()
     */
    public void setMaxThinkDepth(int maxThinkDepth)
    {
        Validate.isTrue( maxThinkDepth > 0 , "maxThinkDepth must be at least 1 half-move." );
        this.maxThinkDepth = maxThinkDepth;
    }

    /**
     * (computer players only) Returns the fully-qualified class name of the {@link IInputProvider} implementation
     * that should be used if this is a computer player.
     * @return
     * @see #isComputer()
     */
    public String algorithm() {
        return algorithm;
    }

    /**
     * (computer players only) Sets the fully-qualified class name of the {@link IInputProvider} implementation that should be used
     * if this is a computer player.
     *
     * @param className
     * @return this instance (for chaining)
     * @see #isComputer()
     */
    public Player setAlgorithm(String className)
    {
        Validate.notBlank( className, "algorithm must not be null or blank");
        this.algorithm = className;
        return this;
    }

    /**
     * Returns the player's name.
     *
     * @return
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns whether this player is a computer.
     *
     * @return
     *
     * @see #setAlgorithm(String)
     * @see #setMaxThinkDepth(int)
     * @see #setComputer(boolean)
     */
    public boolean isComputer() {
        return isComputer;
    }

    /**
     * Marks this player as a human or computer.
     *
     * @param isComputer <code>true</code> if this is a computer player
     */
    public void setComputer(boolean isComputer)
    {
        this.isComputer = isComputer;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public final boolean equals(Object obj)
    {
        return obj instanceof Player && this.id == ((Player) obj).id;
    }

    @Override
    public final int hashCode()
    {
        return Long.hashCode( id );
    }

    public void setName(String name)
    {
        Validate.notBlank( name, "text must not be null or blank");
        this.name = name;
    }


}