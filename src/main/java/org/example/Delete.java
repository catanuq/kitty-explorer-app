package org.example;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Delete {
    Delete(Frame frame, File selectedFile, File currentDirectory) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(frame,
                    "No file/folder selected.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (frame.isProtected(selectedFile)) {
            JOptionPane.showMessageDialog(frame,
                    "You're not allowed to delete this file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete: " + selectedFile.getName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        deleteRecursively(selectedFile);
        selectedFile = null;
        frame.showFiles(currentDirectory);
        JOptionPane.showMessageDialog(frame,
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
}
