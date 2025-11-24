package org.example;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Rename {
    Rename(){}

    public void renameSelectedFile(Frame frame, File currentDirectory, File selectedFile) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(frame,
                    "No file or folder selected.",
                    "Rename",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (frame.isProtected(selectedFile)) {

            JOptionPane.showMessageDialog(frame,
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
                frame,
                "Enter new name:",
                baseName
        );

        if (userInput == null) return; // Cancel pressed
        userInput = userInput.trim();

        if (userInput.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
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
            JOptionPane.showMessageDialog(frame,
                    "A file or folder with that name already exists.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        boolean success = selectedFile.renameTo(renamedFile);

        if (!success) {
            JOptionPane.showMessageDialog(frame,
                    "Failed to rename.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        selectedFile = renamedFile;
        frame.showFiles(currentDirectory);
        JOptionPane.showMessageDialog(frame,
                "Renamed successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);


    }
}
