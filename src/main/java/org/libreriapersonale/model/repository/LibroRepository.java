package org.libreriapersonale.model.repository;

import org.libreriapersonale.model.libro.*;

public interface LibroRepository {
    void salva(java.util.List<Libro> libri) throws java.io.IOException;
    java.util.List<Libro> carica() throws java.io.IOException;
}

