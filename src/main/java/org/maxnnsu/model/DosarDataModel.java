package org.maxnnsu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DosarDataModel {
    String requestDocumentName;
    Date requestDate;
    Date originalReviewDate;
    Date actualReviewDate;
    String conclusionDocumentName;
    Date created;
    Date updated;

    public DosarDataModel(ResultSet resultSet) throws SQLException {
        this.requestDocumentName = resultSet.getString("request_document_name");
        this.requestDate = resultSet.getDate("request_date");
        this.originalReviewDate = resultSet.getDate("original_review_date");
        this.conclusionDocumentName = resultSet.getString("conclusion_document_name");
        this.actualReviewDate = resultSet.getDate("actual_review_date");
        this.created = resultSet.getDate("created");
        this.updated = resultSet.getDate("updated");
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
