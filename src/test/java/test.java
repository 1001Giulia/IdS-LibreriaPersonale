import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.libreriapersonale.model.LibreriaManager;
import org.libreriapersonale.model.libro.*;
import org.libreriapersonale.model.repository.JsonLibroRepository;
import org.libreriapersonale.model.strategy.*;
import org.libreriapersonale.controller.command.*;
import org.libreriapersonale.model.observer.Observer;

@DisplayName("Test LibreriaManager e persistenza")
class LibreriaManagerTest {

    private LibreriaManager manager;
    private JsonLibroRepository repository;
    private File tempFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException { // TempDir serve per creare dir diverse per ogni test / isolare persistenza test
        tempFile = tempDir.resolve("test_libri.json").toFile();
        repository = new JsonLibroRepository(tempFile.getAbsolutePath());
        manager = new LibreriaManager(repository);
    }

    @Test
    @DisplayName("Test pattern builder, aggiunta di un libro e verifica persistenza")
    void testAggiungiLibro() throws IOException {

        Libro libro = new Libro.Builder()
                .titolo("Titolo")
                .autore("Autore")
                .isbn("12345")
                .genere(Genere.GIALLO)
                .rating(4.5)
                .statoLettura(StatoLettura.COMPLETED)
                .build();

        // Test builder: campi corretti
        assertEquals("Titolo", libro.getTitolo());
        assertEquals("Autore", libro.getAutore());
        assertEquals("12345", libro.getIsbn());
        assertEquals(Genere.GIALLO, libro.getGenere());
        assertEquals(4.5, libro.getRating());
        assertEquals(StatoLettura.COMPLETED, libro.getStatoLettura());

        
        manager.aggiungiLibro(libro);

        // Verifica in memoria
        List<Libro> libri = manager.getLibri();
        assertEquals(1, libri.size());
        assertEquals("Titolo", libri.getFirst().getTitolo());
        assertEquals("Autore", libri.getFirst().getAutore());
        assertEquals("12345", libri.getFirst().getIsbn());

        // Verifica persistenza. Creando nuovo repo simulo la riapertura dell'app (ricaricare dati dal file)
        JsonLibroRepository nuovoRepo = new JsonLibroRepository(tempFile.getAbsolutePath());
        List<Libro> libriDB = nuovoRepo.carica();
        assertEquals(1, libriDB.size());
        assertEquals("Titolo", libriDB.getFirst().getTitolo());

        assertTrue(libriDB.contains(libro));

        // Test builder: campi obbligatori

        // Test titolo mancante
        assertThrows(IllegalArgumentException.class, () -> {
            new Libro.Builder()
                    .autore("Autore")
                    .isbn("123")
                    .build();
        });

        // Test autore mancante
        assertThrows(IllegalArgumentException.class, () -> {
            new Libro.Builder()
                    .titolo("Titolo")
                    .isbn("123")
                    .build();
        });

        // Test ISBN mancante
        assertThrows(IllegalArgumentException.class, () -> {
            new Libro.Builder()
                    .titolo("Titolo")
                    .autore("Autore")
                    .build();
        });

    }

    @Test
    @DisplayName("Test eliminazione libro, verificare persistenza, e test libro non presente")
    void testEliminaLibro() throws IOException {
        Libro libro = new Libro.Builder()
                .titolo("1984")
                .autore("George Orwell")
                .isbn("978-0451524935")
                .genere(Genere.FANTASCIENZA)
                .build();

        manager.aggiungiLibro(libro);
        assertEquals(1, manager.getLibri().size());

        manager.eliminaLibro(libro);

        // Verifica in memoria
        assertEquals(0, manager.getLibri().size());

        // Verifica persistenza
        JsonLibroRepository nuovoRepo = new JsonLibroRepository(tempFile.getAbsolutePath());
        List<Libro> libriDB = nuovoRepo.carica();
        assertEquals(0, libriDB.size());

        assertFalse(libriDB.contains(libro));



        Libro libroNonPresente = new Libro.Builder()
                .titolo("???")
                .autore("?!!")
                .isbn("0000")
                .build();

        // Tentativo di eliminazione
        manager.eliminaLibro(libroNonPresente);

        // In memoria non cambia nulla (rimane vuoto)
        assertEquals(0, manager.getLibri().size());

        // Verifica persistenza
        JsonLibroRepository nuovoRepo1 = new JsonLibroRepository(tempFile.getAbsolutePath());
        List<Libro> libriDB1 = nuovoRepo1.carica();
        assertEquals(0, libriDB1.size());

        assertFalse(libriDB1.contains(libroNonPresente));
    }

    @Test
    @DisplayName("Test undo e redo")
    void testUndoRedo() {
        Libro libro1 = new Libro.Builder()
                .titolo("Libro 1")
                .autore("Autore 1")
                .isbn("ISBN-001")
                .genere(Genere.ROMANTICO)
                .build();

        Libro libro2 = new Libro.Builder()
                .titolo("Libro 2")
                .autore("Autore 2")
                .isbn("ISBN-002")
                .genere(Genere.THRILLER)
                .build();

        manager.aggiungiLibro(libro1);
        manager.aggiungiLibro(libro2);
        assertEquals(2, manager.getLibri().size());

        manager.undo();

        assertEquals(1, manager.getLibri().size());
        assertEquals("Libro 1", manager.getLibri().getFirst().getTitolo());

        manager.redo();

        assertEquals(2, manager.getLibri().size());
        assertTrue(manager.getLibri().stream().anyMatch(l -> l.getTitolo().equals("Libro 2")));
    }

    @Test
    @DisplayName("Test Command Pattern - Add,Edit,Del LibroCmd. Check persistenza dopo l'edit")
    void testAddDelLibroCommand() throws IOException {

        // Addo
        Libro libro = new Libro.Builder()
                .titolo("Libro 0")
                .autore("Autore 0")
                .isbn("701539393")
                .genere(Genere.FANTASCIENZA)
                .statoLettura(StatoLettura.PLANTOREAD)
                .rating(3.0)
                .build();

        Command addCmd = new AddLibroCmd(manager, libro);
        addCmd.esegui();

        assertEquals(1, manager.getLibri().size());
        assertEquals("Libro 0", manager.getLibri().getFirst().getTitolo());


        // Modifico
        Libro libroModificato = new Libro.Builder()
                .titolo("Libro 0")
                .autore("Autore 0")
                .isbn("701539393") // Stesso ISBN per poterlo modificare correttamente (metodo in manager)
                .genere(Genere.FANTASCIENZA)
                .statoLettura(StatoLettura.COMPLETED) // Campo editato
                .rating(5.0) // Campo editato
                .build();

        Command editCmd = new EditLibroCmd(manager, libroModificato);
        editCmd.esegui();

        // Verifica in memoria
        List<Libro> libri = manager.getLibri();
        assertEquals(1, libri.size());
        assertEquals(5.0, libri.getFirst().getRating());
        assertEquals(StatoLettura.COMPLETED, libri.getFirst().getStatoLettura());

        // Verifica persistenza
        JsonLibroRepository nuovoRepo = new JsonLibroRepository(tempFile.getAbsolutePath());
        List<Libro> libriDB = nuovoRepo.carica();
        assertEquals(1, libriDB.size());
        assertEquals(5.0, libriDB.getFirst().getRating());
        assertEquals(StatoLettura.COMPLETED, libriDB.getFirst().getStatoLettura());

        assertEquals(libroModificato.getRating(), libriDB.getFirst().getRating());
        assertEquals(libroModificato.getStatoLettura(), libriDB.getFirst().getStatoLettura());


        // Elimino
        Command delCmd = new DelLibroCmd(manager, libro);
        delCmd.esegui();

        assertEquals(0, manager.getLibri().size());
    }

    @Test
    @DisplayName("Test Strategy Pattern - Filtri singoli e multipli")
    void testFiltriStrategy() {
        Libro libro1 = new Libro.Builder()
                .titolo("Libro Fantasy")
                .autore("Autore Fantasy ")
                .isbn("FAN1")
                .genere(Genere.FANTASY)
                .statoLettura(StatoLettura.COMPLETED)
                .rating(4.0)
                .build();

        Libro libro2 = new Libro.Builder()
                .titolo("Libro Romance")
                .autore("Autore Romance")
                .isbn("ROM1")
                .genere(Genere.ROMANTICO)
                .statoLettura(StatoLettura.READING)
                .rating(3.0)
                .build();

        // non aggiungo a repo cuz testo su singoli libri non collez

        // Test FiltroGenere
        FiltroGenere filtroFantasy = new FiltroGenere(Genere.FANTASY);
        assertTrue(filtroFantasy.filtra(libro1));
        assertFalse(filtroFantasy.filtra(libro2));

        // Test FiltroStatoLettura
        FiltroStatoLettura filtroCompleted = new FiltroStatoLettura(StatoLettura.COMPLETED);
        assertTrue(filtroCompleted.filtra(libro1));
        assertFalse(filtroCompleted.filtra(libro2));

        // Test FiltroValutazione
        FiltroValutazione filtroRating4 = new FiltroValutazione(4);
        assertTrue(filtroRating4.filtra(libro1));
        assertFalse(filtroRating4.filtra(libro2));

        // Test FiltroAutore
        FiltroAutore filtroAutore = new FiltroAutore("Fantasy");
        assertTrue(filtroAutore.filtra(libro1));
        assertFalse(filtroAutore.filtra(libro2));

        // Test FiltroTitolo
        FiltroTitolo filtroTitoloFantasy = new FiltroTitolo("Fantasy");
        assertTrue(filtroTitoloFantasy.filtra(libro1));
        assertFalse(filtroTitoloFantasy.filtra(libro2));

        // Filtri multipli - AND

        Libro libroAND = new Libro.Builder()
                .titolo("Libro 3")
                .autore("Autore 3")
                .isbn("123456")
                .build();

        Filtro filtroTitolo1 = new FiltroTitolo("Libro 3");
        Filtro filtroAutore1 = new FiltroAutore("Autore 3");

        FiltroMultiplo filtroAnd = new FiltroMultiplo(List.of(filtroTitolo1, filtroAutore1), true);

        assertTrue(filtroAnd.filtra(libroAND));

        // Libro che non soddisfa filtro titolo
        Libro libroFail = new Libro.Builder()
                .titolo("Libro 4")
                .autore("Autore 3")
                .isbn("95125")
                .build();

        assertFalse(filtroAnd.filtra(libroFail));

        // Filtri multipli - OR

        Libro libroOR = new Libro.Builder()
                .titolo("Libro 4")
                .autore("Autore 4")
                .isbn("5151")
                .build();

        Filtro filtroTitolo2 = new FiltroTitolo("Libro 4");
        Filtro filtroAutore2 = new FiltroAutore("Autore 3");

        FiltroMultiplo filtroOr = new FiltroMultiplo(List.of(filtroTitolo2, filtroAutore2), false);

        assertTrue(filtroOr.filtra(libroOR));

        // Libro che non soddisfa nessun filtro
        Libro libroFail2 = new Libro.Builder()
                .titolo("A")
                .autore("B")
                .isbn("123")
                .build();

        assertFalse(filtroOr.filtra(libroFail2));

    }

    private static class TestObserver implements Observer {
        public int updateCount = 0;
        @Override
        public void update() { updateCount++; }
    }

    @Test
    @DisplayName("Test pattern Observer")
    void testObserverPattern() {
        TestObserver observer = new TestObserver();
        manager.addObserver(observer);

        Libro libro = new Libro.Builder()
                .titolo("Observer Test")
                .autore("Test Autore")
                .isbn("777777777")
                .build();

        manager.aggiungiLibro(libro);
        manager.eliminaLibro(libro);

        assertEquals(2, observer.updateCount);
    }
}


