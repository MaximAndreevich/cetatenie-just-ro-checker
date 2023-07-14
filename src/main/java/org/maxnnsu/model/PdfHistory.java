package org.maxnnsu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class PdfHistory {
    int hash;
    Date date;
}
