package org.libreriapersonale.model.strategy;

import org.libreriapersonale.model.libro.*;

public class FiltroStatoLettura implements Filtro{
    private final StatoLettura stato;

    public FiltroStatoLettura(StatoLettura stato) {
        this.stato = stato;
    }

    @Override
    public boolean filtra(Libro libro) {
        return stato != null && stato.equals(libro.getStatoLettura());
    }
}
