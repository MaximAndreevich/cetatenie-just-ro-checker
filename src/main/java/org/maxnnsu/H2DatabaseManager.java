package org.maxnnsu;

import org.maxnnsu.model.DosarDataModel;
import org.maxnnsu.model.PdfHistory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class H2DatabaseManager {
    private static final String DB_INIT_URL = "jdbc:h2:./src/main/resources/db/dosardata.db";
    private static final String DB_URL = "jdbc:h2:./dosardata.db";

    public static void createDosarTables() {
        try (Connection connection = DriverManager.getConnection(DB_INIT_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS dosar_data (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY," +
                             "request_document_name VARCHAR(255) UNIQUE," +
                             "request_date DATE," +
                             "original_review_date DATE," +
                             "conclusion_document_name VARCHAR(255)," +
                             "actual_review_date DATE," +
                             "created DATE," +
                             "updated DATE" +
                             ")"
             );
             PreparedStatement statement2 = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS pdf_history (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY," +
                             "hash INTEGER," +
                             "name VARCHAR(255)," +
                             "date DATE," +
                             "status VARCHAR(255)" +
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
                             "conclusion_document_name, actual_review_date, created, updated) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)"
             )) {

            statement.setString(1, dosarDataModel.getRequestDocumentName());
            statement.setDate(2, dosarDataModel.getRequestDate());
            statement.setDate(3, dosarDataModel.getOriginalReviewDate());
            statement.setString(4, dosarDataModel.getConclusionDocumentName());
            statement.setDate(5, dosarDataModel.getActualReviewDate());
            statement.setDate(6, getCurrentDate());
            statement.setDate(7, getCurrentDate());

            statement.executeUpdate();
        } catch (SQLException e) {
            if (isDuplicateEntryException(e)) {
                updateDosarData(dosarDataModel);
                return;
            }
            if (isDBLocked(e)) {
                System.exit(1);
            }
            e.printStackTrace();
        }
    }

    public static void setPdfEntry(int hash, String name, Date date, String status) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO pdf_history (hash,name, date, status) VALUES (?, ?, ?, ?)"
             )) {
            statement.setInt(1, hash);
            statement.setString(2, name);
            statement.setDate(3, date);
            statement.setString(4, status);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<PdfHistory> getAllPdfEntries() {
        List<PdfHistory> entries = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM pdf_history"
             )) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int hash = resultSet.getInt("hash");
                Date date = resultSet.getDate("date");
                //TODO: handle table data
                PdfHistory entry = new PdfHistory(hash, resultSet.getString("name"), date, resultSet.getString("status"));
                entries.add(entry);
            }
        } catch (SQLException e) {

        }

        return entries;
    }

    public static DosarDataModel selectDosarDataByRequestDocumentName(String requestDocumentName) {
        DosarDataModel dosarDataModel = null;
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement selectStatement = connection.prepareStatement(
                     "SELECT * FROM dosar_data WHERE request_document_name = ?"
             )) {
            selectStatement.setString(1, requestDocumentName);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                dosarDataModel = new DosarDataModel(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dosarDataModel;
    }

    private static boolean isDuplicateEntryException(SQLException e) {
        return e.getErrorCode() == 23505;
    }

    private static boolean isDBLocked(SQLException e) {
        return e.getErrorCode() == 90020;
    }

    private static void updateDosarData(DosarDataModel dosarDataModel) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement selectStatement = connection.prepareStatement(
                     "SELECT * FROM dosar_data WHERE request_document_name = ?"
             );
             PreparedStatement updateStatement = connection.prepareStatement(
                     "UPDATE dosar_data SET actual_review_date = ?, original_review_date = ?, conclusion_document_name = ?, updated = ? WHERE request_document_name = ?"
             )) {
            selectStatement.setString(1, dosarDataModel.getRequestDocumentName());
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                DosarDataModel existingDosarDataModel = new DosarDataModel(resultSet);
                if (!existingDosarDataModel.equals(dosarDataModel)) {
                    updateStatement.setDate(1, dosarDataModel.getActualReviewDate());
                    updateStatement.setDate(2, dosarDataModel.getOriginalReviewDate());
                    updateStatement.setString(3, dosarDataModel.getConclusionDocumentName());
                    updateStatement.setDate(4, getCurrentDate());
                    updateStatement.setString(5, dosarDataModel.getRequestDocumentName());
                    updateStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static DosarDataModel deleteDosarDataByRequestDocumentName(String requestDocumentName) {
        DosarDataModel dosarDataModel = null;
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement selectStatement = connection.prepareStatement(
                     "delete FROM dosar_data WHERE request_document_name = ?"
             )) {
            selectStatement.setString(1, requestDocumentName);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                dosarDataModel = new DosarDataModel(resultSet);
                System.out.println(" record " + dosarDataModel.getConclusionDocumentName() + " removed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dosarDataModel;
    }

    public static DosarDataModel updateDosarDataByRequestDocumentName(String requestDocumentName) {
        DosarDataModel dosarDataModel = null;
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement selectStatement = connection.prepareStatement(
                     "delete FROM dosar_data WHERE request_document_name = ?"
             )) {
            selectStatement.setString(1, requestDocumentName);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                dosarDataModel = new DosarDataModel(resultSet);
                System.out.println(" record " + dosarDataModel.getConclusionDocumentName() + " removed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dosarDataModel;
    }

    private static Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }
}
