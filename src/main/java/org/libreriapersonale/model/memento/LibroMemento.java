package org.libreriapersonale.model.memento;

import org.libreriapersonale.model.libro.*;

public class LibroMemento {
    private String titolo, autore, isbn;
    private Genere genere; //genere
    private int rating;
    private StatoLettura statoLettura;

    public LibroMemento(String titolo, String autore, Genere genere, int rating, StatoLettura stato) {
        this.titolo = titolo;
        this.autore = autore;
        this.genere = genere;
        this.rating = rating;
        this.statoLettura = stato;
    }

    public String getTitolo() { return titolo; }
    public String getAutore() { return autore; }
    public String getIsbn() { return isbn; }
    public Genere getGenere() { return genere; }
    public int getRating() { return rating; }
    public StatoLettura getStatoLettura() { return statoLettura; }


    //caretaker in modificacommand e commandhistory (?)
}
