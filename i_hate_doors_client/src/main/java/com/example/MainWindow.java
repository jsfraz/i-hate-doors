package com.example;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MainWindow extends JFrame {
    private BufferedImage icon; // icon

    private JPanel panel1;
    private JLabel ipLabel;
    private JTextField ipField;
    private JButton ipOkButton;
    private JButton findButton;
    private JButton testButton;
    // panel2
    private JPanel panel2;
    private JRadioButton muteMicRadio;
    private JButton muteMicBindButton;
    private JRadioButton muteSoundRadio;
    private JButton muteSoundBindButton;
    // panel3
    private JPanel panel4;
    private JCheckBox toggleBindCheckBox;
    private JButton toggleBindButton;
    // panel4
    private JPanel panel5;
    private JButton okButton;
    private JButton onOffButton;
    private JButton exitButton;

    private String oldIp;
    private JDialog searchDialog;
    private DiscoverThread discoverThread;

    // regex pattern:
    // https://mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/
    private static final Pattern ipv4Pattern = Pattern
            .compile("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");

    // constructor
    public MainWindow() {
        /*
         * Swing layouts:
         * https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
         */

        // basic window setup
        super("I hate doors");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);
        setLayout(new GridBagLayout());

        // components

        // TODO align to center
        // panel1 (sensor IP)
        GridBagConstraints tableConstraints1 = new GridBagConstraints();
        tableConstraints1.fill = GridBagConstraints.HORIZONTAL;
        tableConstraints1.gridx = 0;
        tableConstraints1.gridy = 0;
        tableConstraints1.gridwidth = 2;
        // initializing and adding components
        panel1 = new JPanel(new FlowLayout());
        add(panel1, tableConstraints1);
        ipLabel = new JLabel("Sensor IP:");
        panel1.add(ipLabel);
        ipField = new JTextField(10);
        ipField.setHorizontalAlignment(SwingConstants.CENTER);
        ipField.setText(SettingsSingleton.GetInstance().getIp());
        ipField.setCaretPosition(ipField.getText().length());
        ipField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                // nothing
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                if (SettingsSingleton.GetInstance().getIp().equals(ipField.getText())) {
                    ipOkButton.setEnabled(false);
                } else {
                    if (ipOkButton.isEnabled() == false) {
                        ipOkButton.setEnabled(true);
                    }
                }

                Matcher matcher = ipv4Pattern.matcher(ipField.getText());
                if (matcher.matches())
                    testButton.setEnabled(true);
                else
                    testButton.setEnabled(false);
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                // nothing as well
            }
        });
        panel1.add(ipField);
        ipOkButton = new JButton("OK");
        ipOkButton.setEnabled(false);
        ipOkButton.addActionListener(e -> handleIpOkButton(e));
        panel1.add(ipOkButton);
        findButton = new JButton("Find");
        findButton.addActionListener(e -> handleFindButton(e));
        panel1.add(findButton);
        testButton = new JButton("Test");
        testButton.addActionListener(e -> handleTestButton(e));
        Matcher matcher = ipv4Pattern.matcher(ipField.getText());
        if (matcher.matches() == false)
            testButton.setEnabled(false);
        panel1.add(testButton);

        // panel2 (action)
        tableConstraints1.gridx = 0;
        tableConstraints1.gridy = 1;
        tableConstraints1.gridwidth = 1;
        // initializing and adding components
        panel2 = new JPanel(new GridBagLayout());
        panel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Mute option"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(panel2, tableConstraints1);
        // panel grid
        GridBagConstraints tableConstraints2 = new GridBagConstraints();
        tableConstraints2.fill = GridBagConstraints.HORIZONTAL;
        muteMicRadio = new JRadioButton("Microphone");
        muteMicRadio.addActionListener(e -> handleMuteRadio(e));
        tableConstraints2.gridx = 0;
        tableConstraints2.gridy = 0;
        panel2.add(muteMicRadio, tableConstraints2);
        muteMicBindButton = new JButton("###");
        muteMicBindButton.addActionListener(e -> handleMuteMicBindButton(e));
        tableConstraints2.gridx = 1;
        tableConstraints2.gridy = 0;
        panel2.add(muteMicBindButton, tableConstraints2);
        muteSoundRadio = new JRadioButton("Sound");
        muteSoundRadio.addActionListener(e -> handleMuteRadio(e));
        tableConstraints2.gridx = 0;
        tableConstraints2.gridy = 1;
        panel2.add(muteSoundRadio, tableConstraints2);
        muteSoundBindButton = new JButton("###");
        muteSoundBindButton.addActionListener(e -> handleMuteSoundBindButton(e));
        tableConstraints2.gridx = 1;
        tableConstraints2.gridy = 1;
        panel2.add(muteSoundBindButton, tableConstraints2);

        // panel3 (settings)
        tableConstraints1.gridx = 1;
        tableConstraints1.gridy = 1;
        tableConstraints1.gridwidth = 2;
        // initializing and adding components
        panel4 = new JPanel(new FlowLayout());
        panel4.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Settings"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(panel4, tableConstraints1);
        toggleBindCheckBox = new JCheckBox("Toggle key");
        toggleBindCheckBox.setSelected(SettingsSingleton.GetInstance().getToggleButton());
        toggleBindCheckBox.addActionListener(e -> handleToggleKeyRadio(e));
        panel4.add(toggleBindCheckBox);
        toggleBindButton = new JButton("###");
        toggleBindButton.addActionListener(e -> handleToggleBindButton(e));
        if (!SettingsSingleton.GetInstance().getToggleButton())
            toggleBindButton.setEnabled(false);
        panel4.add(toggleBindButton);

        // TODO align to center
        // panel4 (ok and exit buttons)
        tableConstraints1.gridx = 0;
        tableConstraints1.gridy = 3;
        // initializing and adding components
        panel5 = new JPanel(new FlowLayout());
        add(panel5, tableConstraints1);
        okButton = new JButton("OK");
        okButton.addActionListener(e -> handleOkButton(e));
        panel5.add(okButton);
        onOffButton = new JButton("###");
        panel5.add(onOffButton);
        exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> handleExitButton(e));
        panel5.add(exitButton);

        // tray icon: https://github.com/evandromurilo/system_tray_example
        try {
            icon = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("door.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                minimizeToTray();
            }
        });

        // program icon
        if (icon != null)
            setIconImage(icon);

        setMuteRadioValues();
        setButtonTexts();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        requestFocus();
    }

    // minimalizes app on tray
    private void minimizeToTray() {
        System.out.println("Adding icon to tray...");
        if (!SystemTray.isSupported()) {
            System.out.println("Tray icon not supported.");
        }

        PopupMenu popup = new PopupMenu();
        // tray icon was distorted:
        // https://stackoverflow.com/questions/12287137/system-tray-icon-looks-distorted
        final TrayIcon trayIcon = new TrayIcon(
                icon.getScaledInstance(new TrayIcon(icon).getSize().width, -1, Image.SCALE_SMOOTH));
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem openItem = new MenuItem("Settings");
        MenuItem closeItem = new MenuItem("Exit");

        popup.add(openItem);
        popup.add(closeItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
            setVisible(false);
            System.out.println("Tray icon added.");
        } catch (AWTException e) {
            System.out.println("Unable to add tray icon.");
            e.printStackTrace();
        }

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand() != null && actionEvent.getActionCommand().equals("Exit")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
                System.out.println("Opening from tray...");
                setVisible(true);
                tray.remove(trayIcon);
            }
        };

        popup.addActionListener(listener);
        trayIcon.addActionListener(listener);
    }

    // ipOk button
    private void handleIpOkButton(ActionEvent event) {
        Matcher matcher = ipv4Pattern.matcher(ipField.getText());
        if (matcher.matches()) {
            SettingsSingleton.GetInstance().setIp(ipField.getText());
            try {
                SettingsSingleton.GetInstance().saveSettings();

                JOptionPane.showMessageDialog(this, "IP adress successfully set.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                ipOkButton.setEnabled(false);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "IP adress is not valid.", "Error", JOptionPane.ERROR_MESSAGE);
            ipField.setText(SettingsSingleton.GetInstance().getIp());
            ipOkButton.setEnabled(false);
        }
    }

    // find button
    private void handleFindButton(ActionEvent event) {
        findButton.setEnabled(false);
        oldIp = SettingsSingleton.GetInstance().getIp();
        searchDialog = new JDialog(this, "Searching");
        discoverThread = new DiscoverThread(this);
        searchDialog.setResizable(false);
        searchDialog.add(getSearchDialgoPanel());
        searchDialog.setLocationRelativeTo(this);
        searchDialog.pack();
        searchDialog.setVisible(true);
        searchDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                searchEndMessage();
            }
        });

        discoverThread.start();
    }

    public void searchEndMessage() {
        searchDialog.dispose();
        discoverThread.stopRunning();
        DiscoverData data = discoverThread.getDisoverData();
        if (data != null) {
            String message = "";
            String title = "";
            int messageType = 0;
            if (oldIp.equals(data.ip)) {
                message = "IP address wasn't changed.";
                title = "Warning";
                messageType = JOptionPane.WARNING_MESSAGE;
            } else {
                System.out.println("Adding " + data.hostname + " (" + data.ip + ").");
                SettingsSingleton.GetInstance().setIp(data.ip);
                try {
                    SettingsSingleton.GetInstance().saveSettings();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                message = "Found!";
                title = "Info";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                ipField.setText(data.ip);
                ipField.setCaretPosition(ipField.getText().length());
            }
            sendBroadcastEndMessage(data.ip);
            JOptionPane.showMessageDialog(this, message, title, messageType);
        }
        findButton.setEnabled(true);
    }

    // send MQTT message for end of discovery broadcasting
    private void sendBroadcastEndMessage(String ip) {
        try {
            new Mqtt(ip, "sensor/commands")
                    .publish(Tools.objectToJson(new Message(MessageType.stopBroadcast)));
        } catch (MqttException e) {
            JOptionPane.showMessageDialog(this,
                    "Device was found but connecting to MQTT broker failed. Verify that service is running or check your configuration.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // test button
    private void handleTestButton(ActionEvent event) {
        testButton.setEnabled(false);
        boolean success = new Mqtt(SettingsSingleton.GetInstance().getIp(), "sensor/commands").testConnection();
        String title;
        String message;
        int icon;
        if (success) {
            title = "Info";
            message = "Success!";
            icon = JOptionPane.INFORMATION_MESSAGE;
        } else {
            title = "Error";
            message = "Unable to connect to MQTT broker. Verify that service is running or check your configuration.";
            icon = JOptionPane.ERROR_MESSAGE;
        }
        JOptionPane.showMessageDialog(this, message, title, icon);
        testButton.setEnabled(true);
    }

    // setting mute radio button values
    private void setMuteRadioValues() {
        MuteOption option = SettingsSingleton.GetInstance().getMuteOption();
        if (option == MuteOption.microphone) {
            muteMicRadio.setSelected(true);
        } else {
            muteMicRadio.setSelected(false);
        }
        if (option == MuteOption.sound) {
            muteSoundRadio.setSelected(true);
        } else {
            muteSoundRadio.setSelected(false);
        }
    }

    // mute button text values
    private void setButtonTexts() {
        muteMicBindButton.setText(KeyEvent.getKeyText(SettingsSingleton.GetInstance().getMuteMicKey()));
        muteSoundBindButton.setText(KeyEvent.getKeyText(SettingsSingleton.GetInstance().getMuteSoundKey()));
        toggleBindButton.setText(KeyEvent.getKeyText(SettingsSingleton.GetInstance().getToggleKey()));
        pack();
    }

    // mute radio
    private void handleMuteRadio(ActionEvent event) {
        String command = event.getActionCommand();
        if (command == "Microphone") {
            SettingsSingleton.GetInstance().setMuteOption(MuteOption.microphone);
        }
        if (command == "Sound") {
            SettingsSingleton.GetInstance().setMuteOption(MuteOption.sound);
        }
        try {
            SettingsSingleton.GetInstance().saveSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setMuteRadioValues();
    }

    // mute mic bind button
    private void handleMuteMicBindButton(ActionEvent event) {
        muteMicBindButton.setText("   ");
        pack();
        Color originalColor = muteMicBindButton.getBackground();
        muteMicBindButton.setBackground(Color.LIGHT_GRAY);
        JFrame mainFrame = this;
        muteMicBindButton.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent arg0) {
                // nothing
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                int keyCode = arg0.getKeyCode();
                if (keyCode != KeyEvent.VK_ESCAPE) {
                    if (keyCode != SettingsSingleton.GetInstance().getMuteSoundKey()
                            && keyCode != SettingsSingleton.GetInstance().getToggleKey()) {
                        SettingsSingleton.GetInstance().setMuteMicKey(keyCode);
                        try {
                            SettingsSingleton.GetInstance().saveSettings();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        JOptionPane.showMessageDialog(mainFrame, "Key is already used!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                }
                muteMicBindButton.setBackground(originalColor);
                setButtonTexts();
                muteMicBindButton.removeKeyListener(this);
                FocusListener[] focusListeners = muteMicBindButton.getFocusListeners();
                if (focusListeners.length == 1)
                    muteMicBindButton.removeFocusListener(focusListeners[0]);
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                // nothing as well
            }

        });
        muteMicBindButton.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
                // nothing
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                muteMicBindButton.setBackground(originalColor);
                setButtonTexts();
                muteMicBindButton.removeFocusListener(this);
                KeyListener[] keyListeners = muteMicBindButton.getKeyListeners();
                if (keyListeners.length == 1)
                    muteMicBindButton.removeKeyListener(keyListeners[0]);
            }
        });
    }

    // mute sound bind button
    private void handleMuteSoundBindButton(ActionEvent event) {
        muteSoundBindButton.setText("   ");
        pack();
        Color originalColor = muteSoundBindButton.getBackground();
        muteSoundBindButton.setBackground(Color.LIGHT_GRAY);
        JFrame mainFrame = this;
        muteSoundBindButton.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent arg0) {
                // nothing
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                int keyCode = arg0.getKeyCode();
                if (keyCode != KeyEvent.VK_ESCAPE) {
                    if (keyCode != SettingsSingleton.GetInstance().getMuteMicKey()
                            && keyCode != SettingsSingleton.GetInstance().getToggleKey()) {
                        SettingsSingleton.GetInstance().setMuteSoundKey(keyCode);
                        try {
                            SettingsSingleton.GetInstance().saveSettings();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        JOptionPane.showMessageDialog(mainFrame, "Key is already used!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                }
                muteSoundBindButton.setBackground(originalColor);
                setButtonTexts();
                muteSoundBindButton.removeKeyListener(this);
                FocusListener[] focusListeners = muteSoundBindButton.getFocusListeners();
                if (focusListeners.length == 1)
                    muteSoundBindButton.removeFocusListener(focusListeners[0]);
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                // nothing as well
            }

        });
        muteSoundBindButton.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
                // nothing
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                muteSoundBindButton.setBackground(originalColor);
                setButtonTexts();
                muteSoundBindButton.removeFocusListener(this);
                KeyListener[] keyListeners = muteSoundBindButton.getKeyListeners();
                if (keyListeners.length == 1)
                    muteSoundBindButton.removeKeyListener(keyListeners[0]);
            }
        });
    }

    // toggle key radio
    private void handleToggleKeyRadio(ActionEvent event) {
        if (toggleBindCheckBox.isSelected()) {
            SettingsSingleton.GetInstance().setToggleButton(true);
            toggleBindButton.setEnabled(true);
        } else {
            SettingsSingleton.GetInstance().setToggleButton(false);
            toggleBindButton.setEnabled(false);
        }
        try {
            SettingsSingleton.GetInstance().saveSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // toggle bind button
    private void handleToggleBindButton(ActionEvent event) {
        toggleBindButton.setText("   ");
        pack();
        Color originalColor = toggleBindButton.getBackground();
        toggleBindButton.setBackground(Color.LIGHT_GRAY);
        JFrame mainFrame = this;
        toggleBindButton.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent arg0) {
                // nothing
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                int keyCode = arg0.getKeyCode();
                if (keyCode != KeyEvent.VK_ESCAPE) {
                    if (keyCode != SettingsSingleton.GetInstance().getMuteMicKey()
                            && keyCode != SettingsSingleton.GetInstance().getMuteSoundKey()) {
                        SettingsSingleton.GetInstance().setToggleKey(keyCode);
                        try {
                            SettingsSingleton.GetInstance().saveSettings();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        JOptionPane.showMessageDialog(mainFrame, "Key is already used!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                }
                toggleBindButton.setBackground(originalColor);
                setButtonTexts();
                toggleBindButton.removeKeyListener(this);
                FocusListener[] focusListeners = toggleBindButton.getFocusListeners();
                if (focusListeners.length == 1)
                    toggleBindButton.removeFocusListener(focusListeners[0]);
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                // nothing as well
            }

        });
        toggleBindButton.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
                // nothing
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                toggleBindButton.setBackground(originalColor);
                setButtonTexts();
                toggleBindButton.removeFocusListener(this);
                KeyListener[] keyListeners = toggleBindButton.getKeyListeners();
                if (keyListeners.length == 1)
                    toggleBindButton.removeKeyListener(keyListeners[0]);
            }
        });
    }

    // panel for search dialog
    private JPanel getSearchDialgoPanel() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(dialogPanel);
        JLabel loadingLabel = new JLabel(" Searching... ", JLabel.CENTER);
        loadingLabel.setSize(50, 50);
        dialogPanel.add(loadingLabel);
        return dialogPanel;
    }

    // ok button
    private void handleOkButton(ActionEvent event) {
        minimizeToTray();
    }

    // exit button
    private void handleExitButton(ActionEvent event) {
        System.out.println("Exiting...");
        System.exit(0);
    }
}