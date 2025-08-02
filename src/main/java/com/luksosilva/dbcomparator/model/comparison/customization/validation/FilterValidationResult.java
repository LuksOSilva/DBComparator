package com.luksosilva.dbcomparator.model.comparison.customization.validation;

import com.luksosilva.dbcomparator.enums.FilterValidationResultType;

public class FilterValidationResult {
    private final FilterValidationResultType type;
    private final String errorMessage;
    private final String sourceId;

    public FilterValidationResult(FilterValidationResultType type) {
        this(type, null, null);
    }

    public FilterValidationResult(FilterValidationResultType type, String sourceId) {
        this(type, sourceId, null);
    }

    public FilterValidationResult(FilterValidationResultType type, String sourceId, String errorMessage) {
        this.type = type;
        this.sourceId = sourceId;
        this.errorMessage = errorMessage;
    }

    public FilterValidationResultType getType() {
        return type;
    }

    public String getErrorMessage() {
        return errorMessage;
    }


    public String getSourceId() {
        return sourceId;
    }

    public boolean isValid() {
        return type == FilterValidationResultType.VALID;
    }

    public boolean isInvalid() {
        return type.isInvalid();
    }

}
