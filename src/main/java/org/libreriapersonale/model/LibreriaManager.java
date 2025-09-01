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

    /* undo x libro ma cambiato per undo globale. Ogni libro non più un originator che sa reastaurare il proprio memento
    // ================================
    // METODI CON COMANDI
    // ================================
    public void eseguiComando(Command c) {
        history.esegui(c);
        notifyObservers();
    }

    public void undo() {
        history.undo();
        notifyObservers();
    }

    public void redo() {
        history.redo();
        notifyObservers();
    }
*/

/*
    // ================================
    // OPERAZIONI PRINCIPALI
    // ================================

    public void aggiungiLibro(Libro libro) {
        try {
            repository.salva(Arrays.asList(libro));
            libri.add(libro);
            notifyObservers();
        } catch (Exception e) {
            System.err.println("Errore aggiunta libro al database.");
        }
    }

    public void eliminaLibro(Libro libro) {
        if (libri.remove(libro)) {
            try {
                repository.salva(libri);
                notifyObservers();
            } catch (Exception e) {
                System.err.println("Errore eliminazione libro dal database");
            }
        }
    }

    public void modificaLibro(Libro libro) {
        try {
            repository.salva(libri); // Salva tutto
            for (int i = 0; i < libri.size(); i++) {
                if (libri.get(i).getIsbn().equals(libro.getIsbn())) {
                    libri.set(i, libro);
                    notifyObservers();
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore modifica libro.");
        }
    }
*/

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
        for (int i = 0; i < libri.size(); i++) {
            if (libri.get(i).getIsbn().equals(nuovo.getIsbn())) {
                libri.set(i, nuovo);
                try {
                    repository.salva(libri); //todo spostare il trycatch sotto il ciclo? e mettere un break dopo ogni set ? maybe not
                    notifyObservers();
                } catch (Exception e) {
                    System.err.println("Errore modifica libro.");
                }
                return;
            }
        }
    }
    //fare un metodo di salvataggio privato per non scrivere tutte le volte try catch (?)


    // RICERCA LIBRI
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
        libri.addAll(nuoviLibri);
        try {
            repository.salva(libri);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio.");
        }
        notifyObservers();
    }
    /*
    public void setLibri(List<Libro> nuoviLibri) {
        libri.clear();
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
        }
        try {
            repository.salva(libri);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio.");
        }
        notifyObservers();
    }*/



    /*
    public delLibroMemento save() {
        List<Libro> copiaStato = new ArrayList<>();
        for (Libro libro : this.libri) {
            Libro copia = new Libro.Builder()
                    .titolo(libro.getTitolo())
                    .autore(libro.getAutore())
                    .isbn(libro.getIsbn())
                    .genere(libro.getGenere())
                    .rating(libro.getRating())
                    .statoLettura(libro.getStatoLettura())
                    .build();
            copiaStato.add(copia);
        }
        return new delLibroMemento(copiaStato);
    }

    public void restore(delLibroMemento m) {
        this.libri.clear();
        for (Libro libro : m.getStato()) {
            Libro copia = new Libro.LibroBuilder()
                    .titolo(libro.getTitolo())
                    .autore(libro.getAutore())
                    .isbn(libro.getIsbn())
                    .genere(libro.getGenere())
                    .valutazione(libro.getValutazione())
                    .statoLettura(libro.getStatoLettura())
                    .build();
            this.libri.add(copia);
        }
        sincronizzaRepositoryMemoria();
        notifyObservers();
    }
*/




}

/*
// ================================
// OBSERVER CONCRETI SEMPLICI
// ================================

// Observer per la GUI
class GuiObserver implements Observer {
    private final GestoreLibreria gestore;
    private final javax.swing.table.DefaultTableModel tableModel;

    public GuiObserver(GestoreLibreria gestore, javax.swing.table.DefaultTableModel tableModel) {
        this.gestore = gestore;
        this.tableModel = tableModel;
    }

    @Override
    public void update() {
        // Aggiorna la tabella
        tableModel.setRowCount(0);
        for (Libro libro : gestore.getLibri()) {
            Object[] row = {
                    libro.getTitolo(),
                    libro.getAutore(),
                    libro.getGenere(),
                    libro.getStatoLettura(),
                    libro.getValutazione()
            };
            tableModel.addRow(row);
        }
    }
}


*/
