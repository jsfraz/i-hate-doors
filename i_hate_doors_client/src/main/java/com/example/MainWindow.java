package com.example;

import java.awt.AWTException;
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
import javax.swing.ImageIcon;
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
    private JRadioButton muteSound;
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
        tableConstraints2.gridx = 0;
        tableConstraints2.gridy = 0;
        panel2.add(muteMicRadio, tableConstraints2);
        muteMicBindButton = new JButton("###");
        tableConstraints2.gridx = 1;
        tableConstraints2.gridy = 0;
        panel2.add(muteMicBindButton, tableConstraints2);
        muteSound = new JRadioButton("Sound");
        tableConstraints2.gridx = 0;
        tableConstraints2.gridy = 1;
        panel2.add(muteSound, tableConstraints2);
        muteSoundBindButton = new JButton("###");
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
        panel4.add(toggleBindCheckBox);
        toggleBindButton = new JButton("###");
        panel4.add(toggleBindButton);

        // TODO align to center
        // panel4 (ok and exit buttons)
        tableConstraints1.gridx = 0;
        tableConstraints1.gridy = 3;
        // initializing and adding components
        panel5 = new JPanel(new FlowLayout());
        add(panel5, tableConstraints1);
        okButton = new JButton("OK");
        ;
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

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
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
                System.out.println("Saving settings file...");
                SettingsSingleton.GetInstance().saveSettings();
                System.out.println("Success.");

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
        
        setVisible(false);
        JDialog searchDialog = new JDialog(this, "Searching");
        searchDialog.setResizable(false);
        searchDialog.setVisible(true);
        searchDialog.add(getSearchDialgoPanel());
        searchDialog.setModal(true);
        searchDialog.pack();
        searchDialog.setLocationRelativeTo(this);
        searchDialog.setVisible(true);
        
        MainWindow main = this;
        Runnable r = new Runnable() {
            public void run() {
                new DiscoverHandler(main, searchDialog, ipField, findButton).run();
            }
        };
        Thread t = new Thread(r);

        searchDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                //FIXME The method stop() from the type Thread is deprecated
                System.out.println("Stopped listening for UDP discovery packet.");      // not really :(
                findButton.setEnabled(true);
                setVisible(true);
            }
        });

        t.start();
    }

    // test button
    private void handleTestButton(ActionEvent event) {
        testButton.setEnabled(false);
        boolean success = new MqttHandler(SettingsSingleton.GetInstance().getIp(), "sensor/commands").testConnection();
        String title;
        String message;
        int icon;
        if (success) {
            title = "Info";
            message = "Success!";
            icon = JOptionPane.INFORMATION_MESSAGE;
        } else {
            title = "Error";
            message = "Unable to connect to host.";
            icon = JOptionPane.ERROR_MESSAGE;
        }
        JOptionPane.showMessageDialog(this, message, title, icon);
        testButton.setEnabled(true);
    }

    // panel for search dialog
    private JPanel getSearchDialgoPanel() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(dialogPanel);
        // https://stackoverflow.com/questions/7634402/creating-a-nice-loading-animation
        // https://stackoverflow.com/questions/5895829/resizing-image-in-java
        // https://icons8.com/icon/43761/hourglass
        // FIXME no icon after compiling and running on Windows (shitty system as always, definetly not my own fault)
        ImageIcon loadingIcon = new ImageIcon(
                new ImageIcon("src/main/resources/hourglass.png").getImage().getScaledInstance(32,
                        32, Image.SCALE_DEFAULT));
        JLabel loadingLabel = new JLabel(" Searching... ", loadingIcon, JLabel.CENTER);
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