package org.libreriapersonale.model;

import org.libreriapersonale.model.libro.*;
import org.libreriapersonale.model.memento.MementoLibri;
import org.libreriapersonale.model.observer.Observable;
import org.libreriapersonale.model.observer.Observer;
import org.libreriapersonale.model.repository.LibroRepository;

import java.util.*; //TODO da controllare tutto

public class LibreriaManager implements Observable {

    private final List<Libro> libri;
    private final List<Observer> observers;
    private final LibroRepository repository;

    // Caretaker - undo e redo
    private final Deque<MementoLibri> undohistory = new ArrayDeque<>();
    private final Deque<MementoLibri> redohistory = new ArrayDeque<>();

    public LibreriaManager(LibroRepository repository) {
        this.observers = new ArrayList<>();
        this.repository = repository;
        this.libri = caricaLibri();
    }

    private List<Libro> caricaLibri() {
        try {
            return new ArrayList<>(repository.carica());
        } catch (Exception e) {
            System.err.println("Errore caricamento libri.");
            return new ArrayList<>();
        }
    }


    // PATTERN OSBERVER

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }

    // UNDO GLOBALE
    private void salvaStato() {
        undohistory.push(new MementoLibri(libri));
        redohistory.clear();
    }

    public void undo() {
        if (!undohistory.isEmpty()) {
            redohistory.push(new MementoLibri(libri)); //passo i libri correnti facendo copia profonda ecc e poi salva in stack redo
            MementoLibri prev = undohistory.pop();
            libri.clear();
            libri.addAll(prev.getLibri());
            try {
                repository.salva(libri);
            } catch (Exception e) {
                System.err.println("Errore durante undo.");
            }
            notifyObservers();
        }
    }

    public void redo() {
        if (!redohistory.isEmpty()) {
            undohistory.push(new MementoLibri(libri)); //salvo curr state
            MementoLibri next = redohistory.pop();
            libri.clear();
            libri.addAll(next.getLibri());
            try {
                repository.salva(libri);
            } catch (Exception e) {
                System.err.println("Errore durante redo.");
            }
            notifyObservers();
        }
    }


    // CRUD
    public void aggiungiLibro(Libro libro) {
        salvaStato();
        try {
            libri.add(libro);
            repository.salva(libri);
            notifyObservers();
        } catch (Exception e) {
            System.err.println("Errore aggiunta libro.");
        }
    }

    public void eliminaLibro(Libro libro) {
        salvaStato();
        if (libri.remove(libro)) {
            try {
                repository.salva(libri);
                notifyObservers();
            } catch (Exception e) {
                System.err.println("Errore eliminazione libro.");
            }
        }
    }

    public void modificaLibro(Libro nuovo) {
        salvaStato();
        boolean found = false;
        for (int i = 0; i < libri.size(); i++) {
            if (libri.get(i).getIsbn().equals(nuovo.getIsbn())) {
                libri.set(i, nuovo);
                found = true;
                break;
            }
        }
        if (found) {
            try {
                repository.salva(libri);
                notifyObservers();
            } catch (Exception e) {
                System.err.println("Errore modifica libro.");
            }
        }
    }

    // fare un metodo di salvataggio privato per non scrivere tutte le volte try catch (?)

    /* in caso si può fare una cosa del genere al posto delle string ogni volta ma vabbe
    private void msgErrore(String op, Exception e) {
        String mes = "Errore durante " + op + ": " + e.getMessage();
        System.err.println(mes);
    }
    */


    // RICERCA LIBRI
    /* sostituiti con strategy
    public List<Libro> cercaPerTitolo(String titolo) {
        List<Libro> ret = new ArrayList<>();
        for (Libro libro : libri) {
            if (libro.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                ret.add(libro);
            }
        }
        return ret;
    }

    public List<Libro> cercaPerAutore(String autore) {
        List<Libro> ret = new ArrayList<>();
        for (Libro libro : libri) {
            if (libro.getAutore().toLowerCase().contains(autore.toLowerCase())) {
                ret.add(libro);
            }
        }
        return ret;
    }
    */


    public Libro cercaPerIsbn(String isbn) {
        for (Libro libro : libri) {
            if (libro.getIsbn().equals(isbn)) {
                return libro;
            }
        }
        return null;
    }


    // GETTERS E SETTERS
    public List<Libro> getLibri() {
        return new ArrayList<>(libri);
    }

    public void setLibri(List<Libro> nuoviLibri) {
        salvaStato();
        libri.clear();
        //libri.addAll(nuoviLibri);
        for (Libro l : nuoviLibri) {
            // copia x sicurezza
            Libro copia = new Libro.Builder()
                    .titolo(l.getTitolo())
                    .autore(l.getAutore())
                    .isbn(l.getIsbn())
                    .genere(l.getGenere())
                    .rating(l.getRating())
                    .statoLettura(l.getStatoLettura())
                    .build();
            libri.add(copia);
            try {
                repository.salva(libri);
            } catch (Exception e) {
                System.err.println("Errore nel salvataggio.");
            }
            notifyObservers();
        }

    }
}



