package com.luksosilva.dbcomparator.viewmodel.comparison;

import com.luksosilva.dbcomparator.model.comparison.customization.Filter;
import javafx.beans.property.StringProperty;

import java.util.Optional;

public interface FilterViewModel {

    StringProperty displayValueProperty();
    Optional<StringProperty> columnNameProperty();
    Optional<StringProperty> filterTypeDescriptionProperty();
    Filter getModel();
}
