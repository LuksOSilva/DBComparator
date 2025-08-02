package com.luksosilva.dbcomparator.model.comparison.customization;

import com.luksosilva.dbcomparator.model.comparison.compared.ComparedTable;

public interface Filter {
    void apply();
    ComparedTable getComparedTable();
}
