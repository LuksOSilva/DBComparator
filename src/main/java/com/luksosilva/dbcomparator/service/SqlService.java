package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.enums.SqlFiles;
import com.luksosilva.dbcomparator.model.enums.SqlPlaceholders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlService {


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

    public static String loadSQL(SqlFiles sqlFile) {
        try (InputStream inputStream = SqlService.class.getClassLoader().getResourceAsStream(sqlFile.fullPath())) {
            if (inputStream == null) {
                throw new RuntimeException("SQL file not found: " + sqlFile.fullPath());
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not load SQL file: " + sqlFile.fullPath(), e);
        }
    }

    protected static String buildSDWithClause(String sourceIdData, String sourceId, String tableName) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.SOURCE_ID_DATA, sourceIdData,
                SqlPlaceholders.SOURCE_ID, sourceId,
                SqlPlaceholders.TABLE_NAME, tableName
        );

        return buildSQL(SqlFiles.SD_WITH_CLAUSE, placeholders);
    }

    protected static String buildSDIdentifierColumns(String identifierColumns, String columnName) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.IDENTIFIER_COLUMNS, identifierColumns,
                SqlPlaceholders.COLUMN_NAME, columnName
        );

        return buildSQL(SqlFiles.SD_IDENTIFIER_COLUMNS, placeholders);

    }


    private static String buildSQL(SqlFiles sqlFile, Map<SqlPlaceholders, String> placeholders) {
        String sql = loadSQL(sqlFile);

        for (Map.Entry<SqlPlaceholders, String> entry : placeholders.entrySet()) {
            sql = sql.replace(entry.getKey().getPlaceholder(), entry.getValue());
        }

        return sql;
    }






}
