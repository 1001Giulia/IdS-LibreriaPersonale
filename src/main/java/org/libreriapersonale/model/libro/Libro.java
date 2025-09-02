package org.libreriapersonale.model.libro;

public class Libro {
    private String titolo, autore, isbn;
    private Genere genere;
    private double rating;
    private StatoLettura statoLettura;

    private Libro(Builder builder) {
        this.titolo = builder.titolo;
        this.autore = builder.autore;
        this.isbn = builder.isbn;
        this.genere = builder.genere;
        this.rating = builder.rating;
        this.statoLettura = builder.statoLettura;
    }

    // GETTERS e SETTERS
    public String getTitolo() { return titolo; }
    public String getAutore() { return autore; }
    public String getIsbn() { return isbn; }
    public Genere getGenere() { return genere; }
    public double getRating() { return rating; }
    public StatoLettura getStatoLettura() { return statoLettura; }

    public void setTitolo(String titolo) { this.titolo = titolo; }
    public void setAutore(String autore) { this.autore = autore; }
    public void setGenere(Genere genere) { this.genere = genere; }
    public void setRating(double rating) { this.rating = rating; }
    public void setStatoLettura(StatoLettura statoLettura) { this.statoLettura = statoLettura; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libro)) return false;
        Libro libro = (Libro) o;
        return isbn.equals(libro.isbn);
    }

    @Override
    public int hashCode() {
        return isbn.hashCode();
    }

    // BUILDER PATTERN
    public static class Builder {
        private String titolo, autore, isbn; //req
        private Genere genere = Genere.ALTRO;
        private StatoLettura statoLettura = StatoLettura.READING;
        private double rating = 0;

        public Builder titolo(String titolo) { this.titolo = titolo; return this; }
        public Builder autore(String autore) { this.autore = autore; return this; }
        public Builder isbn(String isbn) { this.isbn = isbn; return this; }
        public Builder genere(Genere genere) { this.genere = genere; return this; }
        public Builder statoLettura(StatoLettura sl) { this.statoLettura = sl; return this; }
        public Builder rating(double rating) { this.rating = rating; return this; }

        public Libro build() {
            if (titolo == null || titolo.isEmpty()) {
                throw new IllegalArgumentException("Il titolo è obbligatorio.");
            }
            if (autore == null || autore.isEmpty()) {
                throw new IllegalArgumentException("L'autore è obbligatorio.");
            }
            if (isbn == null || isbn.isEmpty()) {
                throw new IllegalArgumentException("L'ISBN è obbligatorio.");
            }
            return new Libro(this);
        }
    }
}


        /* faccio controllo in build
        public Builder(String titolo, String autore, String isbn) {
            this.titolo = titolo;
            this.autore = autore;
            this.isbn = isbn;
        }
        */

