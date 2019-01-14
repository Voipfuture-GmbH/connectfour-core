package com.voipfuture.connectfour;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link IScreen} implementation that uses Java Swing.
 *
 * @author tobias.gierke@voipfuture.com
 */
public class SwingScreen extends JFrame implements IScreen, IInputProvider
{
    private final List<InputEvent> inputEvents = new ArrayList<>();

    private static final Color LINE_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = Color.RED;

    private JTextArea messages = new JTextArea();
    private JTextField currentPlayerName = new JTextField();
    private JTextField gameStateText = new JTextField();
    private JTextField gameCountText = new JTextField();
    private JTextField player1Wins = new JTextField();
    private JTextField player2Wins = new JTextField();

    final JButton startButton = new JButton("Start Game");
    final JButton stopButton = new JButton("Stop Game");
    final JButton reloadClassesButton = new JButton("Reload classes");

    private GameState gameState;

    private PlayerEditPanel player1Panel;
    private PlayerEditPanel player2Panel;

    private final class PlayerEditPanel extends JPanel {

        private final int playerNo;

        private final JTextField algoField = new JTextField();
        private final JRadioButton humanButton = new JRadioButton("Human");
        private final JRadioButton computerButton = new JRadioButton("Computer");
        private final JTextField nameField = new JTextField();
        private final JTextField maxThinkDepthField = new JTextField();

        private PlayerEditPanel(int playerNo)
        {
            this.playerNo = playerNo;
            final Player player = gameState.players.get( playerNo );

            setLayout(  new GridBagLayout() );

            // player name
            GridBagConstraints cnstrs = new GridBagConstraints();
            cnstrs.gridx = 0; cnstrs.gridy = 0;
            cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
            cnstrs.fill = GridBagConstraints.NONE;

            add( new JLabel("Name:") , cnstrs);

            nameField.setText( player.name() );
            nameField.setColumns( 15 );
            addChangeListener( nameField, () ->
            {
                if ( ! StringUtils.isBlank( nameField.getText() ) ) {
                    gameState.players.get( this.playerNo).setName( nameField.getText() );
                    queueEvent(  new PlayerMetadataChangedEvent() );
                }
            });

            cnstrs = new GridBagConstraints();
            cnstrs.gridx = 1; cnstrs.gridy = 0;
            cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
            cnstrs.fill = GridBagConstraints.NONE;
            add( nameField, cnstrs);

            // player type human
            humanButton.setSelected( ! player.isComputer() );
            humanButton.addActionListener( ev ->
            {
                gameState.players.get( this.playerNo ).setComputer( false );
                algoField.setEnabled(false);
                maxThinkDepthField.setEnabled(false);

                startButton.setEnabled( gameState.onlyComputerPlayers() );
                stopButton.setEnabled( gameState.onlyComputerPlayers() );

                queueEvent(  new PlayerMetadataChangedEvent() );
            } );

            cnstrs = new GridBagConstraints();
            cnstrs.gridx = 2; cnstrs.gridy = 0;
            cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
            cnstrs.fill = GridBagConstraints.NONE;
            add( humanButton, cnstrs );

            // player type computer
            computerButton.setSelected( player.isComputer() );
            computerButton.addActionListener( ev ->
            {
                algoField.setEnabled(true);
                maxThinkDepthField.setEnabled(true);

                gameState.players.get( this.playerNo ).setComputer( true );

                startButton.setEnabled( gameState.onlyComputerPlayers() );
                stopButton.setEnabled( gameState.onlyComputerPlayers() );

                queueEvent(  new PlayerMetadataChangedEvent() );
            } );

            cnstrs = new GridBagConstraints();
            cnstrs.gridx = 3; cnstrs.gridy = 0;
            cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
            cnstrs.fill = GridBagConstraints.NONE;
            add( computerButton, cnstrs );

            final ButtonGroup grp = new ButtonGroup();
            grp.add( humanButton );
            grp.add( computerButton );

            // algorithm text field
            cnstrs = new GridBagConstraints();
            cnstrs.gridx = 4; cnstrs.gridy = 0;
            cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
            cnstrs.fill = GridBagConstraints.NONE;
            add( new JLabel("Algorithm:"), cnstrs );

            algoField.setEnabled( player.isComputer() );
            algoField.setText( gameState.players.get( this.playerNo).algorithm() );
            addChangeListener( algoField, () ->
            {
                if ( StringUtils.isNotBlank( algoField.getText() ) )
                {
                    gameState.players.get( this.playerNo ).setAlgorithm( algoField.getText() );
                    queueEvent(  new PlayerMetadataChangedEvent() );
                }
            } );
            cnstrs = new GridBagConstraints();
            cnstrs.gridx = 5; cnstrs.gridy = 0;
            cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
            cnstrs.fill = GridBagConstraints.NONE;
            add( algoField, cnstrs );

            // max think depth
            maxThinkDepthField.setEnabled( player.isComputer() );
            maxThinkDepthField.setText( Integer.toString( player.maxThinkDepth() ) );
            maxThinkDepthField.setColumns( 3 );

            cnstrs = new GridBagConstraints();
            cnstrs.gridx = 6; cnstrs.gridy = 0;
            cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
            cnstrs.fill = GridBagConstraints.NONE;
            add( new JLabel("Look-ahead:"), cnstrs );

            addChangeListener( maxThinkDepthField, () ->
            {
                if ( StringUtils.isNotBlank( maxThinkDepthField.getText() ) )
                {
                    final int newDepth = Integer.parseInt( maxThinkDepthField.getText() );
                    System.out.println( "Player '"+gameState.player( playerNo ).name()+"' now uses "+newDepth+" moves look-ahead.");
                    gameState.player( playerNo ).setMaxThinkDepth( newDepth );
                    queueEvent(  new PlayerMetadataChangedEvent() );
                }
            } );
            cnstrs = new GridBagConstraints();
            cnstrs.gridx = 7; cnstrs.gridy = 0;
            cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
            cnstrs.fill = GridBagConstraints.NONE;
            add( maxThinkDepthField, cnstrs );
        }
    }

    private static void addChangeListener(JTextField component, Runnable r)
    {
        component.addActionListener( ev -> r.run() );
        component.addFocusListener( new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                r.run();
            }
        });
    }

    private final class MyPanel extends JPanel
    {
        private int boxSize = 30;

        private int xmin = 50;
        private int ymin = 70;

        public MyPanel()
        {
            setFocusable( true );
            addMouseListener( new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    final int col = (e.getX() - xmin) / boxSize;
                    final int row = (e.getY() - ymin) / boxSize;
                    System.out.println("CLICK "+col+","+row);
                    if ( col >= 0 && row >= 0 && col < gameState.board.width && row < gameState.board.height )
                    {
                        inputEvents.removeIf(  ev -> ev.hasType( InputEvent.EventType.MOVE ) && ((MoveEvent) ev).player.equals( gameState.currentPlayer() ) );
                        queueEvent(  new MoveEvent(  gameState.currentPlayer(), col ) );
                    }
                }
            } );
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent( g );

            // occupy 80% of the space
            final int availableSpace = (int) (Math.min( getWidth(), getHeight() )*0.8);

            final int bw = availableSpace / gameState.board.width;
            final int bh = availableSpace / gameState.board.height;
            boxSize = Math.min( bw, bh );

            xmin = (getWidth()-gameState.board.width*boxSize)/2;
            ymin = (getHeight()-gameState.board.height*boxSize)/2;

            final Board board = gameState.board;

            g.setColor( TEXT_COLOR );

            // draw board
            final int xmax = xmin + gameState.board.width*boxSize;
            final int ymax = ymin + gameState.board.height*boxSize;

            g.setColor( TEXT_COLOR );
            for ( int x = 0 ; x <= board.width ; x++ )
            {
                final int xpos = xmin + x * boxSize;
                g.drawLine( xpos, ymin, xpos, ymax );
                for (int y = 0; y <= board.height; y++)
                {
                    final int ypos = ymin + y*boxSize;
                    g.drawLine( xmin , ypos , xmax, ypos );
                }
            }

            g.setColor( LINE_COLOR );
            for ( int x = 0 ; x < board.width ; x++ )
            {
                final int xpos = xmin + x*boxSize;
                for ( int y = 0 ; y < board.height ; y++ )
                {
                    final int ypos = ymin + y*boxSize;

                    g.setColor( TEXT_COLOR );
                    g.drawArc( xpos+1,ypos+1, boxSize-2, boxSize-2, 0 , 360 );

                    final Player player = board.get( x, y );
                    if ( player != null )
                    {
                        g.setColor( player.tileColor() );
                        g.fillArc( xpos+1,ypos+1, boxSize-2, boxSize-2, 0 , 360 );
                        g.setColor( LINE_COLOR );
                    }
                }
            }
        }
    }

    private static JPanel addTitledBorder(JPanel toWrap,String message) {
        toWrap.setBorder( BorderFactory.createTitledBorder( message ) );
        return toWrap;
    }

    private static JPanel addBorder(JPanel toWrap) {
        toWrap.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
        return toWrap;
    }

    public SwingScreen(GameState gameState)
    {
        super("Connect Four!");
        Validate.notNull( gameState, "gameState must not be null" );
        this.gameState = gameState;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo( null );

        MyPanel panel = new MyPanel();
        panel.setPreferredSize( new Dimension(640,480) );
        getContentPane().setLayout( new GridBagLayout() );

        // button panel
        GridBagConstraints cnstrs = new GridBagConstraints();
        cnstrs.gridx = 0; cnstrs.gridy = 0;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.HORIZONTAL;
        cnstrs.weightx = 1.0; cnstrs.weighty = 0.1;
        getContentPane().add( addBorder( createButtonPanel() ) , cnstrs );

        // player #1 edit panel
        player1Panel = new PlayerEditPanel( 0 );
        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 0; cnstrs.gridy = 1;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.HORIZONTAL;
        cnstrs.weightx = 1.0; cnstrs.weighty = 0.1;
        getContentPane().add( addTitledBorder( player1Panel , "Player 1" ), cnstrs );

        // player #2 edit panel
        player2Panel = new PlayerEditPanel( 1 );
        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 0; cnstrs.gridy = 2;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.HORIZONTAL;
        cnstrs.weightx = 1.0; cnstrs.weighty = 0.1;
        getContentPane().add( addTitledBorder( player2Panel , "Player 2") , cnstrs );

        // info panel
        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 0; cnstrs.gridy = 3;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.HORIZONTAL;
        cnstrs.weightx = 1.0; cnstrs.weighty = 0.1;
        getContentPane().add( addTitledBorder( createInfoPanel() , "Info") , cnstrs );

        // board panel
        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 0; cnstrs.gridy = 4;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.weightx = 1.0; cnstrs.weighty = 0.6;
        cnstrs.fill = GridBagConstraints.BOTH;
        getContentPane().add( addTitledBorder(panel,"Board") , cnstrs);

        pack();
        setVisible( true );
    }

    private JPanel createButtonPanel()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(  new FlowLayout() );
        final JButton button = new JButton("New Game");
        button.addActionListener(  ev ->
        {
            inputEvents.removeIf( x -> x.hasType( InputEvent.EventType.NEW_GAME ) );
            queueEvent( new NewGameEvent() );
        });
        panel.add( button );

        startButton.setEnabled( gameState.onlyComputerPlayers() );
        stopButton.setEnabled( false );

        startButton.addActionListener(  ev ->
        {
            inputEvents.removeIf( x -> x.hasType( InputEvent.EventType.START_EVENT ) );
            queueEvent( new StartEvent() );
            startButton.setEnabled( false );
            stopButton.setEnabled( true );
        });

        reloadClassesButton.addActionListener(  ev -> queueEvent(  new PlayerMetadataChangedEvent() ) );

        panel.add( startButton);
        panel.add( stopButton);
        panel.add( reloadClassesButton );

        stopButton.addActionListener(  ev ->
        {
            inputEvents.removeIf( x -> x.hasType( InputEvent.EventType.STOP_EVENT ) );
            queueEvent( new StopEvent() );
            startButton.setEnabled( true );
            stopButton.setEnabled( false );
        });

        return panel;
    }
    
    private void queueEvent(IInputProvider.InputEvent event) 
    {
        Validate.notNull( event, "event must not be null" );
        inputEvents.add(  event );
    }

    private JPanel createInfoPanel()
    {
        final JPanel infoPanel = new JPanel();
        infoPanel.setLayout(  new GridBagLayout() );

        // current player
        GridBagConstraints cnstrs = new GridBagConstraints();
        cnstrs.gridx = 0; cnstrs.gridy = 0;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;
        infoPanel.add( new JLabel("Player:"), cnstrs );

        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 1; cnstrs.gridy = 0;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;

        currentPlayerName.setColumns( 15 );
        currentPlayerName.setEditable(false);
        infoPanel.add( currentPlayerName, cnstrs  );

        // game state
        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 2; cnstrs.gridy = 0;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;
        infoPanel.add( new JLabel("Game state:"), cnstrs );

        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 3; cnstrs.gridy = 0;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;

        gameStateText.setColumns( 10 );
        gameStateText.setEditable(false);
        infoPanel.add( gameStateText  );

        // game count
        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 4; cnstrs.gridy = 0;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;
        infoPanel.add( new JLabel("Game:") , cnstrs );

        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 5; cnstrs.gridy = 0;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;

        gameCountText.setColumns( 5 );
        gameCountText.setEditable(false);
        infoPanel.add( gameCountText, cnstrs  );

        // player 1 wins
        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 0; cnstrs.gridy = 1;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;
        infoPanel.add( new JLabel("Player #1 wins:"), cnstrs );

        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 1; cnstrs.gridy = 1;
        cnstrs.gridwidth = 2; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;

        player1Wins.setColumns( 4 );
        player1Wins.setEditable(false);
        infoPanel.add( player1Wins, cnstrs  );

        // player 2 wins
        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 3; cnstrs.gridy = 1;
        cnstrs.gridwidth = 2; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;
        infoPanel.add( new JLabel("Player #2 wins:"), cnstrs );

        cnstrs = new GridBagConstraints();
        cnstrs.gridx = 5; cnstrs.gridy = 1;
        cnstrs.gridwidth = 1; cnstrs.gridheight = 1;
        cnstrs.fill = GridBagConstraints.NONE;

        player2Wins.setColumns( 4 );
        player2Wins.setEditable(false);
        infoPanel.add( player2Wins, cnstrs  );

        return infoPanel;
    }

    private void updateInfoPanel()
    {
        changeText( currentPlayerName, gameState.currentPlayer().name() );

        final String state = gameState.getState().map( condition -> condition.isDraw ? "DRAW !!!" : condition.player().name()+" WON!" ).orElse( "Ongoing match" );
        changeText( gameStateText, state );

        changeText( gameCountText, Integer.toString( gameState.getGameCount() ) );
        changeText( player1Wins , Integer.toString( gameState.getWinCounts().get( gameState.players.get(0) )));
        changeText( player2Wins , Integer.toString( gameState.getWinCounts().get( gameState.players.get(1) )));
    }

    private static void changeText(JTextComponent component, String newText)
    {
        if ( ! Objects.equals( component.getText() , newText ) ) {
            component.setText(newText);
        }
    }

    public void showMessage(String message)
    {
        System.err.println( message );
        final String existing = messages.getText();
        if ( existing == null ) {
            messages.setText( message );
        } else {
            messages.setText(  existing+"\n"+message );
        }
    }

    public void render(GameState state)
    {
        this.gameState = state;
        updateInfoPanel();
        repaint();
    }

    @Override
    public Optional<InputEvent> readInput(GameState gameState)
    {
        return inputEvents.isEmpty() ? Optional.empty() : Optional.of ( inputEvents.remove( 0 ) );
    }

    @Override
    public void clearInputQueue()
    {
        this.inputEvents.clear();
    }
}