package com.luksosilva.dbcomparator.model.source;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

    public List<SourceTable> getSourceTables() {
        return sourceTables;
    }

    @Override
    public String toString() {
        return "Source{path='" + path + "', tables=[" + sourceTables.stream().map(SourceTable::toString).collect(Collectors.joining("; ")) + "]}";
    }
}
