package org.libreriapersonale.model.strategy;

import org.libreriapersonale.model.libro.Libro;

public class FiltroValutazione implements Filtro{
    private int rating;

    public FiltroValutazione(int rating) {
        this.rating = rating;
    }

    @Override
    public boolean filtra(Libro libro) {
        return rating == libro.getRating();
    }

}
