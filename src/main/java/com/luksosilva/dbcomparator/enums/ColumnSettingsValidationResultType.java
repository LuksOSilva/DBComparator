package com.luksosilva.dbcomparator.enums;

public enum ColumnSettingsValidationResultType {
    NO_IDENTIFIER(
            "As seguintes tabelas não possuem nenhum identificador selecionado",
            "Cada tabela deve ter pelo menos um campo marcado como identificador para que a comparação funcione corretamente."
    ),
    AMBIGUOUS_IDENTIFIER(
            "Os identificadores selecionados para as tabelas abaixo resultaram em múltiplos registros nas fontes",
            "A combinação de campos identificadores deve ser única para cada registro. Ajuste os identificadores para garantir unicidade."
    ),
    NOT_VALIDATED("", ""),
    VALID("", "");

    private final String message;
    private final String tip;

    ColumnSettingsValidationResultType(String message, String tip) {
        this.message = message;
        this.tip = tip;
    }

    public String getMessage() {
        return message;
    }

    public String getTip() {
        return tip;
    }
}
