package org.maxnnsu;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.h2.tools.Server;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.maxnnsu.dosarWebPageProcessor.DosarStadiuProcessorImpl;
import org.maxnnsu.dosarWebPageProcessor.DosarWebPageProcessorService;
import org.maxnnsu.model.DosarDataModel;
import org.maxnnsu.model.PdfHistory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.maxnnsu.H2DatabaseManager.createDosarTables;
import static org.maxnnsu.Utils.loadAppProperties;

public class Main {
    //There are two types of PDFs. with a list of all and with results of particular commissions
    public static final String STADIU_DOSAR = "https://cetatenie.just.ro/stadiu-dosar/";

    //not handled yet
    public static final String DOSAR_ORDINE_ARTICOLUL_11 = "https://cetatenie.just.ro/ordine-articolul-11/";

    private static Server h2Serv = null;
    private static final int hash = 0;

    public static void main(String[] args) {
        Properties properties = loadAppProperties();
        boolean testMode = Boolean.parseBoolean(properties.getProperty("testMode"));

        String url;
        String text = "";
        startH2Server(properties);

        List<PdfHistory> urlsToProcess = new ArrayList<>();
        if (!testMode) {
            url = STADIU_DOSAR;
            Document pdfPage;
            try {
                pdfPage = Jsoup.connect(url).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            DosarWebPageProcessorService pageProcessor = new DosarStadiuProcessorImpl();
            List<String> urls = pageProcessor.getPdfUrlsToProcess(pdfPage);
            urlsToProcess = urls.stream()
                    .map(Main::verifyPdfHashIsNotInDb)
                    .filter(Objects::nonNull)
                    .toList();

        }
        if (testMode) {
            urlsToProcess = processTestData();
        }

        for (PdfHistory pdfHistory : urlsToProcess) {
            String pdfUrl = pdfHistory.getName();

            try (BufferedInputStream inputStream = new BufferedInputStream(new URL(pdfUrl).openStream())) {
                PDDocument document = PDDocument.load(inputStream);

                System.out.println("Document hash: " + hash + "\n");
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
                document.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


            if (StringUtils.isNotEmpty(text)) {
                TimerUtil timer = new TimerUtil();
                System.out.println("Data processing has been started. " + pdfHistory.getName());
                parseDosarData(text).forEach(H2DatabaseManager::insertDosarData);
                H2DatabaseManager.setPdfEntry(pdfHistory.getHash(), pdfHistory.getName(), new Date(System.currentTimeMillis()), PdfStatus.PROCESSED.name());
                System.out.println("Data processing has been finished. Elapsed time: " + timer.stop());
            }
        }

        checkDosarNumberInDB(properties);

        keepH2RunningForConfiguredTime(properties);

        shutdownH2Server();
    }

    private static void checkDosarNumberInDB(Properties properties) {
        String dosarRequestNumber = properties.getProperty("dosarNumber", "");
        if (StringUtils.isNotEmpty(dosarRequestNumber)) {
            DosarDataModel requestedModel = H2DatabaseManager.selectDosarDataByRequestDocumentName(dosarRequestNumber);
            if (Objects.nonNull(requestedModel)) {
                System.out.println("Your request : " + requestedModel);
            } else {
                System.out.println("Your request DOSAR number not found. Maybe it is not listed yet.");

            }
        }
    }

    private static PdfHistory verifyPdfHashIsNotInDb(String stringUrl) {
        //creates hash sum of the bytes amount of the file without downloading it
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int fileSize = connection.getContentLength();
            System.out.println("File Size: " + fileSize + " bytes");
            PdfHistory pdfUnderCheck = new PdfHistory(Objects.hash(fileSize), stringUrl, new Date(System.currentTimeMillis()), PdfStatus.NEW.name());
            List<PdfHistory> allPdfEntries = H2DatabaseManager.getAllPdfEntries();
            if (allPdfEntries.contains(pdfUnderCheck)) {
                System.out.println("PDF file " + pdfUnderCheck + " is already processed");
                return null;

            }
            return pdfUnderCheck;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<DosarDataModel> parseDosarData(String text) {
        ArrayList<DosarDataModel> records = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (1944490191 == line.hashCode() || (line.contains("DATĂ") || line.contains("TERMEN") || line.contains("SOLUȚIE") || line.contains("NUMĂR"))) {
                continue;
            }
            line = line.strip();
            String[] parts = line.split(" ");
            //todo: solve problems with "1 /RD/2012" , "217/P/19.04.2013" "217/P/ 19.04.2013"
            // check log with other error inputs
            if (parts[1].startsWith("/")) {
                parts[0] = parts[0] + parts[1]; //  "1 /RD/2012"
                parts[1] = parts[2];
                if (parts.length > 4) {
                    parts[2] = parts[3];
                }
                if (parts.length > 5) {
                    parts[3] = parts[4];
                }
            }
            if (parts.length > 3 && parts[3].startsWith("/")) {
                parts[2] = parts[2] + parts[3]; //  217/P/19.04.2013
                parts[3] = parts[3].substring(1); // /19.04.2013
            }

            DosarDataModel dosarDataModel = new DosarDataModel();

            if (parts.length == 2) {
                dosarDataModel.setRequestDocumentName(parts[0]);
                Date date = parseData(parts[1], 1);
                if (Objects.isNull(date)) {
                    System.out.println("error parsing data: " + Arrays.toString(parts));
                } else {
                    dosarDataModel.setRequestDate(date);
                }
            }
            if (parts.length == 3) {
                dosarDataModel.setRequestDocumentName(parts[0]);
                Date date = parseData(parts[1], 1);
                if (Objects.isNull(date)) {
                    System.out.println("error parsing data: " + Arrays.toString(parts));
                    continue;
                } else {
                    dosarDataModel.setRequestDate(date);
                }

                if (StringUtils.contains(parts[2], "/P/")) {
                    int index = parts[2].indexOf("/P/") + 3;
                    dosarDataModel.setActualReviewDate(parseData(parts[2].substring(index), 1));
                    dosarDataModel.setConclusionDocumentName(parts[2]);
                }
                if (Objects.nonNull(parseData(parts[2], 0))) {
                    dosarDataModel.setOriginalReviewDate(parseData(parts[2], 1));
                }

                dosarDataModel.setConclusionDocumentName(parts[2]);
            }
            if (parts.length > 3) {
                dosarDataModel.setRequestDocumentName(parts[0]);
                dosarDataModel.setRequestDate(parseData(parts[1], 1));
                if (StringUtils.contains(parts[2], "/P/")) {
                    dosarDataModel.setConclusionDocumentName(parts[2]);
                }
                if (parts.length == 4 && StringUtils.contains(parts[3], "/P/")) {
                    dosarDataModel.setConclusionDocumentName(parts[3]);
                }
                if (parts.length == 5 && Objects.nonNull(parseData(parts[2], 0))) {
                    dosarDataModel.setOriginalReviewDate(parseData(parts[2], 0));
                }
                if (parts.length == 5 && StringUtils.contains(parts[4], "/P/")) {
                    dosarDataModel.setConclusionDocumentName(parts[4]);
                }
                if (parts.length == 4 && Objects.nonNull(parseData(parts[3], 0))) {
                    dosarDataModel.setActualReviewDate(parseData(parts[3], 1));
                }
                if (parts.length == 5 && Objects.nonNull(parseData(parts[4], 0))) {
                    dosarDataModel.setActualReviewDate(parseData(parts[4], 1));
                }
            }
            records.add(dosarDataModel);
        }

        return records;
    }

    private static Date parseData(String dateString, int logging) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return new Date(format.parse(dateString).getTime());
        } catch (ParseException e) {
            if (logging == 1) {
                System.out.println(" dateString = " + dateString);
            }
        }
        return null;
    }

    private static boolean isPdfProcessed(int hash) {
        return H2DatabaseManager.getAllPdfEntries().stream().anyMatch(el -> hash == el.getHash());
    }

    private static List<PdfHistory> processTestData() {//TODO: adjust logic to the new structure
        String url = "src/main/resources/testData/Art.-11-2023-Redobandire.pdf";
        PdfHistory record = new PdfHistory(Objects.hash(url), url, new Date(System.currentTimeMillis()), PdfStatus.NEW.name());
        return List.of(record);
//        try {
//            PDDocument document = PDDocument.load(file);
//            hash = generatePDDocumentHash(document);
//            System.out.println("Document hash: " + hash);
//            if (isPdfProcessed(hash)) {
//                System.out.println("already processed!\n" + url);
//                document.close();
//                return "";
//            }
//            PDFTextStripper stripper = new PDFTextStripper();
//            String text = stripper.getText(document);
//            document.close();
//            return text;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return "";
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

    private static void keepH2RunningForConfiguredTime(Properties properties) {
        String webServerLifeTime = properties.getProperty("h2.keepwebserverMin", "./src/main/resources/db/");
        if (Objects.nonNull(webServerLifeTime) && Integer.parseInt(webServerLifeTime) > 0) {
            try {
                System.out.println("H2 Web Server will be available next " + webServerLifeTime + "min(s)");
                long timeMins = Long.parseLong(webServerLifeTime);
                Thread.sleep(timeMins * 60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void shutdownH2Server() {
        if (Objects.nonNull(h2Serv)) {
            System.out.println("H2 Web Server will be shutdown now!");
            h2Serv.stop();
        }
    }
}