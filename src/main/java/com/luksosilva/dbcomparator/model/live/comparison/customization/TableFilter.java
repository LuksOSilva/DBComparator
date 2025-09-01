package com.luksosilva.dbcomparator.model.live.comparison.customization;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;

public class TableFilter implements Filter {

    ComparedTable comparedTable;
    String userWrittenFilter;


    @Override
    public void apply() {
        comparedTable.setFilter(this);
    }



    public TableFilter(ComparedTable comparedTable, String userWrittenFilter) {
        this.comparedTable = comparedTable;
        this.userWrittenFilter = userWrittenFilter;
    }

    public ComparedTable getComparedTable() {
        return comparedTable;
    }

    public String getUserWrittenFilter() {
        return userWrittenFilter;
    }


}
