package org.libreriapersonale.model.strategy;

import org.libreriapersonale.model.libro.*;

public class FiltroGenere implements Filtro{

    private final Genere genere;

    public FiltroGenere(Genere genere) {
        this.genere = genere;
    }

    @Override
    public boolean filtra(Libro libro) {
        return genere != null && genere.equals(libro.getGenere());
    }
}
