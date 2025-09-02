package org.libreriapersonale;

import org.libreriapersonale.view.LibreriaGUI;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LibreriaGUI().setVisible(true);
        });
    }
}