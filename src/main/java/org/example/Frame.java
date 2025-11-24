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
        renameBtn.addActionListener(ae -> renameSelectedFile());

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
        if (e.getSource() == zipBtn) createArchive();
        else if (e.getSource() == unzipBtn) extractArchive();
        else if (e.getSource() == removeBtn) removeSelectedFile();
    }

    private void renameSelectedFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this,
                    "No file or folder selected.",
                    "Rename",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (isProtected(selectedFile)) {

            JOptionPane.showMessageDialog(this,
                    "You're not allowed to rename this file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                    );
            return;
        }

        String originalName = selectedFile.getName();

        // Extract extension if it exists (e.g. ".txt")
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalName.length() - 1) {
            extension = originalName.substring(dotIndex);  // keep ".txt"
        }

        // Ask user for the new base name (without extension)
        String baseName = (extension.isEmpty())
                ? originalName // folder or no extension
                : originalName.substring(0, dotIndex);

        String userInput = JOptionPane.showInputDialog(
                this,
                "Enter new name:",
                baseName
        );

        if (userInput == null) return; // Cancel pressed
        userInput = userInput.trim();

        if (userInput.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Name cannot be empty.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                    );
            return;
        }

        // Build final name: userInput + original extension
        String finalName = userInput + extension;

        File renamedFile = new File(selectedFile.getParent(), finalName);

        if (renamedFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "A file or folder with that name already exists.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                    );
            return;
        }

        boolean success = selectedFile.renameTo(renamedFile);

        if (!success) {
            JOptionPane.showMessageDialog(this,
                    "Failed to rename.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        selectedFile = renamedFile;
        showFiles(currentDirectory);
        JOptionPane.showMessageDialog(this,
                "Renamed successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);


    }

    private void removeSelectedFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this,
                    "No file/folder selected.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (isProtected(selectedFile)) {
            JOptionPane.showMessageDialog(this,
                    "You're not allowed to delete this file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete: " + selectedFile.getName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        deleteRecursively(selectedFile);
        selectedFile = null;
        showFiles(currentDirectory);
        JOptionPane.showMessageDialog(this,
                "File deleted successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursively(child);
        }
        return file.delete();
    }

    private boolean isProtected(File file) {
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


    public void createArchive() {
        JFileChooser chooser = new JFileChooser(currentDirectory);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selected = chooser.getSelectedFile();
        String suggestedName = selected.getName();
        if (suggestedName.toLowerCase().endsWith(".zip"))
            suggestedName = suggestedName.substring(0, suggestedName.length() - 4);

        String userInput = JOptionPane.showInputDialog(this,
                "Enter ZIP file name:",
                suggestedName);
        if (userInput == null || userInput.trim().isEmpty()) return;

        File zipFile = getUniqueZipFile(new File(currentDirectory, userInput.trim() + ".zip"));

        new Thread(() -> {
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {

                // baseDir = the parent of the selected item. This makes archive entries
                // look like "selectedName/..." for directories or "file.txt" for a single file.
                File baseDir = selected.getParentFile();
                if (baseDir == null) baseDir = selected; // defensive: e.g. root selection

                zipFileRecursiveRelative(selected, baseDir, zos, zipFile);

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(Frame.this,
                                "Created ZIP: " + zipFile.getAbsolutePath(),
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE));

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(this::errorMessage);
            }
        }).start();
        showFiles(currentDirectory);
    }


    private void zipFileRecursiveRelative(File fileToZip, File baseDir, ZipOutputStream zos, File zipFile) throws IOException {

        // Skip hidden and skip the archive file itself (compare canonical paths)
        if (fileToZip.isHidden()) return;
        try {
            if (fileToZip.getCanonicalPath().equals(zipFile.getCanonicalPath())) return;
        } catch (IOException ignored) { /* fallback below will still work */ }

        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File child : children) {
                    zipFileRecursiveRelative(child, baseDir, zos, zipFile);
                    System.out.println("Zipped "+child.getName()+" to "+baseDir.getName());
                }
            }
            System.out.println("Zipped "+fileToZip.getName()+" to "+baseDir.getName());
            return;
        }

        // Compute entry name relative to baseDir
        String entryName;
        try {
            entryName = baseDir.toURI().relativize(fileToZip.toURI()).getPath();
        } catch (Exception ex) {
            // fallback to simple name if relativize fails
            entryName = fileToZip.getName();
        }
        // If relativize returned empty (rare), use filename
        if (entryName == null || entryName.isEmpty()) {
            entryName = fileToZip.getName();
        }
        // Ensure ZIP uses forward slashes
        entryName = entryName.replace(File.separatorChar, '/');

        // Add file entry and write bytes
        ZipEntry entry = new ZipEntry(entryName);
        entry.setTime(fileToZip.lastModified());
        zos.putNextEntry(entry);

        try (FileInputStream fis = new FileInputStream(fileToZip);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
        }

        zos.closeEntry();
    }

    private File getUniqueZipFile(File baseFile) {
        String parent = baseFile.getParent();
        String name = baseFile.getName();
        if (name.toLowerCase().endsWith(".zip")) name = name.substring(0, name.length() - 4);

        File zipFile = new File(parent, name + ".zip");
        int count = 1;
        while (zipFile.exists()) {
            zipFile = new File(parent, name + " (" + count + ").zip");
            count++;
        }
        return zipFile;
    }

    public void extractArchive() {
        JFileChooser chooser = new JFileChooser(currentDirectory);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        final File zipFile = chooser.getSelectedFile();
        if (!zipFile.getName().toLowerCase().endsWith(".zip")) {
            JOptionPane.showMessageDialog(this,
                    "Please select a .zip file",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ask user for folder to extract into (always relative to currentDirectory)
        String defaultFolderName = zipFile.getName().substring(0, zipFile.getName().length() - 4);
        String userFolderName = JOptionPane.showInputDialog(this,
                "Enter folder name for extraction:",
                        defaultFolderName);
        if (userFolderName == null || userFolderName.trim().isEmpty()) return;

        File destDir = getUniqueFolder(new File(currentDirectory, userFolderName.trim()));
        final File destDirCopy = destDir;

        new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(zipFile);
                 ZipInputStream zis = new ZipInputStream(fis)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    File newFile = new File(destDirCopy, entry.getName());
                    if (entry.isDirectory()) newFile.mkdirs();
                    else {
                        new File(newFile.getParent()).mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                        }
                    }
                    zis.closeEntry();
                }

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(Frame.this,
                                "Extracted to: " + destDirCopy.getAbsolutePath(),
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE));

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(this::errorMessage);
            }
        }).start();
        showFiles(currentDirectory);
    }

    private File getUniqueFolder(File baseFolder) {
        File folder = baseFolder;
        int count = 1;
        while (folder.exists()) {
            folder = new File(baseFolder.getParent(), baseFolder.getName() + " (" + count + ")");
            count++;
        }
        folder.mkdirs();
        return folder;
    }
}


