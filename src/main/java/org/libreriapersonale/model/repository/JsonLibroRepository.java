package org.libreriapersonale.model.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.libreriapersonale.model.libro.Libro;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonLibroRepository implements LibroRepository {
    private final String filename;
    private final Gson gson;

    public JsonLibroRepository(String filename) {
        this.filename = filename;
        this.gson = new GsonBuilder()
                .setPrettyPrinting() // scrive il JSON formatttato
                .create();
    }

    @Override
    public void salva(List<Libro> libri) throws IOException {
        try (Writer writer = new FileWriter(filename)) {
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
