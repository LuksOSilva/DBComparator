package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.enums.SqlPlaceholders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlFormatter {

    public static String buildSelectValidateFilter(String sourceId, String tableName, String filterSql) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.TABLE_NAME, tableName,
                SqlPlaceholders.FILTER_SQL, filterSql
        );

        return buildSQL(SqlFiles.SELECT_VALIDATE_FILTERS, placeholders);
    }

    public static String buildSelectValidateIdentifiers(String sourceId, String tableName, List<String> identifierColumns) {

        String quotedIdentifierColumns = identifierColumns.stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(", "));

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.TABLE_NAME, tableName,
                SqlPlaceholders.IDENTIFIER_COLUMNS, quotedIdentifierColumns
        );

        return buildSQL(SqlFiles.SELECT_VALIDATE_IDENTIFIERS, placeholders);
    }


    public static String buildReplaceColumnSettings(String tableName, String columnName, boolean isComparable, boolean isIdentifier) {

        //converts boolean to string
        String strIsComparable = isComparable ? "Y" : "N";
        String strIsIdentifier = isIdentifier ? "Y" : "N";

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.TABLE_NAME, tableName,
                SqlPlaceholders.COLUMN_NAME, columnName,
                SqlPlaceholders.IS_COMPARABLE, strIsComparable,
                SqlPlaceholders.IS_IDENTIFIER, strIsIdentifier
        );

        return buildSQL(SqlFiles.REPLACE_COLUMN_SETTINGS, placeholders);

    }

    public static String buildSelectMapColumnSettings(String listTableNames) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.LIST_TABLE_NAMES, listTableNames
        );

        return buildSQL(SqlFiles.SELECT_MAP_COLUMN_SETTINGS, placeholders);
    }

    public static String buildPragmaTableInfo(String sourceId, String tableName) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.TABLE_NAME, tableName
        );

        return buildSQL(SqlFiles.PRAGMA_TABLE_INFO, placeholders);
    }

    public static String buildSelectTableRecordCount(String sourceId, String tableName) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.TABLE_NAME, tableName
        );

        return buildSQL(SqlFiles.SELECT_TABLE_RECORD_COUNT, placeholders);
    }

    public static String buildPragmaTableList(String sourceId) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID, sourceId
        );

        return buildSQL(SqlFiles.PRAGMA_TABLE_LIST, placeholders);
    }


    public static String buildSelectDiff(String withClause, String selectClause, String fromClause, String whereClause) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.WITH_CLAUSE, withClause,
                SqlPlaceholders.SELECT_CLAUSE, selectClause,
                SqlPlaceholders.FROM_CLAUSE, fromClause,
                SqlPlaceholders.WHERE_CLAUSE, whereClause
        );

        return buildSQL(SqlFiles.SELECT_DIFFERENCES, placeholders);

    }




    public static String buildSQL(SqlFiles sqlFile, Map<SqlPlaceholders, String> placeholders) {
        String sql = SQLiteUtils.loadSQL(sqlFile);

        for (Map.Entry<SqlPlaceholders, String> entry : placeholders.entrySet()) {
            sql = sql.replace(entry.getKey().getPlaceholder(), entry.getValue());
        }

        return sql;
    }






}
