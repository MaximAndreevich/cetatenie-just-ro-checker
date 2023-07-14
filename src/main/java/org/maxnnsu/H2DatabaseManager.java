package org.maxnnsu;
import org.h2.tools.Server;
import org.maxnnsu.model.DosarDataModel;
import org.maxnnsu.model.PdfHistory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class H2DatabaseManager {
    //private static final String DB_URL = "jdbc:h2:./db/dosardata.db";
    private static final String DB_INIT_URL = "jdbc:h2:./src/main/resources/db/dosardata.db";
    private static final String DB_URL = "jdbc:h2:./dosardata.db";
    private static final String DB_USERNAME = "логин";
    private static final String DB_PASSWORD = "пароль";

    public static void createDosarTables() {
        try (Connection connection = DriverManager.getConnection(DB_INIT_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS dosar_data (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY," +
                             "request_document_name VARCHAR(255)," +
                             "request_date VARCHAR(255)," +
                             "original_review_date VARCHAR(255)," +
                             "conclusion_document_name VARCHAR(255)," +
                             "actual_review_date VARCHAR(255)" +
                             ")"
             );
             PreparedStatement statement2 = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS pdf_history (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY," +
                             "hash INTEGER," +
                             "date DATE" +
                             ")"
             )
             ) {
            statement.executeUpdate();
            statement2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertDosarData(DosarDataModel dosarDataModel) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO dosar_data " +
                             "(request_document_name, request_date, original_review_date, " +
                             "conclusion_document_name, actual_review_date) " +
                             "VALUES (?, ?, ?, ?, ?)"
             )) {
            switch (dosarDataModel.getRecordSize()) {
                case 2 -> {
                    statement.setString(1, dosarDataModel.getRequestDocumentName());
                    statement.setString(2, dosarDataModel.getRequestDate().toString());
                    statement.setString(3, null);
                    statement.setString(4, null);
                    statement.setString(5, null);
                }
                case 3 -> {
                    statement.setString(1, dosarDataModel.getRequestDocumentName());
                    statement.setString(2, dosarDataModel.getRequestDate().toString());
                    statement.setString(3, dosarDataModel.getOriginalReviewDate().toString());
                    statement.setString(4, null);
                    statement.setString(5, null);
                }
                case 5 -> {
                    statement.setString(1, dosarDataModel.getRequestDocumentName());
                    statement.setString(2, dosarDataModel.getRequestDate().toString());
                    statement.setString(3, dosarDataModel.getOriginalReviewDate().toString());
                    statement.setString(4, dosarDataModel.getConclusionDocumentName());
                    statement.setString(5, dosarDataModel.getActualReviewDate().toString());
                }
                default -> {
                }
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setPdfEntry(int hash, Date date) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO pdf_history (hash, date) VALUES (?, ?)"
        )) {
            statement.setInt(1, hash);
            statement.setDate(2, (java.sql.Date) date);
            statement.executeUpdate();
        }catch (SQLException e){

    }
    }

    public static List<PdfHistory> getAllPdfEntries() {
        List<PdfHistory> entries = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL);
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM pdf_history"
        )){
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int hash = resultSet.getInt("hash");
            Date date = resultSet.getDate("date");
            PdfHistory entry = new PdfHistory(hash, date);
            entries.add(entry);
        }
        }catch (SQLException e){

        }

        return entries;
    }

    private static Date parseData(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date date;
        try {
            date = format.parse(dateString);
        }catch (ParseException e){
            date = null;
        }
        return date;
    }
}
