package com.luksosilva.dbcomparator.enums;

public enum FilterValidationResultType {
    INVALID_SYNTAX(
            "As seguintes tabelas estão com um filtro com sintaxe inválida",
            "Revise o código SQL."
    ),
    NO_RECORDS_FOUND(
            "As seguintes tabelas estão com um conjunto de filtros que não retorna nenhum registro",
            "Revise os filtros cadastrados para que retornem pelo menos um registro."
    ),
    NOT_VALIDATED("Filtros ainda não validados", ""),
    VALID("Filtros válidos", "");

    private final String message;
    private final String tip;

    FilterValidationResultType(String message, String tip) {
        this.message = message;
        this.tip = tip;
    }

    public String getMessage() {
        return message;
    }

    public String getTip() {
        return tip;
    }

    public boolean isValid() {
        return this == VALID;
    }
    public boolean isInvalid() {
        return this == INVALID_SYNTAX || this == NO_RECORDS_FOUND;
    }
}
