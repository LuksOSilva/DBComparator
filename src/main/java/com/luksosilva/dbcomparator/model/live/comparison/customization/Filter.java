package com.luksosilva.dbcomparator.model.live.comparison.customization;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;

public interface Filter {
    void apply();
    ComparedTable getComparedTable();
}
