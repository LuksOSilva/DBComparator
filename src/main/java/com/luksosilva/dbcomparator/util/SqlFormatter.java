package com.luksosilva.dbcomparator.util;

import com.luksosilva.dbcomparator.enums.SqlFiles;
import com.luksosilva.dbcomparator.enums.SqlPlaceholders;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlFormatter {

    public static String buildUpdateDBCComparisonConfig(String configKey, String configValue) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.CONFIG_KEY, configKey,
                SqlPlaceholders.CONFIG_VALUE, configValue
        );

        return buildSQL(SqlFiles.UPDATE_DBC_CONFIG, placeholders);

    }

    public static String buildDeleteDBCComparison(String comparisonId) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.COMPARISON_ID, comparisonId
        );

        return buildSQL(SqlFiles.DELETE_DBC_COMPARISON, placeholders);

    }

    public static String buildUpdateLastLoaded(String lastLoadedAt, String filePath) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.LAST_LOADED_AT, lastLoadedAt,
                SqlPlaceholders.FILE_PATH, filePath
        );

        return buildSQL(SqlFiles.UPDATE_LAST_LOADED0, placeholders);

    }

    public static String buildSelectDBCComparison(String filePath) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.FILE_PATH, filePath
        );

        return buildSQL(SqlFiles.SELECT_DBC_COMPARISON, placeholders);

    }

    public static String buildInsertDBCComparisons(String description, String filePath, String isImported, String createdAt) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.DESCRIPTION, description,
                SqlPlaceholders.FILE_PATH, filePath,
                SqlPlaceholders.IS_IMPORTED, isImported,
                SqlPlaceholders.CREATED_AT, createdAt
        );

        return buildSQL(SqlFiles.INSERT_DBC_COMPARISONS, placeholders);

    }

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

        return buildSQL(SqlFiles.REPLACE_COLUMN_DEFAULTS, placeholders);

    }

    public static String buildSelectMapColumnSettings(String listTableNames) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.LIST_TABLE_NAMES, listTableNames
        );

        return buildSQL(SqlFiles.SELECT_COLUMN_DEFAULTS, placeholders);
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

    public static String buildSelectDifferences(String withClause, String selectClause, String groupByClause, String havingClause) {

        Map<SqlPlaceholders, String> placeholders = Map.of(
                SqlPlaceholders.WITH_CLAUSE, withClause,
                SqlPlaceholders.SELECT_CLAUSE, selectClause,
                SqlPlaceholders.GROUP_BY_CLAUSE, groupByClause,
                SqlPlaceholders.HAVING_CLAUSE, havingClause
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
