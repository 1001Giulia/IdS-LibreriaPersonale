package org.libreriapersonale.model.memento;

import java.util.ArrayList;
import java.util.List;

import org.libreriapersonale.model.libro.Libro;


public class MementoLibri {
    private final List<Libro> libri;

    public MementoLibri(List<Libro> libri) {
        // copia profonda
        this.libri = new ArrayList<>();
        for (Libro l : libri) {
            this.libri.add(new Libro.Builder()
                    .titolo(l.getTitolo())
                    .autore(l.getAutore())
                    .isbn(l.getIsbn())
                    .genere(l.getGenere())
                    .rating(l.getRating())
                    .statoLettura(l.getStatoLettura())
                    .build());
        }
    }

    public List<Libro> getLibri() {
        // Ritorna una copia per sicurezza
        List<Libro> copia = new ArrayList<>();
        for (Libro l : libri) {
            copia.add(new Libro.Builder()
                    .titolo(l.getTitolo())
                    .autore(l.getAutore())
                    .isbn(l.getIsbn())
                    .genere(l.getGenere())
                    .rating(l.getRating())
                    .statoLettura(l.getStatoLettura())
                    .build());
        }
        return copia;
    }
}
