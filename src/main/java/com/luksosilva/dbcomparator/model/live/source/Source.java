package com.luksosilva.dbcomparator.model.live.source;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Source {

    private String id;
    private int sequence;
    private File file;

    private List<SourceTable> sourceTables = new ArrayList<>();

    public Source() {}

    public Source(String id, int sequence, File file) {
        this.id = id;
        this.sequence = sequence;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getId() {
        return id;
    }

    public int getSequence() {
        return sequence;
    }

    @JsonIgnore
    public List<SourceTable> getSourceTables() {
        return sourceTables;
    }

    public void setFile(File file) {
        this.file = file;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Source other = (Source) obj;

        if (this.file == null || other.file == null) return false;

        return file.equals(other.file)
                && file.length() == other.file.length()
                && file.lastModified() == other.file.lastModified();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                file != null ? file.getAbsolutePath() : null,
                file != null ? file.length() : 0,
                file != null ? file.lastModified() : 0
        );
    }


}
