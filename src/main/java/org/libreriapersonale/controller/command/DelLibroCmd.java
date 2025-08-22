package org.libreriapersonale.controller.command;

import org.libreriapersonale.model.LibreriaManager;
import org.libreriapersonale.model.libro.Libro;

public class DelLibroCmd implements Command{
    private final LibreriaManager manager; //final entrambi?
    private final Libro libro;

    public DelLibroCmd(LibreriaManager manager, Libro libro) {
        this.manager = manager;
        this.libro = libro;
    }

    @Override
    public void esegui() {
        manager.eliminaLibro(libro);
    }

    /*
    @Override
    public void undo() {
        manager.aggiungiLibro(libro);
    }*/
}
