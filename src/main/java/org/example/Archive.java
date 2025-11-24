package org.example;

import javax.swing.*;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Archive {

    Archive(Frame frame, File currentDirectory) {
        JFileChooser chooser = new JFileChooser(currentDirectory);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File selected = chooser.getSelectedFile();
        String suggestedName = selected.getName();
        if (suggestedName.toLowerCase().endsWith(".zip"))
            suggestedName = suggestedName.substring(0, suggestedName.length() - 4);

        String userInput = JOptionPane.showInputDialog(frame,
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
                        JOptionPane.showMessageDialog(frame,
                                "Created ZIP: " + zipFile.getAbsolutePath(),
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE));

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(frame::errorMessage);
            }
        }).start();
        frame.showFiles(currentDirectory);
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
}
