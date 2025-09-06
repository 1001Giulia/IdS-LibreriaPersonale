package org.libreriapersonale.model.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.libreriapersonale.model.libro.Libro;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JsonLibroRepository implements LibroRepository {
    private final String filename;
    private final Gson gson;

    public JsonLibroRepository(String filename) {
        this.filename = filename;
        this.gson = new GsonBuilder()
                .setPrettyPrinting() // scrive il JSON formattato
                .create();
    }

    @Override
    public void salva(List<Libro> libri) throws IOException {
        File originalFile = new File(filename);
        File backupFile = new File(filename + ".bak");

        // Backup del file esistente
        if (originalFile.exists()) {
            Files.copy(originalFile.toPath(), backupFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        // Scrivo sul file og (per sicurezza maggiore si potrebbe scrivere su un
        // file temporaneo, ma considerando che l'app è mono utente
        // ha poche scritture e non è "importante" credo si possa evitare)
        try (Writer writer = new FileWriter(originalFile)) {
            gson.toJson(libri, writer);
        }
    }

    @Override
    public List<Libro> carica() throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Libro>>(){}.getType();
            List<Libro> libri = gson.fromJson(reader, listType);
            return libri != null ? libri : new ArrayList<>();
        }
    }
}