package org.libreriapersonale.controller.command;

import org.libreriapersonale.model.LibreriaManager;
import org.libreriapersonale.model.libro.Libro;

public class EditLibroCmd implements Command {
    private final LibreriaManager manager;
    private final Libro nuovoLibro;

    public EditLibroCmd(LibreriaManager manager, Libro nuovoLibro) {
        this.manager = manager;
        this.nuovoLibro = nuovoLibro;
    }

    @Override
    public void esegui() {
        manager.modificaLibro(nuovoLibro);
    }

}
