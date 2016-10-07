package joeflowsays;

/*
|   Joe Flow Says
|   A simple memory-based game devloped by Joe Boudreau between June 2016 and
|   September 2016. Using Java Swing libraries, the purpose of the game is to
|   showcase various UI attributes, including custom-designed graphic components,
|   parallel event-driven outputs, and audio-visual feedback. 
|   
|   Version: 1.0
|   Date: 04/09/16
|   
*/

import java.util.Random;
import java.util.Arrays;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.sound.midi.Sequence;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.swing.AbstractButton;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;


/**
 * @author      Joseph Boudreau <a href="mailto:thejoeflow@gmail.com">thejoeflow@gmail.com</a>
 * @version     1.0 
 */
public class JoeFlowSays extends JFrame{
    
    //Declare global variables
    private Container       pane;
    private JPanel          startPanel;
    private JPanel          gamePanel;
    private JPanel          userSeq;
    private JPanel          computerSeq;
    private int             numResponses;
    private ImageIcon[]     thumbs;
    private JDialog         gameOverContainer;
    private JDialog         WinGameContainer;
    private JDialog         helpContainer;
    private Sequencer       player;
    private Sequence        gameMusic;
    private JLabel          highScore;
    
    //Initialize some global variables
    private ImageIcon JoeIcon =                 new ImageIcon(getClass().getResource("/Images/Look and Feel/GameOverIcon.png"));
    private PanelChangeListener PCListener =    new PanelChangeListener();
    private boolean volumeOFF =                 false;
    private boolean initialStart =              true;
    private int[] responses =                   new int[10];
    Object LOCK =                               new Object();
    Object LOCK2 =                              new Object();
    
    
    /**
     * Constructor method runs the separate initializer function
     */
    public JoeFlowSays() {
        
        initUI();
    }
    
    /**
     * Initializer function that is called by the constructor to set up the
     * initial game window and the static start panel. Sets up the game music 
     * and the look and feel of the window. Imports the game light thumbnails
     * as well
     */
    public void initUI() {

        pane = getContentPane(); 
        startPanel = getStartPanel();

        try{
        //Set up MidiSystem Midi Sequence player
        gameMusic = MidiSystem.getSequence(getClass().getResource("/Sound/ThemeMusic.mid"));
        player = MidiSystem.getSequencer();
        player.setSequence(gameMusic);
        player.open();
        player.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        adjustVolume(gameMusic, 35);
        player.start();
        }
        catch(InvalidMidiDataException | IOException | MidiUnavailableException u){}

        
        
        BufferedImage JFlowIcon = null;
            try {                                       //importing the Image Icon
                JFlowIcon = ImageIO.read(getClass().getResourceAsStream("/Images/Look and Feel/WindowIcon.png"));
            } catch (IOException e){}
        
        setLayout(new BorderLayout());
        setTitle("Joe Flow Says!");
        setLocationByPlatform(false);                   //Opens in top-left corner of screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(JFlowIcon);
        if(initialStart){try{ Thread.currentThread().sleep(2000);} catch (InterruptedException e) {};}
        
        this.setVisible(true);                          //Opens JFrame
        
        pane.add(startPanel);      
        pane.validate();
        pack();
        
        setResizable(false);
        setSize(750,750);         
                                                         
        thumbs = new ImageIcon[5];  //Initialize the game lights thumbnails for use in the results table
        thumbs[0] = new ImageIcon(getClass().getResource("/Images/Lights/RedLightSmall.jpg"));
        thumbs[1] = new ImageIcon(getClass().getResource("/Images/Lights/BlueLightSmall.jpg"));
        thumbs[2] = new ImageIcon(getClass().getResource("/Images/Lights/GreenLightSmall.jpg"));
        thumbs[3] = new ImageIcon(getClass().getResource("/Images/Lights/YellowLightSmall.jpg"));
        thumbs[4] = new ImageIcon(getClass().getResource("/Images/Lights/OrangeLightSmall.jpg"));
        
        if(highScore == null){
            highScore = new JLabel("0");
            highScore.setFont(gameFonts("Gameplay",16f));
        }
        initialStart = false;
    }
    
    /**
     * Adjusts the volume of a Midi audio sequence
     * 
     * @param musicSeq  the Sequence object that will have their tracks lowered in volume
     * @param volume    the volume level that the tracks will be set to, integer between 0-127
     * @throws InvalidMidiDataException 
     */
    public void adjustVolume(Sequence musicSeq, int volume) throws InvalidMidiDataException{
        Track[] tracks = musicSeq.getTracks();
            for (Track t : tracks){
                for(int channel = 0; channel < 16; channel++){
                    t.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 7, volume), 0));
                }    
            }
    }
    
    /**
     * Returns a JToolBar object that will be used at the top of the game
     * content pane. 
     * <p>
     * This function sets up the visual appearance and attributes of the
     * game's menu bar. It adds one button, Help. It aligns
     * it with the far right side of the window using a BoxLayout and 
     * a horizontal glue object.
     * 
     * @return the JToolBar to use at the top of the game content pane
     */
    public JPanel getInfoBar(){
        JPanel imfoBar = new JPanel();
        imfoBar.setLayout(new BoxLayout(imfoBar, BoxLayout.X_AXIS));
        
        JButton help = new JButton();
        help.setName("Help");
        makeCustomButton(help, "/Images/Look and Feel/helpUP.png", "/Images/Look and Feel/helpP.png");
        help.addActionListener(PCListener);
      
        imfoBar.add(Box.createHorizontalGlue());
        imfoBar.add(help);
        imfoBar.setOpaque(false);
        imfoBar.setBackground(new Color(0,0,0,0));
        
        return imfoBar;
    }
    
    public JPanel getVolumeBar(){
        JPanel volumeBar = new JPanel();
        volumeBar.setLayout(new BoxLayout(volumeBar, BoxLayout.X_AXIS));
        
        JToggleButton vol = new JToggleButton("",volumeOFF);
        vol.setName("Volume");
        makeCustomButton(vol, "/Images/Look and Feel/VolumeButtonUnpressed.png",
                "/Images/Look and Feel/VolumeButtonPressed.png");
        vol.addActionListener(PCListener);
        
        volumeBar.add(vol);
        volumeBar.add(Box.createHorizontalGlue());
        volumeBar.setOpaque(false);
        volumeBar.setBackground(new Color(0,0,0,0));
        
        return volumeBar;
        
    }
    
    /**
     * Returns the JGamePanel object that will be used as the start panel.
     * <p>
     * This function is called in the initUI function. It creates a JGamePanel
     * and adds the start button to it.
     * 
     * @return the JGamePanel object to be used as the start panel
     * @see JGamePanel
     */
    public JGamePanel getStartPanel() {
        
        JGamePanel sP = new JGamePanel("/Images/Look and Feel/backgroundMain.jpg");
        BoxLayout bl = new BoxLayout(sP, BoxLayout.Y_AXIS);
        sP.setLayout(bl);
        
        JButton startButt = new JButton();
        startButt.setName("Start");
        makeCustomButton(startButt,"/Images/Look and Feel/startButtonUnpressed.png", "/Images/Look and Feel/startButtonPressed.png");
        startButt.setAlignmentX(Box.CENTER_ALIGNMENT);
        startButt.addActionListener(PCListener);
        
        JPanel infoBar = getInfoBar();
        JPanel bottomVolumeBar = getVolumeBar();
        
        sP.add(infoBar);
        sP.add(Box.createVerticalStrut(550));       //Add the button 550px down from the top of the window
        sP.add(startButt);
        sP.add(Box.createVerticalGlue());
        sP.add(bottomVolumeBar);
 
        return sP;
    }
    
    /**
     * The main game flow controller. 
     * <p>
     * This function is the process controller for the gameplay of Joe Flow Says
     * It is called by the button Action Listener "PCListener" when the start
     * button is pressed in the start panel.
     * 
     * @see #PCListener
     * 
     */
    public void startGame() {
        
        gamePanel = new JGamePanel("/Images/Look and Feel/backgroundGame.jpg"); 
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        
        //Set up the panels that will be added to the gamePanel
        JPanel infoBar = getInfoBar();
        JPanel scorePanel = setUpScorePanel();
        MsgPanel topPanel = new MsgPanel("Ready?");
        JPanel lightsPanel = setUpLightsRow();
        JPanel buttonRow = setUpButtonsRow();
        JPanel resultsPanel = setUpResultsPanel();
        JPanel bottomVolumeBar = getVolumeBar();
        
        JPanel MsgandScore = new JPanel();
        scorePanel.setAlignmentX(TOP_ALIGNMENT);
        topPanel.setAlignmentY(CENTER_ALIGNMENT);
        MsgandScore.setLayout(new BoxLayout(MsgandScore,BoxLayout.X_AXIS));
        MsgandScore.setOpaque(false);
        MsgandScore.add(Box.createHorizontalStrut(25));
        MsgandScore.add(scorePanel);
        MsgandScore.add(topPanel);
        MsgandScore.add(Box.createHorizontalStrut(110));
        
        //Lay out the panels in the game Panel
        gamePanel.add(infoBar);
        gamePanel.add(MsgandScore);
        gamePanel.add(lightsPanel);
        gamePanel.add(Box.createVerticalStrut(40));
        gamePanel.add(buttonRow);
        gamePanel.add(Box.createVerticalGlue());
        gamePanel.add(resultsPanel);
        gamePanel.add(Box.createVerticalStrut(20));
        gamePanel.add(bottomVolumeBar);
        
        //if coming from the start panel, remove the start panel before adding the game panel
        if (pane.getComponentCount() > 0 && pane.getComponent(0).equals(startPanel)){
            pane.remove(startPanel);
        }
        pane.add(gamePanel);
        pane.validate();
        
        /*The following code block retrieves the 5 JLight objects that are
        set up in the function setUpLightsRow(). The JLights are passed as
        parameters to the light up sequence function during the game, so the
        explicit object pointers are added to an array named "lights"
        */
        JPanel lightsRow = (JPanel) lightsPanel.getComponent(1);
        JLight[] lights = new JLight[5];
        int k = 0;
       
        for (Component jC : lightsRow.getComponents()){
            lights[k++] = (JLight) jC;
        }

        /*The following code block retrieves the 5 JButtons that are
        set up in the function setUpLButtonsRow(). The JButtons are periodically
        enabled and disabled during the gameplay, so the explicit object pointers
        are added to an array named "buttons"
        */
        JButton[] buttons = new JButton[5];
       
        for (int j = 0; j < 5; j++) {
            buttons[j] = (JButton) buttonRow.getComponent(j);
        }

        //The rest of the code below this point is the actual game play
        try{ Thread.currentThread().sleep(1500);} catch (InterruptedException e) {};
        topPanel.removeText();                                  //Remove the "Ready" message after 1.5 seconds
        pane.validate();
        
        boolean stillWinning = true;
        int seqLength = 1;                                      //seqLength is the number of lights to light up in a row, increases by 1 each level
        int[] lightSeq;
        
        while(stillWinning && seqLength <= 10){
            
            try{ Thread.currentThread().sleep(700);} catch (InterruptedException e) {};
            topPanel.setText("Level " + Integer.toString(seqLength));   //indicate level
            try{ Thread.currentThread().sleep(500);} catch (InterruptedException e) {};
            topPanel.removeText();                                      //remove level text
            try{ Thread.currentThread().sleep(200);} catch (InterruptedException e) {};
            topPanel.countDown();                                       //initiate a 3..2..1 countdown
            
            lightSeq = lightUp(seqLength, lights);              //Display the light sequence   
            
            for (JButton b : buttons){ b.setEnabled(true);}     //Enable the buttons    
            
            /*
            This while loop waits for the global variable "numResponses" to reach
            the required number of responses (seqLength). This variable is incremented
            within the buttonAction action listener whenever a response button is
            clicked. The loop utlizies a object wait method in the body of the loop
            to avoid computationally expensive loop condition checking. The object
            "LOCK" is notified from the buttonAction action listener every time a
            response button is clicked, so it only checks the condition every click
            */
            numResponses = 0;
            synchronized(LOCK) {
                while(numResponses < seqLength) {
                    
                    try { LOCK.wait(); }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
            
            for (JButton b : buttons){ b.setEnabled(false);}
            
            showResults(lightSeq, seqLength);                   //Compare the user responses with the generated light sequence

            if(Arrays.equals(lightSeq, responses)){             //Check if the user is correct
                System.out.println("Correct!");
                playSound("Correct");
                if( Integer.parseInt(highScore.getText()) < seqLength ){
                    highScore.setText(Integer.toString(seqLength));
                    MsgandScore.getComponent(1).validate();
                }
                seqLength++;                                    //increase sequence length for the next level
                numResponses = 0;                               //reset number of responses
                for(int i = 0; i<10; i++){responses[i] = 0;}    //reset responses
            }
            else{
                System.out.println("Wrong!");
                playSound("Incorrect");
                stillWinning = false;                           //Set winning flag to false to exit game loop
                seqLength=1;                                    //reset sequence length
                numResponses = 0;                               //reset number of responses
                for(int i = 0; i<10; i++){responses[i] = 0;}    //reset responses
            }
        }
   
        if (!stillWinning){ 
            showGameOverDialog();                               //If user lost, show game over dialog
        }
        else{
            showWinGameDialog();
        }

    }
    
    /**
     * Creates the help dialog using a JDialog instance, displays it in the
     * middle of the window.
     * <p>
     * This JDialog instance has no header border, and therefore no system exit
     * button. The exit button is a custom added JButton
     */
    public void showHelpDialog(){
        helpContainer = new JDialog(this, "Help", true);
        
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
        helpPanel.setBackground(Color.LIGHT_GRAY);
        helpPanel.setBorder(new LineBorder(Color.BLACK, 3));
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBackground(Color.LIGHT_GRAY);
        
        JButton exitHelp = new JButton();
        exitHelp.setName("Exit Help");
        makeCustomButton(exitHelp, "/Images/Look and Feel/exitUP.png", "/Images/Look and Feel/exitP.png");
        exitHelp.addActionListener(PCListener);
        exitHelp.setAlignmentX(LEFT_ALIGNMENT);
        exitHelp.setAlignmentY(BOTTOM_ALIGNMENT);
        
        JLabel title = new JLabel(new ImageIcon(getClass().getResource("/Images/About/helpDialogTitle.png")));

        topPanel.add(Box.createHorizontalStrut(160));
        topPanel.add(title);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(exitHelp);
        
        JTabbedPane tabsPane = new JTabbedPane();
        
        JLabel helpImage = new JLabel(new ImageIcon(getClass().getResource("/Images/About/HelpPanel.jpg")));        
        JLabel aboutImage = new JLabel(new ImageIcon(getClass().getResource("/Images/About/AboutPanel.jpg")));
        
        tabsPane.add("Help", helpImage);
        tabsPane.add("About", aboutImage);

        helpPanel.add(Box.createVerticalStrut(5));
        helpPanel.add(topPanel);
        helpPanel.add(Box.createVerticalStrut(5));
        helpPanel.add(tabsPane);
        
        helpContainer.setContentPane(helpPanel);
        helpContainer.setResizable(false);
        helpContainer.setUndecorated(true); //removes the sytem border and exit button
        helpContainer.pack();
        helpContainer.setLocationRelativeTo(this);
        helpContainer.setVisible(true); 
        
    }
    
    /**
     * Creates the game over dialog using a JOptionPane instance
     * <p>
     * This function sets up a JOptionPane instance with three choices:
     * "Try Again", "Main Menu", and "Quit". Each button is registered to the same
     * Action Listener interface implementation. The OptionPane is displayed in the
     * middle of the window. It is a modal dialog, so no other actions are performed
     * until one of the three choices are chosen. The exit operation will not close
     * the dialog window.
     */
    public void showGameOverDialog(){
        
        JButton[] Options = new JButton[3];
        Options[0] = new JButton();
        Options[0].setName("Try Again");
        Options[0].addActionListener(PCListener);
        makeCustomButton(Options[0], "/Images/Look and Feel/TryAgainUP.png", "/Images/Look and Feel/TryAgainP.png");
        
        Options[1] = new JButton();
        Options[1].setName("Main Menu");
        Options[1].addActionListener(PCListener);
        makeCustomButton(Options[1], "/Images/Look and Feel/MainMenuUP.png", "/Images/Look and Feel/MainMenuP.png");
        
        Options[2] = new JButton();
        Options[2].setName("Quit");
        Options[2].addActionListener(PCListener);
        makeCustomButton(Options[2], "/Images/Look and Feel/QuitUP.png", "/Images/Look and Feel/QuitP.png");
        
        JPanel messagePanel = new JPanel(new BorderLayout());   //This JPanel is used instead of text in the JOpionPane
        messagePanel.setOpaque(true);
        JLabel msg = new JLabel("That's not what I said! Try Again?", JLabel.CENTER);
        msg.setVerticalAlignment(SwingConstants.CENTER);
        msg.setFont(new Font("Gameplay Regular",Font.PLAIN,15));
        messagePanel.add(msg);
        
        JOptionPane gameOver = new JOptionPane(messagePanel, JOptionPane.ERROR_MESSAGE,
        JOptionPane.YES_NO_CANCEL_OPTION, JoeIcon, Options, Options[0]);
        
        gameOverContainer = new JDialog(this, "Joe Flow Says", true);
        gameOverContainer.setContentPane(gameOver);
        gameOverContainer.getContentPane().setBackground(Color.WHITE);
        makeComponentsWhiteBG(gameOverContainer);               //Make background of dialog window white
        
        gameOverContainer.pack();
        gameOverContainer.setLocationRelativeTo(this);
        gameOverContainer.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        gameOverContainer.setVisible(true);
    }
    
    public void showWinGameDialog(){

        JButton[] prizeButt = new JButton[1];
        prizeButt[0] = new JButton();
        prizeButt[0].setName("Prize");
        prizeButt[0].addActionListener(PCListener);
        makeCustomButton(prizeButt[0], "/Images/Look and Feel/PrizeButtonUnpressed.png",
                                    "/Images/Look and Feel/PrizeButtonPressed.png");

        JPanel messagePanel = new JPanel();   //This JPanel is used instead of text in the JOpionPane
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(true);
        JLabel msg1 = new JLabel("YOU WIN!!!", JLabel.CENTER);
        msg1.setFont(new Font("Gameplay Regular",Font.PLAIN,15));
        
        JLabel msg2 = new JLabel("Click below to claim your prize!", JLabel.CENTER);
        msg2.setFont(new Font("Gameplay Regular",Font.PLAIN,12));
        
        messagePanel.add(Box.createVerticalGlue());
        messagePanel.add(msg1);
        messagePanel.add(msg2);
        messagePanel.add(Box.createVerticalGlue());
        
        JOptionPane winGamePane = new JOptionPane(messagePanel, JOptionPane.ERROR_MESSAGE,
        JOptionPane.OK_OPTION, JoeIcon, prizeButt);

        WinGameContainer = new JDialog(this, "Joe Flow Says", true);
        WinGameContainer.setContentPane(winGamePane);
        WinGameContainer.getContentPane().setBackground(Color.WHITE);
        makeComponentsWhiteBG(WinGameContainer);               //Make background of dialog window white

        WinGameContainer.pack();
        WinGameContainer.setLocationRelativeTo(this);
        WinGameContainer.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        WinGameContainer.setVisible(true);
    
    }
    
    /**
     * Sets the background of every component in the specified container to white
     * <p>
     * Uses a recursive algorithm to ensure that containers within the specified
     * container will also have all of their child component's backgrounds set
     * to white as well.
     * @param c     the Container object that can contain components, and other containers 
     */
    public void makeComponentsWhiteBG(Container c){
        
        Component[] m = c.getComponents();

        for(int i = 0; i < m.length; i++){
                m[i].setBackground(Color.WHITE);

            if(c.getClass().isInstance(m[i]));
                makeComponentsWhiteBG((Container)m[i]);
        }
    }
    
    /**
     * Shows the light sequence pattern that the user was supposed to replicate
     * <p>
     * 
     * @param JoeFlowSeq    the int array representing the light sequence
     * @param seqLen        the sequence length for the current level. This 
     *                      parameter is necessary because the int array passed
     *                      to this function is always of length 10.
     */
    public void showResults(int[] JoeFlowSeq, int seqLen){
        
        ImageIcon blank = new ImageIcon(getClass().getResource("/Images/Lights/BlankLight.jpg"));

        JLight[] userLights =       new JLight[10];
        JLight[] computerLights =   new JLight[10];

        for(int j = 0; j < 10; j++){
            //Retrieve the JLight objects from the user and computer panels in the
            //results table and assign them to two arrays
            userLights[j] =         (JLight) userSeq.getComponent(j);
            computerLights[j] =     (JLight) computerSeq.getComponent(j);
        }
        
        try{ Thread.sleep(500);} catch (InterruptedException e) {};
        
        for(int m = 0; m < seqLen; m++){
            
            computerLights[m].lightUpDiff(thumbs[JoeFlowSeq[m]]);           //light up each light in the computer row of the
                                                                            //results table with the corresponding colour in
                                                                            //the given sequence
            pane.validate();
            try{ Thread.sleep(500);} catch (InterruptedException e) {};     //0.5 seconds in between each light up
        }

        try{ Thread.sleep(1000);} catch (InterruptedException e) {};
        /*  wait 1 second, and then make the user results sequence and the computer
            results sequence blank in the following for loop
        */
        for(int n = 0; n < seqLen; n++){
            userLights[n].makeBlank();
            computerLights[n].makeBlank();
        }
    }
    
    /**
     * Returns a JPanel containing the row of Lights, made using custom component JLight.
     * 
     * @return  the JPanel which contains the row of lights to be used in the game panel
     * @see JLight
     */
    public JPanel setUpLightsRow() {
        
        JPanel outerPanel = new JPanel();
        JPanel lightPanel = new JPanel();
        
        outerPanel.setOpaque(false);
        lightPanel.setBackground(Color.WHITE);
        lightPanel.setBorder(new LineBorder(Color.BLACK, 4));
        
        
        JLight[] lights = new JLight[5];
        
        ImageIcon RedOn = new ImageIcon(getClass().getResource("/Images/Lights/RedLightOn.jpg"));
        ImageIcon RedOff = new ImageIcon(getClass().getResource("/Images/Lights/RedLightOff.jpg"));
        
        ImageIcon BlueOn = new ImageIcon(getClass().getResource("/Images/Lights/BlueLightOn.jpg"));
        ImageIcon BlueOff = new ImageIcon(getClass().getResource("/Images/Lights/BlueLightOff.jpg"));
        
        ImageIcon GreenOn = new ImageIcon(getClass().getResource("/Images/Lights/GreenLightOn.jpg"));
        ImageIcon GreenOff = new ImageIcon(getClass().getResource("/Images/Lights/GreenLightOff.jpg"));
        
        ImageIcon YellowOn = new ImageIcon(getClass().getResource("/Images/Lights/YellowLightOn.jpg"));
        ImageIcon YellowOff = new ImageIcon(getClass().getResource("/Images/Lights/YellowLightOff.jpg"));
        
        ImageIcon OrangeOn = new ImageIcon(getClass().getResource("/Images/Lights/OrangeLightOn.jpg"));
        ImageIcon OrangeOff = new ImageIcon(getClass().getResource("/Images/Lights/OrangeLightOff.jpg"));
        
        lights[0] = new JLight(RedOff, RedOn);
        lights[1] = new JLight(BlueOff, BlueOn);
        lights[2] = new JLight(GreenOff, GreenOn);
        lights[3] = new JLight(YellowOff, YellowOn);
        lights[4] = new JLight(OrangeOff, OrangeOn);
        
        for(int i = 0; i < 5; i++) {
            lightPanel.add(lights[i]);
        }
        
        outerPanel.add(Box.createHorizontalGlue());
        outerPanel.add(lightPanel);
        outerPanel.add(Box.createHorizontalGlue());
        
        return outerPanel;
    }
    
    /**
     * Returns a JPanel containing the row of JButtons, and keyboard shortcut indicators
     * 
     * @return  the JPanel which contains the row of JButtons to be used in the game panel
     */
    public JPanel setUpButtonsRow() {
        
        JPanel buttRow = new JPanel();
        buttRow.setOpaque(false);
        buttRow.setLayout(new GridLayout(1, 5)); //Grid layout with 1 row and 5 columns
        
        Action buttListen = new ButtonAction(); //Common Action Listener for all the buttons
        
        JButton[] buttons = new JButton[5];
        
        //Red Button
        buttons[0] = new JButton();
        buttons[0].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke('c'), "Anything1");      //Map to C button
        buttons[0].getActionMap().put("Anything1", buttListen);
        makeCustomButton(buttons[0], "/Images/Look and Feel/redButtonUnpressed.png",
                "/Images/Look and Feel/redButtonPressed.png");
        
        buttons[0].setName("Red");
        
        //Blue Button
        buttons[1] = new JButton();
        buttons[1].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke('v'), "Anything2");      //Map to V button     
        buttons[1].getActionMap().put("Anything2", buttListen);
        makeCustomButton(buttons[1], "/Images/Look and Feel/blueButtonUnpressed.png",
                "/Images/Look and Feel/blueButtonPressed.png");
        
        buttons[1].setName("Blue");
        
        //Green Button
        buttons[2] = new JButton();
        buttons[2].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke('b'), "Anything3");      //Map to B button
        buttons[2].getActionMap().put("Anything3", buttListen);
        makeCustomButton(buttons[2], "/Images/Look and Feel/greenButtonUnpressed.png",
                "/Images/Look and Feel/greenButtonPressed.png");
        
        buttons[2].setName("Green");
        
        //Yellow Button
        buttons[3] = new JButton();
        buttons[3].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke('n'), "Anything4");      //Map to N button
        buttons[3].getActionMap().put("Anything4", buttListen);
        makeCustomButton(buttons[3], "/Images/Look and Feel/yellowButtonUnpressed.png",
                "/Images/Look and Feel/yellowButtonPressed.png");
        
        buttons[3].setName("Yellow");
        
        //Orange Button
        buttons[4] = new JButton();
        buttons[4].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke('m'), "Anything5");      //Map to M button
        buttons[4].getActionMap().put("Anything5", buttListen);
        makeCustomButton(buttons[4], "/Images/Look and Feel/orangeButtonUnpressed.png",
                "/Images/Look and Feel/orangeButtonPressed.png");
        
        buttons[4].setName("Orange");
        
        
        //Add the buttons to the first row of the button Panel
        for(int i = 0; i < 5; i++) {
            
            buttRow.add(buttons[i]);
            buttons[i].addActionListener(buttListen);   //Assign the action listener
            buttons[i].setEnabled(false);               //default set to enabled
        }
        
        return buttRow;
    }
    
    /**
     * Returns a JPanel containing the results table, made using custom component JLights.
     * <p>
     * The results table is composed of multiple nested containers. The parent container,
     * the one that is returned by the function, is called <code>resultsPanel</code>.
     * The hierarchy is as follows:
     * <code>resultsPanel
     *          -computerSeqBox (JPanel)
     *              -computerTitle (JLabel)
     *              -computerSeq (JPanel)
     *                  -10 x blank JLights (JLight
     *          -userSeqBox (JPanel)
     *              -userTitle (JLabel)
     *              -userSeq (JPanel)
     *                  -10 x blank JLights (JLight)
     * </code>
     * 
     * @return  the JPanel which contains the results table to be used in the game panel
     */
    public JPanel setUpResultsPanel(){
        
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.PAGE_AXIS));
        setAbsoluteSize(resultsPanel, 650, 140);
        resultsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.setOpaque(false);
        
        JPanel computerSeqBox = new JPanel();
        computerSeqBox.setLayout(new BoxLayout(computerSeqBox,BoxLayout.X_AXIS));
        computerSeqBox.setBorder(new LineBorder(Color.BLACK, 1));
        setAbsoluteSize(computerSeqBox, 650, 55);
        computerSeqBox.setBackground(Color.WHITE);

        JLabel computerTitle = new JLabel("Joe Flow Says:");
        computerTitle.setFont(gameFonts("Gameplay", 12f));
        computerTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);         //Align text with right edge
        computerSeqBox.add(computerTitle);
        computerSeqBox.add(new JSeparator(SwingConstants.VERTICAL));
        
        JPanel userSeqBox = new JPanel();
        userSeqBox.setLayout(new BoxLayout(userSeqBox,BoxLayout.X_AXIS));
        userSeqBox.setBorder(new LineBorder(Color.BLACK, 1));
        setAbsoluteSize(userSeqBox, 650, 55);
        userSeqBox.setBackground(Color.WHITE);
        
        JLabel userTitle = new JLabel("You Say:");
        userTitle.setFont(gameFonts("Gameplay",12f));
        userTitle.setPreferredSize(new Dimension(                       //Make the width of the User Row Title
                computerTitle.getPreferredSize().width,                 // the same as the Computer Row Title            
                computerTitle.getPreferredSize().height));
        userTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);             //Align text with right edge
        userTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        userSeqBox.add(userTitle);
        userSeqBox.add(new JSeparator(SwingConstants.VERTICAL));
        
        
        ImageIcon blank = new ImageIcon(getClass().getResource("/Images/Lights/BlankLight.jpg"));
        
        userSeq = new JPanel();
        userSeq.setBackground(Color.WHITE);
        computerSeq = new JPanel();
        computerSeq.setBackground(Color.WHITE);
        
        for(int i = 0; i < 10; i++){

            userSeq.add(new JLight(blank));
            computerSeq.add(new JLight(blank));
        }
        
        userSeqBox.add(userSeq);
        computerSeqBox.add(computerSeq);
        
        resultsPanel.add(userSeqBox);
        resultsPanel.add(computerSeqBox);
        resultsPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                "Results", TitledBorder.LEFT, TitledBorder.LEFT, gameFonts("Gameplay",20f),
                Color.WHITE));
        
        return resultsPanel;
    }
    
    public JPanel setUpScorePanel(){

    JPanel scorePanel = new JPanel();
    scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
    setAbsoluteSize(scorePanel, 120, 100);
    scorePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    scorePanel.setOpaque(false);

    JPanel goalBox = new JPanel();
    goalBox.setLayout(new BoxLayout(goalBox,BoxLayout.X_AXIS));
    goalBox.setBorder(new LineBorder(Color.BLACK, 1));
    //setAbsoluteSize(goalBox, 120, 50);
    goalBox.setBackground(Color.WHITE);



    JPanel highScoreBox = new JPanel();
    highScoreBox.setLayout(new BoxLayout(highScoreBox,BoxLayout.X_AXIS));
    highScoreBox.setBorder(new LineBorder(Color.BLACK, 1));
    //setAbsoluteSize(highScoreBox, 120, 50);
    highScoreBox.setBackground(Color.WHITE);

    JLabel userTitle = new JLabel("High Score:");
    userTitle.setAlignmentX(Component.RIGHT_ALIGNMENT); 
    userTitle.setFont(gameFonts("Gameplay",12f));
    
    highScoreBox.add(userTitle);
    highScoreBox.add(new JSeparator(SwingConstants.VERTICAL));

    JLabel computerTitle = new JLabel("Goal:");
    computerTitle.setFont(gameFonts("Gameplay", 12f));
    computerTitle.setPreferredSize(new Dimension(                       //Make the width of the User Row Title
            userTitle.getPreferredSize().width,                 // the same as the Computer Row Title            
            userTitle.getPreferredSize().height));
    computerTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);             //Align text with right edge
    computerTitle.setHorizontalAlignment(SwingConstants.RIGHT);       //Align text with right edge
    
    goalBox.add(computerTitle);
    goalBox.add(new JSeparator(SwingConstants.VERTICAL));
    
    JLabel goalScore = new JLabel("10");
    goalScore.setFont(gameFonts("Gameplay",16f));
    
    highScore.setPreferredSize(new Dimension(                       //Make the width of the User Row Title
            goalScore.getPreferredSize().width,                 // the same as the Computer Row Title            
            goalScore.getPreferredSize().height));
    highScore.setHorizontalAlignment(SwingConstants.RIGHT);  
    
    highScoreBox.add(highScore);
    goalBox.add(goalScore);

    scorePanel.add(highScoreBox);
    scorePanel.add(goalBox);
    scorePanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
            "Score", TitledBorder.LEFT, TitledBorder.LEFT, gameFonts("Gameplay",20f),
            Color.WHITE));

    return scorePanel;
    
    }
    
    /**
     * Lights up a random Jlight in the lights panel every 850ms a specific number of times
     * <p>
     * This function uses a random integer generating function to light up one of the five
     * JLights in the lights panel for 500ms, and then light up another one 350ms later,
     * repeating this process for the number of times specified.
     * @param numTimes      Number of times a JLight will be lit up
     * @param lightsUse     An array of the 5 JLights in the lights panel
     * @return              the sequence that was lit up is returned in an int array
     * @see randomInteger
     */
    public int[] lightUp(int numTimes, JLight[] lightsUse) {
        
        int[] Seq = new int[10];

        for (int i = 0; i < numTimes; i++){
            
            Seq[i] = randomInteger(0,4);
            
            try{ Thread.sleep(350);} catch (InterruptedException e) {};
            
            lightsUse[Seq[i]].lightUp();
            switch(Seq[i]){
                case 0:
                    playSound("Red");
                    break;
                case 1:
                    playSound("Blue");
                    break;
                case 2:
                    playSound("Green");
                    break;
                case 3:
                    playSound("Yellow");
                    break;
                case 4:
                    playSound("Orange");
                    break;
            }
            try{ Thread.sleep(500);} catch (InterruptedException e) {};
                
            lightsUse[Seq[i]].lightOff();
        }

        return Seq;
    }
    
    public synchronized void playSound(final String Colour) {
        
        if(!volumeOFF){
            new Thread(new Runnable() {
            public void run() {
              try {
                Clip clip = AudioSystem.getClip();
                InputStream audioSrc = getClass().getResourceAsStream("/Sound/"+Colour+"Sound.wav");
                InputStream bufferedIn = new BufferedInputStream(audioSrc);
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedIn);
                clip.open(inputStream);
                clip.start(); 
              }catch (Exception e) {
                System.err.println(e.getMessage());
                System.out.println("fucked up");
              }
            }
          }).start();
        }
    }
    
    /**
     * Sets the minimumSize, maximumSize, preferredSize, and current Size of a
     * specified JComponent to the same width and height.
     * <p>
     * This function is used to overcome some limitations and undesired resizing
     * of components found in some of the UI layout managers.
     * @param comp      the JComponent to be resized
     * @param width     the desired width of the component; an integer
     * @param height    the desired height of the component; an integer
     */
    public void setAbsoluteSize(JComponent comp, int width, int height){
        Dimension d = new Dimension(width, height);
        
        comp.setPreferredSize(d);
        comp.setMinimumSize(d);
        comp.setMaximumSize(d);
    }
    
    /**
     * Customizes an existing JButton using custom graphics for the pressed and
     * unpressed states
     * 
     * @param butt          the JButton to customize
     * @param unpressed     the path and filename of the unpressed button image.
     *                      Must be found within the local class directory
     * @param pressed       the path and filename of the pressed button image.
     *                      Must be found within the local class directory
     */
    public void makeCustomButton(JButton butt, String unpressed, String pressed){
        butt.setIcon(new ImageIcon(getClass().getResource(unpressed)));
        butt.setPressedIcon(new ImageIcon(getClass().getResource(pressed)));
        butt.setDisabledIcon(new ImageIcon(getClass().getResource(unpressed)));
        butt.setOpaque(false);              //let unpainted areas of button show
                                            //the image below it
        butt.setContentAreaFilled(false);   //do not paint the entire JButton background
        butt.setBorderPainted(false);
        butt.setFocusPainted(false);
        
    }
    
    public void makeCustomButton(JToggleButton butt, String unpressed, String pressed){
        butt.setIcon(new ImageIcon(getClass().getResource(unpressed)));
        butt.setPressedIcon(new ImageIcon(getClass().getResource(pressed)));
        butt.setSelectedIcon(new ImageIcon(getClass().getResource(pressed)));

        butt.setOpaque(false);              //let unpainted areas of button show
                                            //the image below it
        butt.setContentAreaFilled(false);   //do not paint the entire JButton background
        butt.setBorderPainted(false);
        butt.setFocusPainted(false);
        
    }
    
    /**
     * Generates and returns a pseudorandom integer within the specified range
     * 
     * @param min   the smallest integer that could be generated, inclusive
     * @param max   the largest integer that could be generated, inclusive
     * @return      a random integer between min and max
     * @see java.util.Random
     */
    public int randomInteger(int min, int max) {

    Random rand = new Random();

    // nextInt excludes the top value so we have to add 1 to include the top value
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
    }
    
    /**
     * Returns a Font object with the specified font type and size
     * 
     * @param type  A String that can be either "Karmatic Arcade" or "Gameplay"
     * @param size  A float representation of the desired font size
     * @return      The Font object
     */
    public Font gameFonts(String type, float size){
        
        Font daFont;
        
        if("Karmatic Arcade".equals(type)){
            
            try{
                daFont = Font.createFont(Font.TRUETYPE_FONT,
                        getClass().getResourceAsStream("/Images/Look and Feel/ka1.ttf"));
            } catch(IOException | FontFormatException f){
                daFont = new Font("Arial", Font.PLAIN, 24);}    //default to Arial Size 24
            
            return daFont.deriveFont(size);
        }
        else if("Gameplay".equals(type)){
            
            try{
                daFont = Font.createFont(Font.TRUETYPE_FONT,
                        getClass().getResourceAsStream("/Images/Look and Feel/Gameplay.ttf"));
            } catch(IOException | FontFormatException f){
                daFont = new Font("Arial", Font.PLAIN, 24);}    //default to Arial Size 24   
            
            return daFont.deriveFont(size);
            
        }
        else{
            return new Font("Arial", Font.PLAIN, 24);          //default to Arial Size 24
        }
    }
    
    /**
     * Implements the ActionListener interface and controls content pane related
     * layout changes corresponding to various buttons throughout the game
     */
    public class PanelChangeListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractButton src = (AbstractButton) e.getSource();
            String buttonName = src.getName();
            
            Runnable game = new Runnable(){
                    @Override
                    public void run(){
                        startGame();
                    }
                };      //Runnable object initiates the startGame function
            Runnable start = new Runnable(){
                    @Override
                    public void run(){
                        initUI();
                    }
                };     //Runnable object initiates the initUI function
            
            //Action performed is dependent on which button click initiated the call to the interface
            if(null!= buttonName) switch (buttonName){
                case "Start":
                    playSound("Click");
                    player.stop();
                    new Thread(game).start();                                            //Start new thread to run game
                    break;
                case "Try Again":
                    playSound("Click");
                    pane.removeAll();
                    gameOverContainer.setVisible(false);    
                    new Thread(game).start();                                           //Start new thread tp run game
                    break;
                case "Main Menu":
                    playSound("Click");
                    pane.removeAll();
                    gameOverContainer.setVisible(false);
                    new Thread(start).start();                                         //Restart game in new thread
                    break;
                case "Quit":
                    gameOverContainer.setVisible(false);
                    System.exit(0);                                                   //Exit game
                    break;
                case "Help":
                    playSound("Click");
                    showHelpDialog();
                    break;
                case "Exit Help":
                    playSound("Click");
                    helpContainer.setVisible(false);
                    break;
                case "Prize":
                    WinGameContainer.setVisible(false);
                    System.exit(0);                                                   //Exit game
                    break;
                case "Volume":
                    
                    if(!volumeOFF && player.isRunning()){
                        player.stop();
                    }
                    if(volumeOFF && pane.getComponent(0).equals(startPanel)){
                        player.start();
                    }
                    volumeOFF = !volumeOFF;
                    break;
            }
        }
    }
    
    /**
     * Action Listener class responsible for gameplay related button events
     * <p>
     * When this action listener is called, the actionPerformed function will
     * light up the appropriate JLight thumbnail in the user Results Sequence,
     * update the global "responses" variable, increment the number of total
     * responses in the global "numResponses" variable, and notify the LOCK
     * object in order to check the while loop condition in the startGame function
     * again
     */
    public class ButtonAction extends AbstractAction{
        
        @Override
        public void actionPerformed(ActionEvent e){
            
            JLight currLight = (JLight) userSeq.getComponent(numResponses);     //numResponses would be the next blank light
            JButton buttonPressed = (JButton) e.getSource();
            String buttonName = buttonPressed.getName();                        //Get the name of the button pressed
            
            if(null != buttonName)switch (buttonName) {
                case "Red":
                    responses[numResponses] = 0;
                    currLight.lightUpDiff(thumbs[0]);
                    playSound("Red");
                    break;
                case "Blue":
                    responses[numResponses] = 1;
                    currLight.lightUpDiff(thumbs[1]);
                    playSound("Blue");
                    break;
                case "Green":
                    responses[numResponses] = 2;
                    currLight.lightUpDiff(thumbs[2]);
                    playSound("Green");
                    break;
                case "Yellow":
                    responses[numResponses] = 3;
                    currLight.lightUpDiff(thumbs[3]);
                    playSound("Yellow");
                    break;
                default:
                    responses[numResponses] = 4;
                    currLight.lightUpDiff(thumbs[4]);
                    playSound("Orange");
                    break;
            }
            numResponses++;
            synchronized(LOCK){
                LOCK.notifyAll();
            }
        }
    }
    
    /**
     * Class which extends JLabel, used in the game to display a dynamic light
     * icon which can be a certain colour, and can be lit up or turned off
     * 
     */
    public class JLight  extends JLabel {

        private final ImageIcon poff;
        private final ImageIcon pon;
        private final ImageIcon pblank;
        
        public JLight(ImageIcon off, ImageIcon on){
            poff = off;
            pon = on;
            pblank = off;
            setIcon(poff);
        }
        
        public JLight(ImageIcon neither){ //this constructor is used to make
                                          //placeholder JLights using a blank
            poff = neither;               //ImageIcon of the same dimensions
            pon = neither;                //as the the regular JLight icons
            pblank = neither;
            setIcon(neither);
        }
        
        public void lightUp(){
            setIcon(pon);
        }
        
        public void lightUpDiff(ImageIcon i){
            setIcon(i);
        }
        
        public void lightOff() {
            setIcon(poff);
            
        }
        
        public void makeBlank(){
            setIcon(pblank);
        }
    }
    
    /**
     * JGamePanel is an extension of JPanel that overrides the internal 
     * paintComponent method in order to use a custom image background that is
     * given as a filepath in the input parameters
     */
    public class JGamePanel extends JPanel{
        
        private String filePath;
        
        private JGamePanel(String path){
            filePath = path;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            BufferedImage bg = null;
                try {
                    bg = ImageIO.read(getClass().getResourceAsStream(filePath));
                } catch (IOException e){}
            
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, null);
        }
    }
   
    /**
     * MsgPanel manages the top-most information panel in the main game pane.
     * It includes public methods to show and remove text, and to display a timed
     * countdown before each level begins.
     */
    public class MsgPanel extends JPanel {
        
        String txt;
        JLabel displayText;
        private MsgPanel(String initText) {
            
            txt = initText;
            displayText = new JLabel(txt);

            displayText.setFont(gameFonts("Karmatic Arcade", 35f));
            displayText.setForeground(Color.WHITE);
            displayText.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.setOpaque(false);                      //allow background to show
            
            setUpVisual();
        }
        
        /*The following method ensures the MsgPanel instance does not collapse
        and change the layout appearance by reducing the size to zero when
        all the components are removed. A Box object with the same dimensions
        as the panel before the components were removed is added to hold the
        MsgPanel size.
        */
        public void removeText(){
            Dimension dim = this.getSize();
            this.removeAll();

            this.add(Box.createRigidArea(dim));        
            displayText.setText("");
        }
        
        public void setText(String Text){
            
            displayText.setText(Text);
            setUpVisual();
            this.validate();
        }
        
        //Display a 3,2,1 countdown with set intervals
        public void countDown(){
            setUpVisual();
            this.validate();
            try{ Thread.currentThread().sleep(500);} catch (InterruptedException e) {};
            displayText.setText("3");
            try{ Thread.currentThread().sleep(800);} catch (InterruptedException e) {};
            displayText.setText("2");
            try{ Thread.currentThread().sleep(800);} catch (InterruptedException e) {};
            displayText.setText("1");
            try{ Thread.currentThread().sleep(800);} catch (InterruptedException e) {};
            this.removeText();
            this.validate();
            
        }
        /*This method ensures the text is always displayed in the same area
        through the use of vertical struts
        */  
        private void setUpVisual(){
            
            this.removeAll();
            this.add(Box.createVerticalStrut(40));
            this.add(displayText);
            this.add(Box.createVerticalStrut(40));
        }
        
        
        
        
        
        
    }
    
    /**
     * Java file main function. Invokes a new Runnable object which instantiates
     * an instance of the JoeFlowSays class.
     * @param args 
     */
    public static void main(String[] args) {
        
        
        EventQueue.invokeLater(new Runnable() {
           
            @Override
            public void run() {
                JoeFlowSays jfs = new JoeFlowSays();
            }
        });
    
    }
    
    
}