package com.luksosilva.dbcomparator.model.comparison.compared;


import com.luksosilva.dbcomparator.model.source.Source;

public class ComparedSource {

    private String sourceId;
    private int sequence;
    private Source source;

    public ComparedSource(String sourceId, int sequence, Source source) {
        this.sourceId = sourceId;
        this.sequence = sequence;
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public int getSequence() {
        return sequence;
    }

    public Source getSource() {
        return source;
    }

    

}
