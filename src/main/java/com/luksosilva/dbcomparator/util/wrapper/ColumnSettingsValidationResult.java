//package com.luksosilva.dbcomparator.util.wrapper;
//
//import com.luksosilva.dbcomparator.enums.ColumnSettingsValidationResultType;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class ColumnSettingsValidationResult {
//    private final Map<ColumnSettingsValidationResultType, String> result = new HashMap<>();
//
//
//    public void addResult(ColumnSettingsValidationResultType type, String text) {
//        result.put(type, text);
//    }
//
//    public boolean isValid() {
//        return result.containsKey(ColumnSettingsValidationResultType.VALID);
//    }
//
//    public Map<ColumnSettingsValidationResultType, String> getResult() {
//        return result;
//    }
//}
