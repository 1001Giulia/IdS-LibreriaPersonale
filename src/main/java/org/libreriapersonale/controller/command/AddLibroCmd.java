package org.libreriapersonale.controller.command;

import org.libreriapersonale.model.LibreriaManager;
import org.libreriapersonale.model.libro.Libro;

public class AddLibroCmd implements Command {

    private final LibreriaManager manager;
    private final Libro libro;

    public AddLibroCmd(LibreriaManager manager, Libro libro) {
        this.manager = manager;
        this.libro = libro;
    }

    @Override
    public void esegui() {
        manager.aggiungiLibro(libro); //il receiver sa come fare
    }

}
