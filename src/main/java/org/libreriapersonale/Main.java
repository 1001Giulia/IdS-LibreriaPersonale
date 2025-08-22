package org.libreriapersonale;

import org.libreriapersonale.model.LibreriaManager;
import org.libreriapersonale.controller.command.*;
import org.libreriapersonale.model.libro.*;
import org.libreriapersonale.model.repository.*;
import org.libreriapersonale.model.strategy.*;


import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Creo il repository (JSON)
            LibroRepository repo = new JsonLibroRepository("libreria.json");

            // 2. Creo il manager
            LibreriaManager manager = new LibreriaManager(repo);

            // 3. Creo dei libri di test
            Libro libro1 = new Libro.Builder()
                    .titolo("Il Nome della Rosa")
                    .autore("Umberto Eco")
                    .isbn("1111")
                    .genere(Genere.ROMANZO)
                    .rating(5)
                    .statoLettura(StatoLettura.COMPLETED)
                    .build();

            Libro libro2 = new Libro.Builder()
                    .titolo("Clean Code")
                    .autore("Robert C. Martin")
                    .isbn("2222")
                    .genere(Genere.SAGGIO)
                    .rating(4)
                    .statoLettura(StatoLettura.READING)
                    .build();

            // 4. Eseguo i comandi
            Command add1 = new AddLibroCmd(manager, libro1);
            Command add2 = new AddLibroCmd(manager, libro2);

            add1.esegui();
            add2.esegui();

            // 5. Mostro i libri
            System.out.println("Libri dopo aggiunta:");
            manager.getLibri().forEach(l ->
                    System.out.println(l.getTitolo() + " - " + l.getAutore()));

            // 6. Modifico un libro
            Libro libro2mod = new Libro.Builder()
                    .titolo("Clean Code (2nd Edition)")
                    .autore("Robert C. Martin")
                    .isbn("2222") // stesso ISBN -> riconosciuto come stesso libro
                    .genere(Genere.SAGGIO)
                    .rating(5)
                    .statoLettura(StatoLettura.PLANTOREAD)
                    .build();

            Command edit2 = new EditLibroCmd(manager, libro2mod);
            edit2.esegui();

            System.out.println("\nLibri dopo modifica:");
            manager.getLibri().forEach(l ->
                    System.out.println(l.getTitolo() + " - " + l.getAutore()));

            // 7. Elimino un libro
            Command del1 = new DelLibroCmd(manager, libro1);
            del1.esegui();

            System.out.println("\nLibri dopo eliminazione:");
            manager.getLibri().forEach(l ->
                    System.out.println(l.getTitolo() + " - " + l.getAutore()));

            // 8. Undo globale
            manager.undo();
            System.out.println("\nLibri dopo UNDO:");
            manager.getLibri().forEach(l ->
                    System.out.println(l.getTitolo() + " - " + l.getAutore()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



