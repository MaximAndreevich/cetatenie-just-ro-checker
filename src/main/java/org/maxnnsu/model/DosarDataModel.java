package org.maxnnsu.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Data
public class DosarDataModel {
    String requestDocumentName;
    Date requestDate;
    Date originalReviewDate;
    Date actualReviewDate;
    String conclusionDocumentName;

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
}
