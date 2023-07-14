package org.maxnnsu.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Data
public class DosarDataModel {
    String requestDocumentName;
    Date requestDate;
    Date originalReviewDate;
    Date actualReviewDate;
    String conclusionDocumentName;

    public DosarDataModel(ResultSet resultSet) throws SQLException, SQLException {
        this.requestDocumentName = resultSet.getString("request_document_name");
        this.requestDate = resultSet.getDate("request_date");
        this.originalReviewDate = resultSet.getDate("original_review_date");
        this.conclusionDocumentName = resultSet.getString("conclusion_document_name");
        this.actualReviewDate = resultSet.getDate("actual_review_date");
    }

    public DosarDataModel(){

    }

    public DosarDataModel(String requestDocumentName, Date requestDate, Date originalReviewDate,
                          String conclusionDocumentName, Date actualReviewDate) {
        this.requestDocumentName = requestDocumentName;
        this.requestDate = requestDate;
        this.originalReviewDate = originalReviewDate;
        this.conclusionDocumentName = conclusionDocumentName;
        this.actualReviewDate = actualReviewDate;
    }

    public void setRequestDocumentName(String requestDocumentName) {
        this.requestDocumentName = requestDocumentName;
    }

    public String getRequestDocumentName() {
        return requestDocumentName;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getOriginalReviewDate() {
        return originalReviewDate;
    }

    public void setOriginalReviewDate(Date originalReviewDate) {
        this.originalReviewDate = originalReviewDate;
    }

    public Date getActualReviewDate() {
        return actualReviewDate;
    }

    public void setActualReviewDate(Date actualReviewDate) {
        this.actualReviewDate = actualReviewDate;
    }

    public String getConclusionDocumentName() {
        return conclusionDocumentName;
    }

    public void setConclusionDocumentName(String conclusionDocumentName) {
        this.conclusionDocumentName = conclusionDocumentName;
    }

    public int getRecordSize(){

        int result = 0;
        if (StringUtils.isNotEmpty(String.valueOf(actualReviewDate)) && !"null".equals(String.valueOf(actualReviewDate))){
            result = 5;
        }
        if (StringUtils.isNotEmpty(String.valueOf(originalReviewDate)) && !"null".equals(String.valueOf(actualReviewDate))){
            result = 3;
        }
        if(StringUtils.isNotEmpty(requestDocumentName)){
            result = 2;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestDocumentName, requestDate, originalReviewDate, conclusionDocumentName, actualReviewDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DosarDataModel other = (DosarDataModel) obj;
        return Objects.equals(requestDocumentName, other.requestDocumentName) &&
                Objects.equals(requestDate, other.requestDate) &&
                Objects.equals(originalReviewDate, other.originalReviewDate) &&
                Objects.equals(conclusionDocumentName, other.conclusionDocumentName) &&
                Objects.equals(actualReviewDate, other.actualReviewDate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("requestDocumentName=").append(requestDocumentName != null ? requestDocumentName : "null").append(", ");
        sb.append("requestDate=").append(requestDate != null ? requestDate.toString() : "null").append(", ");
        sb.append("originalReviewDate=").append(originalReviewDate != null ? originalReviewDate.toString() : "null").append(", ");
        sb.append("conclusionDocumentName=").append(conclusionDocumentName != null ? conclusionDocumentName : "null").append(", ");
        sb.append("actualReviewDate=").append(actualReviewDate != null ? actualReviewDate.toString() : "null");
        return sb.toString();
    }
}
