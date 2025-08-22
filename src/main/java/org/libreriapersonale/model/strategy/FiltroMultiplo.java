package org.libreriapersonale.model.strategy;

import org.libreriapersonale.model.libro.Libro;

import java.util.List;

public class FiltroMultiplo implements Filtro {
    private final List<Filtro> filtri;
    private final boolean and; // true = tutti i filtri, false = almeno uno

    public FiltroMultiplo(List<Filtro> filtri, boolean and) {
        this.filtri = filtri;
        this.and = and;
    }

    @Override
    public boolean filtra(Libro libro) {
        if (and) {
            // tutti i filtri
            for (Filtro f : filtri) {
                if (!f.filtra(libro)) { //metodo filtra specif di f (poli)
                    return false;
                }
            }
            return true;
        } else {
            // basta un filtro
            for (Filtro f : filtri) {
                if (f.filtra(libro)) {
                    return true;
                }
            }
            return false;
        }
    }
}
