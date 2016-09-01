package joeflowsays;


import java.util.Random;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import javax.imageio.ImageIO;
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
import javax.swing.JLayeredPane;
import static javax.swing.JLayeredPane.FRAME_CONTENT_LAYER;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class JoeFlowSays extends JFrame{

    private Container pane;
    private JPanel startPanel;
    private JPanel gamePanel;
    private JPanel userSeq;
    private JPanel computerSeq;
    private int[] responses = new int[10];
    private int numResponses;
    private ImageIcon[] thumbs;
    Object LOCK = new Object();
    private ImageIcon JoeIcon = new ImageIcon(getClass().getResource("/Images/Look and Feel/GameOverIcon.png"));
    private PanelChangeListener PCListener = new PanelChangeListener();
    private JDialog gameOverContainer;
    
    public JoeFlowSays() {
        
        initUI();
    }
    
    private void initUI() {
        
        pane = getContentPane();
        
        
        startPanel = getStartPanel();
        
        
        
        
        BufferedImage JFlowIcon = null;
            try {
                JFlowIcon = ImageIO.read(new File(getClass().
                        getResource("/Images/Look and Feel/WindowIcon.png").toURI()));
            } catch (IOException e){
            } catch (URISyntaxException u){    
            }
        
        pack();
        setLayout(new BorderLayout());
        setResizable(false);
        setTitle("Joe Flow Says!");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(JFlowIcon);
        this.setVisible(true);
        
        JToolBar topMenuBar = getToolBar();
        pane.add(topMenuBar, BorderLayout.PAGE_START);
        pane.add(startPanel, BorderLayout.CENTER);
        setSize(750,750+topMenuBar.getHeight());
        
        thumbs = new ImageIcon[5];
        
        thumbs[0] = new ImageIcon(getClass().getResource("/Images/Lights/RedLightSmall.jpg"));
        thumbs[1] = new ImageIcon(getClass().getResource("/Images/Lights/BlueLightSmall.jpg"));
        thumbs[2] = new ImageIcon(getClass().getResource("/Images/Lights/GreenLightSmall.jpg"));
        thumbs[3] = new ImageIcon(getClass().getResource("/Images/Lights/YellowLightSmall.jpg"));
        thumbs[4] = new ImageIcon(getClass().getResource("/Images/Lights/OrangeLightSmall.jpg"));
        
    }
    
    private JToolBar getToolBar(){
        JToolBar menuBar = new JToolBar();
        menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.X_AXIS));
        
        JButton help = new JButton("Help");
        JButton about = new JButton("About");
        
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(help);
        menuBar.addSeparator();
        menuBar.add(about);
        menuBar.setFloatable(false);
        menuBar.setRollover(true);
        menuBar.setBackground(Color.DARK_GRAY);
        
        return menuBar;
    }
    
    private JGamePanel getStartPanel() {
        
        JGamePanel sP = new JGamePanel("/Images/Look and Feel/backgroundMain.jpg");
        BoxLayout bl = new BoxLayout(sP, BoxLayout.Y_AXIS);
        
        sP.setLayout(bl);
        
        JButton startButt = new JButton();
        startButt.setName("Start");
        makeCustomButton(startButt,"/Images/Look and Feel/startButtonUnpressed.png", "/Images/Look and Feel/startButtonPressed.png");
        startButt.setAlignmentX(Box.CENTER_ALIGNMENT);
        startButt.addActionListener(PCListener);
        
        sP.add(Box.createVerticalStrut(550));
        sP.add(startButt);
 
        return sP;
    }
    
    private void startGame() {
        gamePanel = new JGamePanel("/Images/Look and Feel/backgroundGame.jpg");
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        
        JPanel lightsPanel = setUpLightsRow();
        JPanel lightsRow = (JPanel) lightsPanel.getComponent(1);
        
        JLight[] lights = new JLight[5];
        int k = 0;
       
        for (Component jC : lightsRow.getComponents()){
            lights[k++] = (JLight) jC;
        }

        JPanel buttonRow = setUpButtonsRow();
        buttonRow.setAlignmentY(Container.CENTER_ALIGNMENT);
        
        JButton[] buttons = new JButton[5];
       
        for (int j = 0; j < 5; j++) {
            buttons[j] = (JButton) buttonRow.getComponent(j);
        }

        JPanel resultsPanel = setUpResultsPanel();
        
        MsgPanel topPanel = new MsgPanel("Ready?");
        
        gamePanel.add(topPanel);
        gamePanel.add(lightsPanel);
        gamePanel.add(Box.createVerticalStrut(40));
        gamePanel.add(buttonRow);
        gamePanel.add(Box.createVerticalStrut(70));
        gamePanel.add(resultsPanel);
        gamePanel.add(Box.createVerticalStrut(20));
        
        
        if (pane.getComponentCount() > 0 && pane.getComponent(1).equals(startPanel)){
            pane.remove(startPanel);
        }
        pane.add(gamePanel);
        pane.validate();
        
        try{ Thread.currentThread().sleep(1800);} catch (InterruptedException e) {};
        topPanel.removeText();
        
        pane.validate();
        
        boolean stillWinning = true;
        int seqLength = 1;
        int[] lightSeq;
        while(stillWinning && seqLength < 10){
            
            try{ Thread.currentThread().sleep(700);} catch (InterruptedException e) {};
            topPanel.setText("Level " + Integer.toString(seqLength));
            try{ Thread.currentThread().sleep(700);} catch (InterruptedException e) {};
            topPanel.removeText();
            try{ Thread.currentThread().sleep(200);} catch (InterruptedException e) {};
            topPanel.countDown();
            
            lightSeq = lightUp(seqLength, lights);
            
            for (JButton b : buttons){ b.setEnabled(true);}
            
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
            
            showResults(lightSeq, seqLength);

            if(Arrays.equals(lightSeq, responses)){
                System.out.println("Correct!");
                seqLength++;
                numResponses = 0;
                for(int i = 0; i<10; i++){responses[i] = 0;}
            }
            else{
                System.out.println("Wrong!");
                stillWinning = false;
                seqLength=1;
                numResponses = 0;
                for(int i = 0; i<10; i++){responses[i] = 0;}
            }
        }
   
        if (!stillWinning){
            showGameOverDialog();
        }

    }
    
    private void showGameOverDialog(){
        
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
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(true);
        JLabel msg = new JLabel("That's not what I said! Try Again?", JLabel.CENTER);
        msg.setVerticalAlignment(SwingConstants.CENTER);
        msg.setFont(new Font("Gameplay Regular",Font.PLAIN,15));
        messagePanel.add(msg);
        
        JOptionPane gameOver = new JOptionPane(messagePanel, JOptionPane.ERROR_MESSAGE,
        JOptionPane.YES_NO_CANCEL_OPTION, JoeIcon, Options, Options[0]);
        
        gameOverContainer = new JDialog(this, "Joe Flow Says", true);
        gameOverContainer.setContentPane(gameOver);
        gameOverContainer.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        gameOverContainer.getContentPane().setBackground(Color.WHITE);
        makeComponentsWhiteBG(gameOverContainer);
        gameOverContainer.pack();
        gameOverContainer.setLocationRelativeTo(this);
        gameOverContainer.setVisible(true);
        
        
        
    }
    
    private void makeComponentsWhiteBG(Container c){
        
        Component[] m = c.getComponents();

        for(int i = 0; i < m.length; i++){
                m[i].setBackground(Color.WHITE);

            if(c.getClass().isInstance(m[i]));
                makeComponentsWhiteBG((Container)m[i]);
        }
    }
    
    private void showResults(int[] JoeFlowSeq, int seqLen){
        
        try{ Thread.sleep(500);} catch (InterruptedException e) {};
        
        JLight[] userLights = new JLight[10];
        JLight[] computerLights = new JLight[10];
        
        ImageIcon blank = new ImageIcon(getClass().getResource("/Images/Lights/BlankLight.jpg"));

        
        for(int j = 0; j < 10; j++){
            
            userLights[j] = (JLight) userSeq.getComponent(j);
            computerLights[j] = (JLight) computerSeq.getComponent(j);
        }
        
        for(int m = 0; m < seqLen; m++){
            
            computerLights[m].lightUpDiff(thumbs[JoeFlowSeq[m]]);
            
            pane.validate();
            
            try{ Thread.sleep(600);} catch (InterruptedException e) {};
        }

        try{ Thread.sleep(1000);} catch (InterruptedException e) {};
        
        for(int n = 0; n < seqLen; n++){
            userLights[n].makeBlank();
            computerLights[n].makeBlank();
        }
    }
        
    private JPanel setUpLightsRow() {
        
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
    
    private JPanel setUpButtonsRow() {
        
        JPanel buttRow = new JPanel();
        buttRow.setOpaque(false);
        buttRow.setLayout(new GridLayout(2, 5));
        
        Action buttListen = new ButtonAction();
        
        JButton[] buttons = new JButton[5];
        
        buttons[0] = new JButton();
        buttons[0].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('c'), "Anything1");
        buttons[0].getActionMap().put("Anything1", buttListen);
        makeCustomButton(buttons[0], "/Images/Look and Feel/redButtonUnpressed.png", "/Images/Look and Feel/redButtonPressed.png");
        buttons[0].setName("Red");
        
        buttons[1] = new JButton();
        buttons[1].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('v'), "Anything2");
        buttons[1].getActionMap().put("Anything2", buttListen);
        makeCustomButton(buttons[1], "/Images/Look and Feel/blueButtonUnpressed.png", "/Images/Look and Feel/blueButtonPressed.png");
        buttons[1].setName("Blue");
        
        buttons[2] = new JButton();
        buttons[2].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('b'), "Anything3");
        buttons[2].getActionMap().put("Anything3", buttListen);
        makeCustomButton(buttons[2], "/Images/Look and Feel/greenButtonUnpressed.png", "/Images/Look and Feel/greenButtonPressed.png");
        buttons[2].setName("Green");
        
        buttons[3] = new JButton();
        buttons[3].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('n'), "Anything4");
        buttons[3].getActionMap().put("Anything4", buttListen);
        makeCustomButton(buttons[3], "/Images/Look and Feel/yellowButtonUnpressed.png", "/Images/Look and Feel/yellowButtonPressed.png");
        buttons[3].setName("Yellow");
        
        buttons[4] = new JButton();
        buttons[4].getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('m'), "Anything5");
        buttons[4].getActionMap().put("Anything5", buttListen);
        makeCustomButton(buttons[4], "/Images/Look and Feel/orangeButtonUnpressed.png", "/Images/Look and Feel/orangeButtonPressed.png");
        buttons[4].setName("Orange");
        
        
        
        for(int i = 0; i < 5; i++) {
            buttRow.add(buttons[i]);
            buttons[i].addActionListener(buttListen);
            buttons[i].setEnabled(false);
            
        }
        
        ImageIcon CKey = new ImageIcon(getClass().getResource("/Images/Keyboard Shortcuts/c.png"));
        ImageIcon VKey = new ImageIcon(getClass().getResource("/Images/Keyboard Shortcuts/v.png"));
        ImageIcon BKey = new ImageIcon(getClass().getResource("/Images/Keyboard Shortcuts/b.png"));
        ImageIcon NKey = new ImageIcon(getClass().getResource("/Images/Keyboard Shortcuts/n.png"));
        ImageIcon MKey = new ImageIcon(getClass().getResource("/Images/Keyboard Shortcuts/m.png"));
        
        JLabel[] buttKeyLabels = new JLabel[5];
        
        buttKeyLabels[0] = new JLabel(CKey);
        buttKeyLabels[1] = new JLabel(VKey);
        buttKeyLabels[2] = new JLabel(BKey);
        buttKeyLabels[3] = new JLabel(NKey);
        buttKeyLabels[4] = new JLabel(MKey);
        
        for(int ii = 0; ii < 5; ii++){
            buttRow.add(buttKeyLabels[ii]);
        }
        
        return buttRow;
    }
    
    private JPanel setUpResultsPanel(){
        
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.PAGE_AXIS));
        setAbsoluteSize(resultsPanel, 620, 140);
        resultsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.setOpaque(false);
        
        JPanel computerSeqBox = new JPanel();
        computerSeqBox.setLayout(new BoxLayout(computerSeqBox,BoxLayout.X_AXIS));
        computerSeqBox.setBorder(new LineBorder(Color.BLACK, 1));
        setAbsoluteSize(computerSeqBox, 620, 55);
        computerSeqBox.setBackground(Color.WHITE);

        JLabel computerTitle = new JLabel("Joe Flow Says:");
        computerTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);
        computerSeqBox.add(computerTitle);
        computerSeqBox.add(new JSeparator(SwingConstants.VERTICAL));
        
        
        JPanel userSeqBox = new JPanel();
        userSeqBox.setLayout(new BoxLayout(userSeqBox,BoxLayout.X_AXIS));
        userSeqBox.setBorder(new LineBorder(Color.BLACK, 1));
        setAbsoluteSize(userSeqBox, 620, 55);
        userSeqBox.setBackground(Color.WHITE);
        
        JLabel userTitle = new JLabel("You Say:");
        userTitle.setPreferredSize(new Dimension(computerTitle.getPreferredSize().width, computerTitle.getPreferredSize().height));
        userTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);
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
                "Results", TitledBorder.LEFT, TitledBorder.LEFT, new Font("results Font", Font.BOLD, 15),
                Color.WHITE));
        
        return resultsPanel;
    }
    
    private int[] lightUp(int numTimes, JLight[] lightsUse) {
        int[] Seq = new int[10];
        String holdText;
        String lightUpIcon = "";
        
        for (int i = 0; i < numTimes; i++){
            Seq[i] = randomInteger(0,4);
            
            try{ Thread.sleep(350);} catch (InterruptedException e) {};
            
            lightsUse[Seq[i]].lightUp();
           
            try{ Thread.sleep(500);} catch (InterruptedException e) {};
                
            lightsUse[Seq[i]].lightOff();
            
            
        }

        return Seq;
    }
    
    private void setAbsoluteSize(JComponent comp, int width, int height){
        Dimension d = new Dimension(width, height);
        
        comp.setPreferredSize(d);
        comp.setMinimumSize(d);
        comp.setMaximumSize(d);
    }
    
    private void makeCustomButton(JButton butt, String unpressed, String pressed){
        butt.setIcon(new ImageIcon(getClass().getResource(unpressed)));
        butt.setPressedIcon(new ImageIcon(getClass().getResource(pressed)));
        butt.setDisabledIcon(new ImageIcon(getClass().getResource(unpressed)));
        butt.setOpaque(false);
        butt.setContentAreaFilled(false);
        butt.setBorderPainted(false);
        butt.setFocusPainted(false);
    }
    
    public int randomInteger(int min, int max) {

    Random rand = new Random();

    // nextInt excludes the top value so we have to add 1 to include the top value
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
    }
    
    private class PanelChangeListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton src = (JButton) e.getSource();
            String buttonName = src.getName();
            
            Runnable game = new Runnable(){
                    @Override
                    public void run(){
                        startGame();
                    }
                };
            Runnable start = new Runnable(){
                    @Override
                    public void run(){
                        initUI();
                    }
                };
            
            if(null!= buttonName) switch (buttonName){
                case "Start":
                    new Thread(game).start();
                    break;
                case "Try Again":
                    pane.removeAll();
                    gameOverContainer.setVisible(false);
                    new Thread(game).start();
                    break;
                case "Main Menu":
                    pane.removeAll();
                    gameOverContainer.setVisible(false);
                    new Thread(start).start();
                    break;
                case "Quit":
                    gameOverContainer.setVisible(false);
                    System.exit(0);
                    break;
            }
        }
    }
    
    private class ButtonAction extends AbstractAction{
        
        @Override
        public void actionPerformed(ActionEvent e){
            
            JLight currLight = (JLight) userSeq.getComponent(numResponses);
            JButton buttonPressed = (JButton) e.getSource();
            String buttonName = buttonPressed.getName();
            
            if(null != buttonName)switch (buttonName) {
                case "Red":
                    responses[numResponses] = 0;
                    currLight.lightUpDiff(thumbs[0]);
                    break;
                case "Blue":
                    responses[numResponses] = 1;
                    currLight.lightUpDiff(thumbs[1]);
                    break;
                case "Green":
                    responses[numResponses] = 2;
                    currLight.lightUpDiff(thumbs[2]);
                    break;
                case "Yellow":
                    responses[numResponses] = 3;
                    currLight.lightUpDiff(thumbs[3]);
                    break;
                default:
                    responses[numResponses] = 4;
                    currLight.lightUpDiff(thumbs[4]);
                    break;
            }
            numResponses++;
            synchronized(LOCK){
                LOCK.notifyAll();
            }
        }
    }
    
    private class JLight  extends JLabel {

        private final ImageIcon poff;
        private final ImageIcon pon;
        private final ImageIcon pblank;
        
        public JLight(ImageIcon off, ImageIcon on){
            poff = off;
            pon = on;
            pblank = off;
            setIcon(poff);
        }
        
        public JLight(ImageIcon neither){
            
            poff = neither;
            pon = neither;
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
    
    private class JGamePanel extends JPanel{
        
        private String filePath;
        
        private JGamePanel(String path){
            filePath = path;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            BufferedImage bg = null;
                try {
                    bg = ImageIO.read(new File(getClass().getResource(filePath).toURI()));
                } catch (IOException e){
                } catch (URISyntaxException u){
                }
            
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, null);
        }
    }
   
    private class MsgPanel extends JPanel {
        
        String txt;
        JLabel displayText;
        private MsgPanel(String initText) {
            
            txt = initText;
            
            displayText = new JLabel(txt);
            Font msgFont;
            try{
                msgFont = Font.createFont(Font.TRUETYPE_FONT,
                        new File(getClass().getResource("/Images/Look and Feel/ka1.ttf").toURI()));
            } catch(IOException | URISyntaxException | FontFormatException f){
                msgFont = new Font("Arial", Font.PLAIN, 35);}

            displayText.setFont(msgFont.deriveFont(35f));
            displayText.setForeground(Color.WHITE);
            displayText.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.setOpaque(false);
            
            setUpVisual();
        }
        
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
        
        private void setUpVisual(){
            
            this.removeAll();
            this.add(Box.createVerticalStrut(40));
            this.add(displayText);
            this.add(Box.createVerticalStrut(40));
        }
        
        
        
        
        
        
    }
    
    public static void main(String[] args) {
        
        
        EventQueue.invokeLater(new Runnable() {
           
            @Override
            public void run() {
                JoeFlowSays jfs = new JoeFlowSays();
                //jfs.setVisible(true);
            }
        });
    
    }
    
    
}