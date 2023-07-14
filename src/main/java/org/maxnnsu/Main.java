package org.maxnnsu;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.h2.tools.Server;
import org.maxnnsu.model.DosarDataModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

import static org.maxnnsu.H2DatabaseManager.createDosarTables;
import static org.maxnnsu.Utils.generatePDDocumentHash;
import static org.maxnnsu.Utils.loadAppProperties;

public class Main {

    public static final String REMOTE_URL = "http://cetatenie.just.ro/wp-content/uploads/2019/12/Art.-11-2023-Redobandire.pdf";
    private static Server h2Serv = null;
    private static int hash = 0;

    public static void main(String[] args) {
        Properties properties = loadAppProperties();
        boolean testMode = Boolean.parseBoolean(properties.getProperty("testMode"));

        String url;
        String text = "";
        startH2Server(properties);

        if (!testMode) {
            url = REMOTE_URL;
            try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
                PDDocument document = PDDocument.load(inputStream);
                hash = generatePDDocumentHash(document);
                System.out.println("Document hash: " + hash + "\n");
                if (isPdfProcessed(document.hashCode())) {
                    System.out.println("already processed!\n" + url);
                    shutdownH2Server();
                    return;
                }
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
                document.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (testMode) {
            text = processTestData();
        }

        if (StringUtils.isEmpty(text)) {
            shutdownH2Server();
            return;
        }
        TimerUtil timer = new TimerUtil();
        parseDosarData(text).forEach(H2DatabaseManager::insertDosarData);
        System.out.println("Data processing has been finished. Elapsed time: " + timer.stop());

        if (hash != 0) {
            H2DatabaseManager.setPdfEntry(hash, new java.sql.Date(new Date().toInstant().toEpochMilli()));
        }
        String webServerLifeTime = properties.getProperty("h2.keepwebserverMin", "./src/main/resources/db/");
        if(Objects.nonNull(webServerLifeTime) && Integer.parseInt(webServerLifeTime) > 0){
            try {
                System.out.println("H2 Web Server will be available next " + webServerLifeTime + "min(s)");
                long timeMins = Long.parseLong(webServerLifeTime);
                Thread.sleep( timeMins * 60000);
                System.out.println("H2 Web Server will be shut down now!");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        shutdownH2Server();
    }

    private static ArrayList<DosarDataModel> parseDosarData(String text) {
        ArrayList<DosarDataModel> records = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (1944490191 == line.hashCode()) {
                continue;
            }
            DosarDataModel dosarDataModel = new DosarDataModel();

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

    private static String processTestData() {
        String url = "src/main/resources/testData/Art.-11-2023-Redobandire.pdf";
        File file = new File(url);
        try {
            PDDocument document = PDDocument.load(file);
            hash = generatePDDocumentHash(document);
            System.out.println("Document hash: " + hash);
            if (isPdfProcessed(hash)) {
                System.out.println("already processed!\n" + url);
                return "";
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void startH2Server(Properties properties) {
        String h2Port = properties.getProperty("h2.port", "9400");
        String h2Path = properties.getProperty("h2.path", "./src/main/resources/db/");

        try {
            createDosarTables();
            h2Serv = Server.createWebServer(
                            "-webPort", h2Port,
                            "-baseDir", h2Path)
                    .start();

            System.out.printf("Server H2 has started: " + h2Serv.getURL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void shutdownH2Server() {
        if (Objects.nonNull(h2Serv)) {
            h2Serv.stop();
        }
    }
}