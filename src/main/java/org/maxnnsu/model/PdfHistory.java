package org.maxnnsu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;
import java.util.Objects;

@Data
@AllArgsConstructor
public class PdfHistory {
    int hash;
    String name;
    Date date;
    String status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PdfHistory that = (PdfHistory) o;
        return hash == that.hash && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, name);
    }
}
