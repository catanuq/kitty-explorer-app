package org.example;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unarchive {

    Unarchive(Frame frame,File currentDirectory) {
        JFileChooser chooser = new JFileChooser(currentDirectory);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        final File zipFile = chooser.getSelectedFile();
        if (!zipFile.getName().toLowerCase().endsWith(".zip")) {
            JOptionPane.showMessageDialog(frame,
                    "Please select a .zip file",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        String defaultFolderName = zipFile.getName().substring(0, zipFile.getName().length() - 4);
        String userFolderName = JOptionPane.showInputDialog(frame,
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
                        JOptionPane.showMessageDialog(frame,
                                "Extracted to: " + destDirCopy.getAbsolutePath(),
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE));

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(frame::errorMessage);
            }
        }).start();
        frame.showFiles(currentDirectory);
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
