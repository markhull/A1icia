/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.swingconsole;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.shared.SharedUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import redis.clients.jedis.Jedis;

/**
 * The Swing console window.
 * 
 * @author hulles
 */
class ConsoleWindow extends JFrame implements KeyListener, ActionListener {
	final static Logger LOGGER = Logger.getLogger("AlixiaSwingConsole.ConsoleWindow");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
//	final static Level LOGLEVEL = Level.INFO;
    private static final long serialVersionUID = 1L;
    private JTextArea displayArea;
    private JTextField typingArea;
    private final SimpleAttributeSet defaultAttrs;
	private final JebusPool jebusPool;
    private final SwingConsole console;
	private final String historyKey;
	private int historyIx;
	private final static int HISTORYLIMIT = 255;
    static final String NEWLINE = System.getProperty("line.separator");

    ConsoleWindow(String name, SwingConsole console) {
        super(name);

        SharedUtils.checkNotNull(name);
        SharedUtils.checkNotNull(console);
        this.console = console;
        defaultAttrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(defaultAttrs, "SansSerif");
        StyleConstants.setFontSize(defaultAttrs, 14);
		jebusPool = JebusHub.getJebusLocal();
		historyKey = JebusBible.getStringKey(JebusBible.JebusKey.ALIXIASWINGHISTORYKEY, jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			// start fresh, although we *could* retain history, thanks to Redis
			jebus.del(historyKey);
		}		
		historyIx = 0;
    }

    void addComponentsToPane() {

        JButton button = new JButton("Clear");
        button.addActionListener(this);

        typingArea = new JTextField(60);
        typingArea.addKeyListener(this);

        //Uncomment this if you wish to turn off focus
        //traversal.  The focus subsystem consumes
        //focus traversal keys, such as Tab and Shift Tab.
        //If you uncomment the following line of code, this
        //disables focus traversal and the Tab events will
        //become available to the key event listener.
        //typingArea.setFocusTraversalKeysEnabled(false);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        getContentPane().add(typingArea, BorderLayout.PAGE_START);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(button, BorderLayout.PAGE_END);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        int keycode;
        String text;
        long maxHistory;

        SharedUtils.checkNotNull(event);
        keycode = event.getKeyCode();
        switch (keycode) {
            case 10:
                // enter key
                LOGGER.log(LOGLEVEL, "Received ENTER key");
                text = typingArea.getText();
                handleEnter(text);
                event.consume();
                break;
            case 38:
                // up arrow
                LOGGER.log(LOGLEVEL, "Received UP ARROW");
                try (Jedis jebus = jebusPool.getResource()) {
                    maxHistory = jebus.llen(historyKey);
                    if (historyIx < maxHistory) {
                        text = jebus.lindex(historyKey, historyIx);
                        typingArea.setText(text);
                        historyIx++;
                    } else {
                        historyIx = 0;
                        typingArea.setText("");
                    }
                }
                event.consume();
                break;
            case 40:
                // down arrow
                LOGGER.log(LOGLEVEL, "Received DOWN ARROW");
                try (Jedis jebus = jebusPool.getResource()) {
                    if (historyIx-- > 0) {
                        text = jebus.lindex(historyKey, historyIx);
                        typingArea.setText(text);
                    } else {
                        historyIx = 0;
                        typingArea.setText("");
                    }
                }
                event.consume();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void handleEnter(String text) {

        SharedUtils.checkNotNull(text);
        LOGGER.log(LOGLEVEL, "Handling text line");
        displayTextln(text);
        try (Jedis jebus = jebusPool.getResource()) {
            jebus.lpush(historyKey, text);
            jebus.ltrim(historyKey, 0, HISTORYLIMIT);
        }		
        historyIx = 0;
        if (text == null) {
            return;
        }
        if (console.command(text)) {
            clearTypingArea();
            return;
        }
        if (!console.sendText(text)) {
            displayTextln("Can't communicate with Alixia Central");
        }
        clearTypingArea();

    }

    private void clearTypingArea() {

        typingArea.setText("");
        typingArea.requestFocusInWindow();
    }

    void displayText(String text) {			

        SharedUtils.checkNotNull(text);
        displayArea.append(text);
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    void displayTextln() {

        displayArea.append(NEWLINE);
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }
    void displayTextln(String text) {

        SharedUtils.checkNotNull(text);
        displayArea.append(text);
        displayArea.append(NEWLINE);
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    /** Handle the button click. */
    @Override
    public void actionPerformed(ActionEvent e) {
        
        displayArea.setText("");
        typingArea.setText("");
        typingArea.requestFocusInWindow();
    }
}
