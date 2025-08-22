package org.libreriapersonale;

import org.libreriapersonale.model.libro.Genere;
import org.libreriapersonale.model.libro.Libro;
import org.libreriapersonale.model.libro.StatoLettura;
import org.libreriapersonale.model.strategy.*;

import java.util.List;

public class Main2 {
    public static void main(String[] args) {
        // Creo qualche libro
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

        Libro libro3 = new Libro.Builder()
                .titolo("It")
                .autore("Stephen King")
                .isbn("3333")
                .genere(Genere.HORROR)
                .rating(5)
                .statoLettura(StatoLettura.PLANTOREAD)
                .build();

        List<Libro> libri = List.of(libro1, libro2, libro3);


        // === TEST FILTRI ===
        Filtro f1 = new FiltroAutore("eco");
        Filtro f2 = new FiltroGenere(Genere.SAGGIO);
        Filtro f3 = new FiltroStatoLettura(StatoLettura.PLANTOREAD);
        Filtro f4 = new FiltroValutazione(5);

        System.out.println("Filtro Autore = 'eco'");
        libri.stream().filter(f1::filtra).forEach(l ->
                System.out.println(" -> " + l.getTitolo() + " di " + l.getAutore()));

        System.out.println("\nFiltro Genere = SAGGIO");
        libri.stream().filter(f2::filtra).forEach(l ->
                System.out.println(" -> " + l.getTitolo()));

        System.out.println("\nFiltro Stato Lettura = PLANTOREAD");
        libri.stream().filter(f3::filtra).forEach(l ->
                System.out.println(" -> " + l.getTitolo()));

        System.out.println("\nFiltro Rating = 5");
        libri.stream().filter(f4::filtra).forEach(l ->
                System.out.println(" -> " + l.getTitolo()));



        // Filtro combinato: autore contiene "eco" E rating=5
        Filtro multiploAND = new FiltroMultiplo(
                List.of(new FiltroAutore("eco"), new FiltroValutazione(5)), true);

        System.out.println("\nFiltro Multiplo (autore=eco AND rating=5)");
        libri.stream().filter(multiploAND::filtra).forEach(l ->
                System.out.println(" -> " + l.getTitolo())
        );

        // Filtro combinato: genere=SAGGIO OR rating=5
        Filtro multiploOR = new FiltroMultiplo(
                List.of(new FiltroGenere(Genere.SAGGIO), new FiltroValutazione(5)),
                false // OR
        );

        System.out.println("\nFiltro Multiplo (genere=SAGGIO OR rating=5)");
        libri.stream().filter(multiploOR::filtra).forEach(l ->
                System.out.println(" -> " + l.getTitolo())
        );
    }
}
