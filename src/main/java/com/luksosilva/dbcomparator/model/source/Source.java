package com.luksosilva.dbcomparator.model.source;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @JsonIgnore
    public List<SourceTable> getSourceTables() {
        return sourceTables;
    }


}
