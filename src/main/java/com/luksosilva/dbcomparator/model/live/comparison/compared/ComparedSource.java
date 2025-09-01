package com.luksosilva.dbcomparator.model.live.comparison.compared;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.luksosilva.dbcomparator.model.live.source.Source;


@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "sourceId"
)
public class ComparedSource {

    private String sourceId;
    private int sequence;
    private Source source;

    public ComparedSource() {}

    public ComparedSource(String sourceId, int sequence, Source source) {
        this.sourceId = sourceId;
        this.sequence = sequence;
        this.source = source;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setSource(Source source) {
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
