package org.libreriapersonale.controller.command;

import org.libreriapersonale.model.LibreriaManager;
import org.libreriapersonale.model.libro.Libro;

public class EditLibroCmd implements Command {
    private final LibreriaManager manager; //anche qua, final entrambi?
    private final Libro nuovoLibro;
    //private delLibroMemento backup;

    public EditLibroCmd(LibreriaManager manager, Libro nuovoLibro) {
        this.manager = manager;
        this.nuovoLibro = nuovoLibro;
    }

    @Override
    public void esegui() {
        manager.modificaLibro(nuovoLibro);
    }

    /*
    @Override
    public void esegui() {
        Libro vecchio = manager.cercaPerIsbn(nuovoLibro.getIsbn());
        if (vecchio != null) {
            backup = vecchio.save(); // salva stato precedente
            manager.modificaLibro(nuovoLibro);
        }
    }

    @Override
    public void undo() {
        if (backup != null) {
            Libro daRipristinare = manager.cercaPerIsbn(backup.getIsbn());
            if (daRipristinare != null) {
                daRipristinare.restore(backup);
                manager.notificaObservers(); // aggiorna GUI/CLI
            }
        }
    }*/
}
