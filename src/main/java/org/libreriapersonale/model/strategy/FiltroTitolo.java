package org.libreriapersonale.model.strategy;

import org.libreriapersonale.model.libro.Libro;

public class FiltroTitolo implements Filtro {
    private final String titolo; // quello che sto cercando tra i libri

    public FiltroTitolo(String titolo) {
        this.titolo = titolo.toLowerCase();
    }

    @Override
    public boolean filtra(Libro libro) {
        return titolo != null && libro.getTitolo() != null &&
               libro.getTitolo().toLowerCase().contains(titolo);
    }
}
