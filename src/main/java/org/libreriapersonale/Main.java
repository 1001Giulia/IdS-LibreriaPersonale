package org.libreriapersonale;



import org.libreriapersonale.model.libro.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        //Libro test = new Libro.Builder("Titolo", "Autore", "1234").statoLettura(StatoLettura.READING).build();
        Libro test = new Libro.Builder()
                .titolo("Titolo")
                .autore("Autore")
                .isbn("1234")
                .statoLettura(StatoLettura.READING)
                .build();
        System.out.println(test.toString());


        Scanner scanner = new Scanner(System.in);
        /*System.out.print("Inserisci nuovo rating (0-5): ");
        int nr = scanner.nextInt();
        test.aggiornaRating(nr);*/


        test.aggiornaIsbn("129740");
        test.rimuoviTag("Comedy");
        test.aggiungiTag("Horror");
        test.aggiornaRating(5);
        System.out.println(test.toString());

    }
}