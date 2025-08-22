package org.libreriapersonale.model.strategy;

import org.libreriapersonale.model.libro.Libro;

public interface Filtro {
    boolean filtra(Libro libro);
}
