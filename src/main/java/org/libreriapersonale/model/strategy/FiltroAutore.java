package org.libreriapersonale.model.strategy;

import org.libreriapersonale.model.libro.Libro;

public class FiltroAutore implements Filtro {
    private final String autore; // quello che sto cercando tra i libri

    public FiltroAutore(String autore) {
        this.autore = autore.toLowerCase();
    }

    @Override
    public boolean filtra(Libro libro) {
        return autore != null && libro.getAutore() != null &&
               libro.getAutore().toLowerCase().contains(autore);
    }
}
