package org.libreriapersonale.view;

import org.libreriapersonale.controller.command.*;
import org.libreriapersonale.model.LibreriaManager;
import org.libreriapersonale.model.libro.*;
import org.libreriapersonale.model.observer.Observer;
import org.libreriapersonale.model.repository.JsonLibroRepository;
import org.libreriapersonale.model.strategy.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LibreriaGUI extends JFrame implements Observer {

    private final LibreriaManager manager;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Campi per aggiungere/modificare libro
    private JTextField titoloField;
    private JTextField autoreField;
    private JTextField isbnField;
    private JComboBox<Genere> genereCombo;
    private JComboBox<StatoLettura> statoCombo;
    private JSlider ratingSlider;
    private JLabel ratingLabel;

    // Bottoni dinamici del form
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton clearButton;

    // Campi per filtri
    private JTextField filtroTitoloField;
    private JTextField filtroAutoreField;
    private JComboBox<Genere> filtroGenereCombo;
    private JComboBox<StatoLettura> filtroStatoCombo;
    private JSlider filtroRatingSlider;
    private JLabel filtroRatingLabel;
    private JCheckBox filtroAndCheckbox;
    private JTextArea testoFiltri;

    // Lista completa dei libri (senza filtri)
    private List<Libro> libriCompleti = new ArrayList<>();

    // Flag per sapere se ci sono filtri attivi
    private boolean filtriAttivi = false;

    public LibreriaGUI() {
        // Inizializza il manager
        JsonLibroRepository repository = new JsonLibroRepository("libri.json");
        manager = new LibreriaManager(repository);
        manager.addObserver(this);

        inizializzaGUI();
        update(); // Carica i dati iniziali
    }

    private void inizializzaGUI() {
        setTitle("Libreria Personale");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Menu Bar
        setJMenuBar(createMenuBar());
        // Main frame diviso in 2
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // SX: parte per aggiungere/modificare
        JPanel editPanel = createPanel();
        editPanel.setMinimumSize(new Dimension(280, 400));
        splitPane.setLeftComponent(editPanel);
        // DX: Tabella con i libri
        JPanel tablePanel = createTablePanel();
        tablePanel.setMinimumSize(new Dimension(300, 400));
        splitPane.setRightComponent(tablePanel);


        splitPane.setDividerLocation(350);
        add(splitPane, BorderLayout.CENTER);

        pack(); // Compatta automaticamente
        setMinimumSize(new Dimension(500, 400));
        //setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Modifica
        JMenu modificaMenu = new JMenu("Modifica");

        JMenuItem undoMenuItem = new JMenuItem("Annulla");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));
        undoMenuItem.addActionListener(e -> manager.undo());

        JMenuItem redoMenuItem = new JMenuItem("Ripeti");
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Y"));
        redoMenuItem.addActionListener(e -> manager.redo());

        modificaMenu.add(undoMenuItem);
        modificaMenu.add(redoMenuItem);

        // Menu Filtri
        JMenu filtriMenu = new JMenu("Filtri");

        JMenuItem apriFiltriMenuItem = new JMenuItem("Apri Filtri");
        apriFiltriMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl F"));
        apriFiltriMenuItem.addActionListener(e -> apriFinestraFiltri());

        JMenuItem azzeraFiltriMenuItem = new JMenuItem("Azzera Filtri");
        azzeraFiltriMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
        azzeraFiltriMenuItem.addActionListener(e -> azzeraFiltri());

        filtriMenu.add(apriFiltriMenuItem);
        filtriMenu.add(azzeraFiltriMenuItem);

        // Menu Visualizza
        JMenu visualizzaMenu = new JMenu("Visualizza");

        JMenuItem statisticheMenuItem = new JMenuItem("Statistiche");
        statisticheMenuItem.addActionListener(e -> mostraStatistiche());

        JMenuItem informazioniMenuItem = new JMenuItem("Informazioni");
        informazioniMenuItem.addActionListener(e -> mostraInformazioni());

        visualizzaMenu.add(statisticheMenuItem);
        //visualizzaMenu.addSeparator();
        visualizzaMenu.add(informazioniMenuItem);

        //menuBar.add(fileMenu);
        menuBar.add(modificaMenu);
        menuBar.add(filtriMenu);
        menuBar.add(visualizzaMenu);

        return menuBar;
    }

    private JPanel createFiltroPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Filtri"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        //Prima riga - Titolo e Autore
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Titolo:"), gbc);
        gbc.gridx = 1;
        filtroTitoloField = new JTextField(10);
        panel.add(filtroTitoloField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Autore:"), gbc);
        gbc.gridx = 3;
        filtroAutoreField = new JTextField(10);
        panel.add(filtroAutoreField, gbc);

        // Seconda riga - Genere e Stato
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Genere:"), gbc);
        gbc.gridx = 1;
        filtroGenereCombo = new JComboBox<>();
        filtroGenereCombo.addItem(null); //opzione tutti
        for (Genere g : Genere.values()) {
            filtroGenereCombo.addItem(g);
        }
        panel.add(filtroGenereCombo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Stato:"), gbc);
        gbc.gridx = 3;
        filtroStatoCombo = new JComboBox<>();
        filtroStatoCombo.addItem(null); // Opzione "Tutti"
        for (StatoLettura s : StatoLettura.values()) {
            filtroStatoCombo.addItem(s);
        }
        panel.add(filtroStatoCombo, gbc);

        // Terza riga - Rating e controlli
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Rating min:"), gbc);
        gbc.gridx = 1;

        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filtroRatingSlider = new JSlider(0, 10, 0);
        filtroRatingSlider.setPreferredSize(new Dimension(100, 25));
        filtroRatingLabel = new JLabel("0/5");
        filtroRatingLabel.setPreferredSize(new Dimension(50, 25));

        filtroRatingSlider.addChangeListener(e -> {
            double rating = filtroRatingSlider.getValue() / 2.0;
            if (rating == (int) rating) { // se intero
                filtroRatingLabel.setText(String.format("%d/5", (int) rating));
            } else {
                filtroRatingLabel.setText(String.format(Locale.US, "%.1f/5", rating)); // Preferenza personale, punto ai decimali
            }
        });

        ratingPanel.add(filtroRatingSlider);
        ratingPanel.add(filtroRatingLabel);
        panel.add(ratingPanel, gbc);

        gbc.gridx = 2;
        filtroAndCheckbox = new JCheckBox("Tutti i filtri");
        filtroAndCheckbox.setSelected(true);
        panel.add(filtroAndCheckbox, gbc);

        // Bottoni
        gbc.gridx = 3;
        JPanel buttonFiltroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));

        JButton applicaFiltroBtn = new JButton("Applica Filtri");
        applicaFiltroBtn.addActionListener(e -> applicaFiltri());

        JButton azzeraFiltroBtn = new JButton("Azzera Filtri");
        azzeraFiltroBtn.addActionListener(e -> azzeraFiltri());

        buttonFiltroPanel.add(applicaFiltroBtn);
        buttonFiltroPanel.add(azzeraFiltroBtn);
        panel.add(buttonFiltroPanel, gbc);

        return panel;
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Aggiungi Libri"));

        // Pannello centrale con i campi
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Titolo
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Titolo:"), gbc);
        gbc.gridx = 1;
        titoloField = new JTextField(15);
        fieldsPanel.add(titoloField, gbc);

        // Autore
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Autore:"), gbc);
        gbc.gridx = 1;
        autoreField = new JTextField(15);
        fieldsPanel.add(autoreField, gbc);

        // ISBN
        gbc.gridx = 0;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        isbnField = new JTextField(15);
        fieldsPanel.add(isbnField, gbc);

        // Genere
        gbc.gridx = 0;
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Genere:"), gbc);
        gbc.gridx = 1;
        genereCombo = new JComboBox<>(Genere.values());
        genereCombo.setPreferredSize(new Dimension(140, 25));
        genereCombo.setSelectedItem(Genere.ALTRO);
        /*genereCombo = new JComboBox<>(); per averlo vuoto senza val di default
        genereCombo.addItem(null);
        for (Genere g : Genere.values()) {
            filtroGenereCombo.addItem(g);
        }*/

        fieldsPanel.add(genereCombo, gbc);

        // Stato Lettura
        gbc.gridx = 0;
        gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Stato:"), gbc);
        gbc.gridx = 1;
        statoCombo = new JComboBox<>(StatoLettura.values());
        statoCombo.setSelectedItem(StatoLettura.READING);
        statoCombo.setPreferredSize(new Dimension(140, 25));
        fieldsPanel.add(statoCombo, gbc);

        // Rating
        gbc.gridx = 0;
        gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Rating:"), gbc);
        gbc.gridx = 1;
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ratingSlider = new JSlider(0, 10, 0);
        ratingSlider.setPreferredSize(new Dimension(120, 30));
        ratingLabel = new JLabel("0/5");
        ratingLabel.setPreferredSize(new Dimension(60, 30));

        ratingSlider.addChangeListener(e -> {
            double rating = ratingSlider.getValue() / 2.0;
            if (rating == (int) rating) {
                // Senza decimali se è intero
                ratingLabel.setText(String.format("%d/5", (int) rating));
            } else {
                ratingLabel.setText(String.format(Locale.US, "%.1f/5", rating));
            }
        });
        ratingPanel.add(ratingSlider);
        ratingPanel.add(Box.createHorizontalStrut(5));
        ratingPanel.add(ratingLabel);
        fieldsPanel.add(ratingPanel, gbc);

        // Bottoni sempre in basso
        JPanel buttonPanel = createButtonPanel();

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Prima riga
        JPanel topRow = new JPanel(new FlowLayout());
        addButton = new JButton("Aggiungi Libro");
        addButton.addActionListener(e -> aggiungiLibro());
        topRow.add(addButton);

        clearButton = new JButton("Azzera campi");
        clearButton.addActionListener(e -> azzeraCampi());
        topRow.add(clearButton);

        // Seconda riga
        JPanel bottomRow = new JPanel(new FlowLayout());
        editButton = new JButton("Modifica Libro");
        editButton.addActionListener(e -> modificaLibro());
        //editButton.setVisible(false);
        editButton.setEnabled(false);
        editButton.setToolTipText("Seleziona un libro da modificare"); // Esce fuori passandoci sopra col mouse

        bottomRow.add(editButton);

        deleteButton = new JButton("Elimina Libro");
        deleteButton.addActionListener(e -> eliminaLibro());
        //deleteButton.setVisible(false);
        deleteButton.setEnabled(false);
        bottomRow.add(deleteButton);
        deleteButton.setToolTipText("Seleziona un libro da eliminare");

        mainPanel.add(topRow, BorderLayout.NORTH);
        mainPanel.add(bottomRow, BorderLayout.SOUTH);

        return mainPanel;
    }

    // DB libri
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista Libri"));

        // Indicatore filtri attivi
        testoFiltri = new JTextArea();
        testoFiltri.setEditable(false);
        testoFiltri.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        testoFiltri.setBackground(getBackground()); // Sfondo per mimetizzarsi
        testoFiltri.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        testoFiltri.setVisible(false); // Nascosto all'inizio
        panel.add(testoFiltri, BorderLayout.NORTH);

        // Fix per quando la finestra è troppo piccola e i filtri sono troppi
        testoFiltri.setLineWrap(true); // A capo automaticamente
        testoFiltri.setWrapStyleWord(true); // non spezza le parole

        // Modello tabella
        String[] colonne = {"Titolo", "Autore", "ISBN", "Genere", "Stato", "Rating"};
        tableModel = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabella non editabile
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Aggiungi sorting alla tabella
        tableSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(tableSorter);


        table.getSelectionModel().addListSelectionListener(e -> { // Listener alla selezione della JTable
            if (!e.getValueIsAdjusting()) { // evita doppie chiamate              // Selection model gestisce quale riga è selez
                int selectedRow = table.getSelectedRow();                         // Quando cambia la selezione viene eseguito questo blocco
                if (selectedRow >= 0) { // Con questo verifico se qualcosa è stato selezionato (table.get.. res -1 se non c'è selez)
                    // Converti l'indice della vista (gui) nell'indice del modello (dati)
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    caricaDatiNelForm(modelRow);
                    // Mostra i bottoni modifica ed elimina
                    // editButton.setVisible(true);
                    // deleteButton.setVisible(true);
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    addButton.setEnabled(false);
                } else {
                    // Nascondi i bottoni quando non c'è selezione
                    //editButton.setVisible(false);
                    //deleteButton.setVisible(false);
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    addButton.setEnabled(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void apriFinestraFiltri() {
        // Creaz finestra per i filtri
        JDialog filtroDialog = new JDialog(this, "Filtri Avanzati", true);
        filtroDialog.setLayout(new BorderLayout());

        filtroDialog.setResizable(false);

        // Contenuto finestra
        JPanel filtroPanel = createFiltroPanel();
        filtroDialog.add(filtroPanel, BorderLayout.CENTER);

        // Pulsanti della finestra
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            applicaFiltri();
            filtroDialog.dispose();
        });

        JButton cancelButton = new JButton("Annulla");
        cancelButton.addActionListener(e -> filtroDialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        filtroDialog.add(buttonPanel, BorderLayout.SOUTH);

        filtroDialog.pack(); // Compatta finestra alle dim minima per le componenti
        filtroDialog.setLocationRelativeTo(this);
        filtroDialog.setVisible(true);
    }

    private void aggiornaTestoFiltri() {
        if (!filtriAttivi) {
            testoFiltri.setVisible(false);
            return;
        }

        StringBuilder info = new StringBuilder("[Filtri attivi] ");
        List<String> filtriAttivi = getStrings();

        if (filtriAttivi.isEmpty()) {
            testoFiltri.setVisible(false); // Nascondo il testo se nessun filtro è attivo
        } else {
            // Unisco i vari filtri
            for (int i = 0; i < filtriAttivi.size(); i++) {
                info.append(filtriAttivi.get(i));
                if (i < filtriAttivi.size() - 1) { // non aggiungo la virgola solo all'ultimo elemento
                    info.append(", ");
                }
            }
            // si poteva fare con info.append(String.join(", ", filtriAttivi) ops

            if (filtroAndCheckbox != null) {
                info.append(" (").append(filtroAndCheckbox.isSelected() ? "Tutti i filtri" : "Almeno un filtro").append(")");
            }
            testoFiltri.setText(info.toString());
            testoFiltri.setVisible(true);
        }
    }

    private List<String> getStrings() {
        List<String> filtriAttivi = new ArrayList<>();

        // Controllo se il campo esiste (controllo giusto per sicurezza ma inutile) e contiene del testo (non vuoto, trimmandolo prima)
        if (filtroTitoloField != null && !filtroTitoloField.getText().trim().isEmpty()) {
            filtriAttivi.add("Titolo: \"" + filtroTitoloField.getText().trim() + "\"");
        }
        if (filtroAutoreField != null && !filtroAutoreField.getText().trim().isEmpty()) {
            filtriAttivi.add("Autore: \"" + filtroAutoreField.getText().trim() + "\"");
        }
        if (filtroGenereCombo != null && filtroGenereCombo.getSelectedItem() != null) {
            filtriAttivi.add("Genere: " + filtroGenereCombo.getSelectedItem());
        }
        if (filtroStatoCombo != null && filtroStatoCombo.getSelectedItem() != null) {
            filtriAttivi.add("Stato: " + filtroStatoCombo.getSelectedItem());
        }

        if (filtroRatingSlider != null && filtroRatingSlider.getValue() > 0) { // Rating > 0
            double rating = filtroRatingSlider.getValue() / 2.0; // Converte 0-10 dello slider in 0-5
            if (rating == (int) rating) { // se è un numero intero non c'è bisogno di mostrare i decimali
                filtriAttivi.add("Rating min: " + (int) rating + "/5");
            } else {
                filtriAttivi.add("Rating min: " + String.format(Locale.US, "%.1f/5", rating));
            }
        }
        return filtriAttivi;
    }


    private void applicaFiltri() {
        List<Filtro> filtri = new ArrayList<>();

        String titolo = filtroTitoloField.getText().trim();
        if (!titolo.isEmpty()) {
            filtri.add(new FiltroTitolo(titolo));
        }

        String autore = filtroAutoreField.getText().trim();
        if (!autore.isEmpty()) {
            filtri.add(new FiltroAutore(autore));
        }

        Genere genere = (Genere) filtroGenereCombo.getSelectedItem();
        if (genere != null) {
            filtri.add(new FiltroGenere(genere));
        }

        StatoLettura stato = (StatoLettura) filtroStatoCombo.getSelectedItem();
        if (stato != null) {
            filtri.add(new FiltroStatoLettura(stato));
        }

        double ratingMin = filtroRatingSlider.getValue() / 2.0;
        if (ratingMin > 0) {
            filtri.add(libro -> libro.getRating() >= ratingMin);
        }

        if (filtri.isEmpty()) {
            // Nessun filtro, mostra tutti i libri
            filtriAttivi = false;
            aggiornaTabella(libriCompleti);
            aggiornaTestoFiltri();
            return;
        }

        // Applica i filtri
        filtriAttivi = true;
        boolean useAnd = filtroAndCheckbox.isSelected();
        Filtro filtroFinale = new FiltroMultiplo(filtri, useAnd);

        List<Libro> libriFiltrati = new ArrayList<>();
        for (Libro libro : libriCompleti) {
            if (filtroFinale.filtra(libro)) {
                libriFiltrati.add(libro);
            }
        }

        aggiornaTabella(libriFiltrati);
        aggiornaTestoFiltri();
    }


    private void azzeraFiltri() {

        if (filtroTitoloField != null) {
            filtroTitoloField.setText("");
            filtroAutoreField.setText("");
            filtroGenereCombo.setSelectedItem(null);
            filtroStatoCombo.setSelectedItem(null);
            filtroRatingSlider.setValue(0);
            filtroRatingLabel.setText("0/5");
            filtroAndCheckbox.setSelected(true);
        }

        // Pulisci la selezione prima di aggiornare
        // table.clearSelection();
        // Il TableRowSorter si aspetta sempre tutti i dati, ma modifico direttamente il DefaultTableMOodel
        // quindi non trova più per esempio il 5o elemento dopo aver filtrato
        // Inoltre facendo così disabilito i tasti di modifica ed elimina

        // Non serve più ho fixato direttamente aggiornaTabella, gestisce la selezione (mantiene la selez se c'è ancora)

        // Mostra i libri
        filtriAttivi = false;
        aggiornaTabella(libriCompleti);
        aggiornaTestoFiltri();
    }

    private void aggiornaTabella(List<Libro> libri) {
        // Salva la selezione corrente (se presente)
        int selectedRow = table.getSelectedRow();
        String selectedIsbn = null;
        if (selectedRow >= 0) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            selectedIsbn = (String) tableModel.getValueAt(modelRow, 2);
        }

        tableModel.setRowCount(0);
        for (Libro libro : libri) {
            Object[] row = {
                    libro.getTitolo(),
                    libro.getAutore(),
                    libro.getIsbn(),
                    libro.getGenere(),
                    libro.getStatoLettura(),
                    libro.getRating()
            };
            tableModel.addRow(row);
        }

        // Ripristina la selezione se il libro è ancora presente
        if (selectedIsbn != null) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (selectedIsbn.equals(tableModel.getValueAt(i, 2))) {
                    // Conversione indice modello (dati) -> indice vista (ciò che vede l'utente)
                    int viewRow = table.convertRowIndexToView(i);
                    if (viewRow >= 0) {
                        table.setRowSelectionInterval(viewRow, viewRow);
                    }
                    break;
                }
            }
        }
    }

    private void mostraStatistiche() {
        List<Libro> libri = manager.getLibri();
        int totale = libri.size();

        int n = StatoLettura.values().length;
        int[] cont = new int[n];
        double ratingMedio = 0;
        int libriRatati = 0;

        for (Libro libro : libri) {
            // Conta per stato
            for (int i = 0; i < n; i++) {
                if (libro.getStatoLettura() == StatoLettura.values()[i]) {
                    cont[i]++;
                    break;
                }
            }
            // Rating medio
            if (libro.getRating() > 0) {
                ratingMedio += libro.getRating();
                libriRatati++;
            }
        }

        if (libriRatati > 0) {
            ratingMedio /= libriRatati;
        }

        StringBuilder stats = new StringBuilder();
        stats.append("Totale libri: ").append(totale).append("\n\n");

        for (int i = 0; i < n; i++) {
            stats.append("- ").append(StatoLettura.values()[i]).append(": ").append(cont[i]).append("\n");
        }

        stats.append("\nRating medio: ").append(String.format("%.2f", ratingMedio));

        JOptionPane.showMessageDialog(this, stats.toString(), "Statistiche", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostraInformazioni() {
        String info = """
                Libreria Personale
                
                Versione: 1.0
                Studente: Giulia Pignanelli
                Matricola: 239588
                
                
                Progetto di Ingegneria del Software
                AA 2024-2025""";
        JOptionPane.showMessageDialog(this, info, "Informazioni", JOptionPane.INFORMATION_MESSAGE);
    }

    private void caricaDatiNelForm(int row) {
        titoloField.setText((String) tableModel.getValueAt(row, 0));
        autoreField.setText((String) tableModel.getValueAt(row, 1));
        isbnField.setText((String) tableModel.getValueAt(row, 2));
        isbnField.setEditable(false);
        genereCombo.setSelectedItem(tableModel.getValueAt(row, 3));
        statoCombo.setSelectedItem(tableModel.getValueAt(row, 4));

        // Carica rating sullo slider del rating
        Object ratingObj = tableModel.getValueAt(row, 5);
        if (ratingObj instanceof Number) {
            double rating = ((Number) ratingObj).doubleValue();
            ratingSlider.setValue((int) (rating * 2)); // Converte 0-5 in 0-10
        }
    }

    private void aggiungiLibro() {
        try {
            Libro libro = new Libro.Builder()
                    .titolo(titoloField.getText().trim())
                    .autore(autoreField.getText().trim())
                    .isbn(isbnField.getText().trim())
                    .genere((Genere) genereCombo.getSelectedItem())
                    .statoLettura((StatoLettura) statoCombo.getSelectedItem())
                    .rating(ratingSlider.getValue() / 2.0) // 0-10 dello slider in 0-5
                    .build();

            Command cmd = new AddLibroCmd(manager, libro);
            cmd.esegui();
            azzeraCampi();

        } catch (IllegalArgumentException e) { // Gestisce pure l'eccezione di libro duplicato, oltre a quelle nel builder
            JOptionPane.showMessageDialog(this, e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificaLibro() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) { // Ho tolto il bottone sempre visibile ma lo lascio per sicurezza you never know
            JOptionPane.showMessageDialog(this, "Seleziona un libro da modificare.",
                    "Nessuna Selezione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Libro libro = new Libro.Builder()
                    .titolo(titoloField.getText().trim())
                    .autore(autoreField.getText().trim())
                    .isbn(isbnField.getText().trim())
                    .genere((Genere) genereCombo.getSelectedItem())
                    .statoLettura((StatoLettura) statoCombo.getSelectedItem())
                    .rating(ratingSlider.getValue() / 2.0) // Converte 0-10 in 0-5
                    .build();

            Command cmd = new EditLibroCmd(manager, libro);
            cmd.esegui();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Errore: " + e.getMessage(),
                    "Errore Validazione", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminaLibro() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) { // Anche qua ho tolto il bottone ma okay lo lascio
            JOptionPane.showMessageDialog(this, "Seleziona un libro da eliminare!",
                    "Nessuna Selezione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Conferma eliminazione
        int conferma = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler eliminare questo libro?",
                "Conferma Eliminazione", JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null);

        if (conferma == JOptionPane.YES_OPTION) {
            // Indice vista -> indice modello
            int modelRow = table.convertRowIndexToModel(selectedRow); // selectedRow id della riga visibile nella tab, converto all'id del dato reale
            String isbn = (String) tableModel.getValueAt(modelRow, 2);
            Libro libro = manager.cercaPerIsbn(isbn);
            if (libro != null) {
                Command cmd = new DelLibroCmd(manager, libro);
                cmd.esegui();
                azzeraCampi();
            }
        }
    }

    private void azzeraCampi() {
        titoloField.setText("");
        autoreField.setText("");
        isbnField.setText("");
        isbnField.setEditable(true);
        genereCombo.setSelectedItem(Genere.ALTRO);
        statoCombo.setSelectedItem(StatoLettura.READING);
        ratingSlider.setValue(0);
        ratingLabel.setText("0/5");
        table.clearSelection();
        // Nascondi bottoni
        //editButton.setVisible(false);
        //deleteButton.setVisible(false);
        deleteButton.setEnabled(false);
        editButton.setEnabled(false);
    }

    // Observer
    @Override
    public void update() {
        // Salva la lista completa dei libri
        libriCompleti = new ArrayList<>(manager.getLibri());
        // No filtri attivi -> aggiorna normalmente
        if (!filtriAttivi) {
            aggiornaTabella(libriCompleti);
        } else {
            // Riapplica i filtri
            applicaFiltri(); // E faccio aggiornaTabella con i libri filtrati
        }
    }


    /*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LibreriaGUI().setVisible(true);
        });
    }
    */
}