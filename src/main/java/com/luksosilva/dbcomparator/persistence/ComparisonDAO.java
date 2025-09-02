package com.luksosilva.dbcomparator.persistence;

import com.luksosilva.dbcomparator.model.persistence.SavedComparison;
import com.luksosilva.dbcomparator.util.SQLiteUtils;
import com.luksosilva.dbcomparator.util.SqlFormatter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ComparisonDAO {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void saveCreatedComparison(File file) throws Exception {
        boolean isLoaded = false;
        saveComparison(file, isLoaded);
    }

    public static void saveLoadedComparison(File file) throws Exception {
        boolean isLoaded = true;
        saveComparison(file, isLoaded);
    }


    public static List<SavedComparison> loadAllComparisons() throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = "SELECT * FROM DBC_COMPARISONS";


            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                List<SavedComparison> list = new ArrayList<>();
                while (rs.next()) {

                    boolean isImported = rs.getString("IS_IMPORTED").equals("Y");

                    String lastLoadedAtStr = rs.getString("LAST_LOADED_AT");

                    list.add(new SavedComparison(
                            rs.getInt("COMPARISON_ID"),
                            rs.getString("DESCRIPTION"),
                            new File(rs.getString("FILE_PATH")),
                            isImported,
                            LocalDateTime.parse(rs.getString("CREATED_AT"), DATE_TIME_FORMATTER),
                            lastLoadedAtStr == null ? null : LocalDateTime.parse(rs.getString("LAST_LOADED_AT"), DATE_TIME_FORMATTER)
                    ));
                }
                return list;
            }
        }
    }


    private static void saveComparison(File file, boolean isLoaded) throws Exception {
        if (isLoaded && isComparisonSaved(file)) {
            alterLastLoadedTime(file);
            return;
        }


        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {


            String description = FilenameUtils.removeExtension(file.getName());
            String isImportedStr = isLoaded ? "Y" : "N";
            String createdAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);

            String sql = SqlFormatter.buildInsertDBCComparisons(
                    description,
                    file.getCanonicalPath(),
                    isImportedStr,
                    createdAt);

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    private static void alterLastLoadedTime(File file) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String lastLoadedAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);

            String sql = SqlFormatter.buildUpdateLastLoaded(
                    lastLoadedAt,
                    file.getCanonicalPath());

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }

        }
    }

    private static boolean isComparisonSaved(File file) throws Exception {
        try (Connection connection = SQLiteUtils.getDataSource().getConnection()) {

            String sql = SqlFormatter.buildSelectDBCComparison(file.getCanonicalPath());

            try (Statement stmt = connection.createStatement();
                 ResultSet resultSet = stmt.executeQuery(sql)
            ) {
                if (resultSet.next()) return true;
            }

            return false;
        }
    }

}
