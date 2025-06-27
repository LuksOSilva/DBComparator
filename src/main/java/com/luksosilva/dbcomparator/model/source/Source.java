package com.luksosilva.dbcomparator.model.source;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Source {

    private File path;
    private List<SourceTable> sourceTables = new ArrayList<>();

    public Source(File path) {
        this.path = path;
    }

    public File getPath() {
        return path;
    }

    @JsonIgnore
    public List<SourceTable> getSourceTables() {
        return sourceTables;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Source other = (Source) obj;

        if (this.path == null || other.path == null) return false;

        return path.equals(other.path)
                && path.length() == other.path.length()
                && path.lastModified() == other.path.lastModified();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                path != null ? path.getAbsolutePath() : null,
                path != null ? path.length() : 0,
                path != null ? path.lastModified() : 0
        );
    }


}
