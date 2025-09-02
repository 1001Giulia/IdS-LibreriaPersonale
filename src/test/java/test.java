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
    @DisplayName("Test per aggiungere un libro e verifica persistenza")
    void testAggiungiLibro() throws IOException {

        Libro libro = new Libro.Builder()
                .titolo("Titolo")
                .autore("Autore")
                .isbn("12345")
                .genere(Genere.GIALLO)
                .rating(4.5)
                .statoLettura(StatoLettura.COMPLETED)
                .build();

        manager.aggiungiLibro(libro);

        // Verifica in memoria
        List<Libro> libri = manager.getLibri();
        assertEquals(1, libri.size());
        assertEquals("Titolo", libri.get(0).getTitolo());
        assertEquals("Autore", libri.get(0).getAutore());
        assertEquals("12345", libri.get(0).getIsbn());
        assertEquals(Genere.GIALLO, libri.get(0).getGenere());

        // Verifica persistenza. Creando nuovo repo simulo la riapertura dell'app (ricaricare dati dal file)
        JsonLibroRepository nuovoRepo = new JsonLibroRepository(tempFile.getAbsolutePath());
        List<Libro> libriDB = nuovoRepo.carica();
        assertEquals(1, libriDB.size());
        assertEquals("Titolo", libriDB.get(0).getTitolo());

        assertTrue(libriDB.contains(libro));

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
    }

    @Test
    @DisplayName("Test eliminazione libro non presente")
    void testEliminaLibroNonPresente() throws IOException {
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
        JsonLibroRepository nuovoRepo = new JsonLibroRepository(tempFile.getAbsolutePath());
        List<Libro> libriDB = nuovoRepo.carica();
        assertEquals(0, libriDB.size());

        assertFalse(libriDB.contains(libroNonPresente));
    }

    @Test
    @DisplayName("Test modifica libro nel manager e persistenza")
    void testModificaLibro() throws IOException {
        Libro libroOg = new Libro.Builder()
                .titolo("Libro 0")
                .autore("Autore 0")
                .isbn("701539393")
                .genere(Genere.FANTASCIENZA)
                .statoLettura(StatoLettura.PLANTOREAD)
                .rating(3.0)
                .build();

        manager.aggiungiLibro(libroOg);

        Libro libroModificato = new Libro.Builder()
                .titolo("Libro 0")
                .autore("Autore 0")
                .isbn("701539393")
                .genere(Genere.FANTASCIENZA)
                .statoLettura(StatoLettura.COMPLETED)
                .rating(5.0)
                .build();

        manager.modificaLibro(libroModificato);

        // Verifica in memoria
        List<Libro> libri = manager.getLibri();
        assertEquals(1, libri.size());
        assertEquals(5.0, libri.get(0).getRating());
        assertEquals(StatoLettura.COMPLETED, libri.get(0).getStatoLettura());

        // Verifica persistenza
        JsonLibroRepository nuovoRepo = new JsonLibroRepository(tempFile.getAbsolutePath());
        List<Libro> libriDB = nuovoRepo.carica();
        assertEquals(1, libriDB.size());
        assertEquals(5.0, libriDB.get(0).getRating());
        assertEquals(StatoLettura.COMPLETED, libriDB.get(0).getStatoLettura());

        assertEquals(libroModificato.getRating(), libriDB.get(0).getRating());
        assertEquals(libroModificato.getStatoLettura(), libriDB.get(0).getStatoLettura());
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
        assertEquals("Libro 1", manager.getLibri().get(0).getTitolo());

        manager.redo();

        assertEquals(2, manager.getLibri().size());
        assertTrue(manager.getLibri().stream().anyMatch(l -> l.getTitolo().equals("Libro 2")));
    }

    @Test
    @DisplayName("Test Command Pattern - AddLibroCmd")
    void testAddLibroCommand() {
        Libro libro = new Libro.Builder()
                .titolo("Command Test")
                .autore("Test Autore")
                .isbn("888888888")
                .build();

        Command addCmd = new AddLibroCmd(manager, libro);

        addCmd.esegui();

        assertEquals(1, manager.getLibri().size());
        assertEquals("Command Test", manager.getLibri().get(0).getTitolo());
    }

    @Test
    @DisplayName("Test Command Pattern - DelLibroCmd")
    void testDelLibroCommand() {
        Libro libro = new Libro.Builder()
                .titolo("Del Test")
                .autore("Test Autore")
                .isbn("999999999")
                .build();

        manager.aggiungiLibro(libro);
        assertEquals(1, manager.getLibri().size());

        Command delCmd = new DelLibroCmd(manager, libro);

        delCmd.esegui();

        assertEquals(0, manager.getLibri().size());
    }

    @Test
    @DisplayName("Test Strategy Pattern - Filtri")
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
    }


    @Test
    @DisplayName("Test Strategy Pattern - Filtro Multiplo (AND)")
    void testFiltroMultiploAnd() {
        Libro libro = new Libro.Builder()
                .titolo("Libro 3")
                .autore("Autore 3")
                .isbn("123456")
                .build();

        Filtro filtroTitolo = new FiltroTitolo("Libro 3");
        Filtro filtroAutore = new FiltroAutore("Autore 3");

        FiltroMultiplo filtroAnd = new FiltroMultiplo(List.of(filtroTitolo, filtroAutore), true);

        assertTrue(filtroAnd.filtra(libro));

        // Libro che non soddisfa filtro titolo
        Libro libroFail = new Libro.Builder()
                .titolo("Libro 4")
                .autore("Autore 3")
                .isbn("95125")
                .build();

        assertFalse(filtroAnd.filtra(libroFail));
    }

    @Test
    @DisplayName("Test Strategy Pattern - Filtro Multiplo (OR)")
    void testFiltroMultiploOr() {
        Libro libro = new Libro.Builder()
                .titolo("Libro 4")
                .autore("Autore 4")
                .isbn("5151")
                .build();

        Filtro filtroTitolo = new FiltroTitolo("Libro 4");
        Filtro filtroAutore = new FiltroAutore("Autore 3");

        FiltroMultiplo filtroOr = new FiltroMultiplo(List.of(filtroTitolo, filtroAutore), false);

        assertTrue(filtroOr.filtra(libro));

        // Libro che non soddisfa nessun filtro
        Libro libroFail = new Libro.Builder()
                .titolo("A")
                .autore("B")
                .isbn("123")
                .build();

        assertFalse(filtroOr.filtra(libroFail));
    }

    @Test
    @DisplayName("Test Builder Pattern e campi obbligatori")
    void testLibroBuilder() {
        // Test costruzione corretta
        Libro libro = new Libro.Builder()
                .titolo("Test Titolo")
                .autore("Test Autore")
                .isbn("TEST123")
                .genere(Genere.AZIONE)
                .rating(4.5)
                .statoLettura(StatoLettura.READING)
                .build();

        assertEquals("Test Titolo", libro.getTitolo());
        assertEquals("Test Autore", libro.getAutore());
        assertEquals("TEST123", libro.getIsbn());
        assertEquals(Genere.AZIONE, libro.getGenere());
        assertEquals(4.5, libro.getRating());
        assertEquals(StatoLettura.READING, libro.getStatoLettura());

        // Test titolo mancante
        assertThrows(IllegalArgumentException.class, () -> {
            new Libro.Builder()
                    .autore("Test Autore")
                    .isbn("TEST123")
                    .build();
        });

        // Test autore mancante
        assertThrows(IllegalArgumentException.class, () -> {
            new Libro.Builder()
                    .titolo("Test Titolo")
                    .isbn("TEST123")
                    .build();
        });

        // Test ISBN mancante
        assertThrows(IllegalArgumentException.class, () -> {
            new Libro.Builder()
                    .titolo("Test Titolo")
                    .autore("Test Autore")
                    .build();
        });
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