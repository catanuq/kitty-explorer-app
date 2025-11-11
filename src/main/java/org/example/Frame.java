package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Frame extends JFrame implements ActionListener {
    JButton button;
    JPanel panel;
    JScrollPane scrollPane;
    File currentDirectory;
    ImageIcon icon;
    JLabel label;

    Frame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 600);
        this.setTitle("Kitty Explorer");
        this.getContentPane().setBackground(new Color(0xffa4c6));
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);

        icon = new ImageIcon("C:\\DOWNLOADS\\helloKitty.png");
        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        icon = new ImageIcon(img);
        label = new JLabel(icon);
        c.fill = GridBagConstraints.NONE;
        c.anchor=GridBagConstraints.NORTH;
        c.gridx=0;
        c.gridy=0;
        this.add(label,c);

        this.setIconImage(icon.getImage());

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0xfffafc));

        scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        c.gridx=1;
        c.gridy=0;
        c.gridheight=2;
        c.fill=GridBagConstraints.BOTH;
        c.weightx=1;
        c.weighty=1;
        this.add(scrollPane,c);

        button = new JButton();
        button.setBackground(new Color(0xffdae7));
        button.setText("Local Disk (C:)");
        button.addActionListener(this);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(120,50));

        c.gridy=0;
        c.gridx=0;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.anchor=GridBagConstraints.CENTER;
        c.weightx=0;
        c.weighty=0;
        this.add(button,c);

        currentDirectory = new File("C:\\");
        this.setVisible(true);
    }

    public void errorMessage() {
        icon = new ImageIcon("C:\\DOWNLOADS\\sadHelloKitty.png");
        Image image = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        icon = new ImageIcon(image);
        label = new JLabel(icon);
        label.setBounds(35, 60, 100, 50);

        JPanel panel = new JPanel();
        panel.add(new JLabel(    "<html><center>Couldn't open file :(<br>Accepted formats: PNG, JPEG, TXT, GIF, MP3, MP4, PDF, DOCX, MDP</center></html>"));

        panel.add(label);
        JOptionPane.showMessageDialog(this,
                panel,
                "Uh-oh!",
                JOptionPane.PLAIN_MESSAGE);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            showFiles(currentDirectory);
        }
    }

    public void showFiles(File dir) {
        panel.removeAll();
        File[] files = dir.listFiles();
        if (files == null) {
            panel.add(new JLabel("Cannot access directory."));
        } else {
            for (File file : files) {
                JButton fileButton = new JButton(file.getName());
                if (file.isDirectory()) {
                    fileButton.setBackground(new Color(0xffdae7));
                } else {
                    fileButton.setBackground(new Color(0xfaebff));
                }
                fileButton.setHorizontalAlignment(SwingConstants.LEFT);
                fileButton.setFocusable(false);
                fileButton.addActionListener(ae -> {
                    if (file.isDirectory()) {
                        currentDirectory = file;
                        showFiles(file);
                    } else {
                        try {
                            String name = file.getName().toLowerCase();

                            if (name.endsWith(".txt") || name.endsWith(".png") || name.endsWith(".jpg") ||
                                    name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".bmp") ||
                                    name.endsWith(".mp3") || name.endsWith(".mp4") || name.endsWith(".wav") ||
                                    name.endsWith(".pdf") || name.endsWith(".docx")|| name.endsWith(".xls") || name.endsWith(".mdp")) {

                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().open(file);
                                } else {
                                    errorMessage();
                                }

                            } else {
                                errorMessage();
                            }

                        } catch (Exception ex) {
                            errorMessage();
                        }
                    }
                });
                panel.add(fileButton);
            }
        }
        if (dir.getParentFile() != null) {
            JButton backButton = new JButton("← Back");
            backButton.setBackground(new Color(255, 230, 230));
            backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            backButton.addActionListener(ae -> {
                currentDirectory = dir.getParentFile();
                showFiles(currentDirectory);
            });
            panel.add(backButton, 0);
        }

        panel.revalidate();
        panel.repaint();

    }
}



