package org.maxnnsu;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.h2.tools.Server;
import org.maxnnsu.model.DosarDataModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import static org.maxnnsu.H2DatabaseManager.createDosarTables;
import static org.maxnnsu.Utils.generatePDDocumentHash;

public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream inputStream = Main.class.getResourceAsStream("/configuration.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean testMode = Boolean.parseBoolean(properties.getProperty("testMode"));
        String url;
        int hash = 0;
        ArrayList<DosarDataModel> records;
        String text = "";

        Server h2Serv;
        try {
            createDosarTables();
            h2Serv = Server.createWebServer("-webPort", "9400", "-tcpAllowOthers", "-baseDir", "./src/main/resources/db/").start();
            //
            System.out.printf("Server H2 has started: " + h2Serv.getURL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (testMode) {
            url = "src/main/resources/testData/Art.-11-2023-Redobandire.pdf";
            File file = new File(url);

            try {
                PDDocument document =  PDDocument.load(file);
                hash = generatePDDocumentHash(document);
                System.out.println("Document hash: " + hash);
                if (isPdfProcessed(hash)) {
                    System.out.println("already processed!" + url);
                    h2Serv.stop();
                    return;
                }
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            url = "http://cetatenie.just.ro/wp-content/uploads/2019/12/Art.-11-2023-Redobandire.pdf"; // Замените URL на адрес PDF-файла
            try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
                PDDocument document = PDDocument.load(inputStream);
                hash = generatePDDocumentHash(document);
                System.out.println("Document hash: " + hash);
                if (isPdfProcessed(document.hashCode())) {
                    System.out.println("already processed!" + url);
                    h2Serv.stop();
                    return;
                }
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
                document.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        parseDosarData(text).forEach(H2DatabaseManager::insertDosarData);
        if (hash != 0) {
            H2DatabaseManager.setPdfEntry(hash, new java.sql.Date(new Date().toInstant().toEpochMilli()));
        }
        h2Serv.stop();
    }

    private static ArrayList<DosarDataModel> parseDosarData(String text) {
        ArrayList<DosarDataModel> records = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (1944490191 == line.hashCode()) {
                continue;
            }
            DosarDataModel dosarDataModel = new DosarDataModel();
            // Здесь вам нужно реализовать код для разбора строки и создания экземпляра DosarDataModel.
            // В этом примере просто разделим строку по пробелам и присвоим значения полям.
            String[] parts = line.split(" ");

            if (parts.length == 2) {
                dosarDataModel.setRequestDocumentName(parts[0]);
                dosarDataModel.setRequestDate(parseData(parts[1]));
            }
            if (parts.length == 3) {
                dosarDataModel.setRequestDocumentName(parts[0]);
                dosarDataModel.setRequestDate(parseData(parts[1]));
                dosarDataModel.setOriginalReviewDate(parseData(parts[2]));
            }
            if (parts.length > 3) {//TODO: should cover existing records
                dosarDataModel.setConclusionDocumentName(parts[3]);
                dosarDataModel.setActualReviewDate(new Date());


            }
            records.add(dosarDataModel);
        }

        return records;
    }

    private static Date parseData(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date date;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            date = null;
        }
        return date;
    }

    private static boolean isPdfProcessed(int hash) {
        return H2DatabaseManager.getAllPdfEntries().stream().anyMatch(el -> hash == el.getHash());
    }
}