package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Frame extends JFrame implements ActionListener {
    JButton zipBtn, unzipBtn, removeBtn, backBtn, renameBtn;
    JPanel panel;
    JScrollPane scrollPane;
    JPanel topActionPanel;
    ImageIcon icon;
    JLabel label;

    File currentDirectory;
    File selectedFile; // currently selected file/folder

    private final String[] allowedExtensions = {
            ".txt", ".png", ".jpg", ".jpeg", ".gif", ".bmp",
            ".mp3", ".mp4", ".wav", ".pdf", ".docx", ".xls",
            ".mdp", ".zip", ".rar"};

    Frame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setTitle("Kitty Explorer");
        this.getContentPane().setBackground(new Color(0xffa4c6));
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        // --- Load logo ---
        icon = new ImageIcon(getClass().getResource("/images/helloKitty.png"));
        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        icon = new ImageIcon(img);
        label = new JLabel(icon);

        // --- Left panel for logo + disk buttons ---
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(0xffa4c6));
        GridBagConstraints lc = new GridBagConstraints();
        lc.insets = new Insets(5, 5, 5, 5);
        lc.gridx = 0;
        lc.fill = GridBagConstraints.HORIZONTAL;
        lc.anchor = GridBagConstraints.NORTH;
        lc.weightx = 1;
        lc.weighty = 0;

        lc.gridy = 0;
        leftPanel.add(label, lc);

        File[] roots = File.listRoots();
        for (File root : roots) {
            JButton rootBtn = new JButton(root.getAbsolutePath());
            rootBtn.setBackground(new Color(0xffdae7));
            rootBtn.setPreferredSize(new Dimension(120, 50));
            rootBtn.setFocusable(false);
            rootBtn.addActionListener(e -> {
                currentDirectory = root;
                selectedFile = null;
                showFiles(currentDirectory);
            });
            lc.gridy++;
            leftPanel.add(rootBtn, lc);
        }

        // --- Create action buttons ---
        zipBtn = new JButton("Zip");
        unzipBtn = new JButton("Unzip");
        removeBtn = new JButton("Delete");

        zipBtn.setBackground(new Color(0xFC81B6));
        unzipBtn.setBackground(new Color(0xFC81B6));
        removeBtn.setBackground(new Color(0xFC81B6));

        zipBtn.setFocusable(false);
        unzipBtn.setFocusable(false);
        removeBtn.setFocusable(false);

        zipBtn.addActionListener(this);
        unzipBtn.addActionListener(this);
        removeBtn.addActionListener(this);

        // --- Right panel setup ---
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0xfffafc));
        scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // --- Top toolbar ---
        topActionPanel = new JPanel();
        topActionPanel.setLayout(new BoxLayout(topActionPanel, BoxLayout.X_AXIS));
        topActionPanel.setBackground(new Color(0xfffafc));

        // Back button
        backBtn = new JButton("← Back");
        backBtn.setFocusable(false);
        backBtn.setBackground(new Color(0xFC81B6));
        backBtn.addActionListener(ae -> {
            if (currentDirectory.getParentFile() != null) {
                currentDirectory = currentDirectory.getParentFile();
                selectedFile = null;
                showFiles(currentDirectory);
            }
        });
        topActionPanel.add(backBtn);
        topActionPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        // Rename button
        renameBtn = new JButton("Rename");
        renameBtn.setFocusable(false);
        renameBtn.setBackground(new Color(0xFC81B6));
        topActionPanel.add(renameBtn);
        topActionPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        Rename rename = new Rename();
        renameBtn.addActionListener(ae -> rename.renameSelectedFile(this, currentDirectory, selectedFile));

        // Add zip/unzip/delete buttons to toolbar
        topActionPanel.add(zipBtn);
        topActionPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        topActionPanel.add(unzipBtn);
        topActionPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        topActionPanel.add(removeBtn);
        topActionPanel.add(Box.createHorizontalGlue());

        // --- Right panel containing toolbar and file list ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(topActionPanel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Add panels to main frame ---
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 1;
        this.add(leftPanel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        this.add(rightPanel, c);

        this.setIconImage(icon.getImage());

        if (roots.length > 0) currentDirectory = roots[0];

        this.setVisible(true);
        showFiles(currentDirectory);
    }

    public void showFiles(File dir) {
        panel.removeAll();
        File[] files = dir.listFiles();
        selectedFile = null;

        if (files == null) {
            panel.add(new JLabel("Cannot access directory."));
        } else {
            for (File file : files) {
                JButton fileButton = new JButton(file.getName());
                fileButton.setHorizontalAlignment(SwingConstants.LEFT);
                fileButton.setFocusable(false);
                fileButton.setBackground(new Color(0xfffafc));

                fileButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Single-click: select
                        if (e.getClickCount() == 1) {
                            selectedFile = file;
                            highlightSelectedButton(fileButton);
                        }

                        // Double-click: open or navigate
                        if (e.getClickCount() == 2) {
                            if (file.isDirectory()) {
                                currentDirectory = file;
                                selectedFile = null;
                                showFiles(file);
                            } else {
                                try {
                                    Desktop.getDesktop().open(file);
                                } catch (Exception ex) {
                                    errorMessage();
                                }
                            }
                        }
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        fileButton.setBackground(new Color(0xffc0cb));
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if(!file.equals(selectedFile)) {
                            fileButton.setBackground(new Color(0xfffafc));
                        }

                    }
                });
                panel.add(fileButton);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    public void errorMessage() {

        JOptionPane.showMessageDialog(this,
                "Something went wrong :(",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == zipBtn) {
            Archive archive = new Archive(this,currentDirectory);
        }
        else if (e.getSource() == unzipBtn) {
            Unarchive unarchive = new Unarchive(this, currentDirectory);
        }
        else if (e.getSource() == removeBtn) {
            Delete delete = new Delete(this, selectedFile, currentDirectory);
        }
    }

    protected boolean isProtected(File file) {
        try {
            if (file.isHidden()) return true;
            String canonicalPath = file.getCanonicalPath();
            if (canonicalPath.startsWith("Local Disk (C:)") ||
                    canonicalPath.startsWith("C:\\Windows") ||
                    canonicalPath.startsWith("C:\\Program Files") ||
                    canonicalPath.startsWith("C:\\Program Files (x86)")) return true;

            if (file.isDirectory()) return false;

            String name = file.getName().toLowerCase();
            for (String ext : allowedExtensions) {
                if (name.endsWith(ext)) return false;
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }

    }

    private void highlightSelectedButton(JButton clickedButton) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;  // classic cast for Java 8
                if (button == clickedButton) {
                    button.setBackground(new Color(0xffc0cb)); // selected
                } else {
                    // Reset background depending on whether it's a folder or file
                    button.setBackground(new Color(0xfffafc));
                }
            }
        }
    }
}


