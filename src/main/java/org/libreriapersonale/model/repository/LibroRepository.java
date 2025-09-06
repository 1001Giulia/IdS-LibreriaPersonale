package org.libreriapersonale.model.repository;

import org.libreriapersonale.model.libro.*;

public interface LibroRepository { // Interfaccia in caso dovessi aggiungere più implementazioni (x es xml)
    void salva(java.util.List<Libro> libri) throws java.io.IOException;
    java.util.List<Libro> carica() throws java.io.IOException;
}

