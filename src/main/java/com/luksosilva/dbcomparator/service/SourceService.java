package com.luksosilva.dbcomparator.service;

import com.luksosilva.dbcomparator.model.live.comparison.compared.ComparedTable;
import com.luksosilva.dbcomparator.model.live.comparison.config.ConfigRegistry;
import com.luksosilva.dbcomparator.model.live.source.Source;
import com.luksosilva.dbcomparator.model.live.source.SourceTable;
import com.luksosilva.dbcomparator.model.live.source.SourceTableColumn;
import com.luksosilva.dbcomparator.persistence.SourceLoader;
import com.luksosilva.dbcomparator.persistence.temp.TempSourcesDAO;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SourceService {

    public static void processSources(List<File> fileList, ConfigRegistry configRegistry) throws Exception {
        try {
            List<File> fileSourcesOnDb = getSourcesFiles();
            List<File> toRemoveList = getDifferenceList(fileSourcesOnDb, fileList);
            List<File> toAddList = getDifferenceList(fileList, fileSourcesOnDb);

            List<Source> sourcesToAdd = mapFilesToSources(toAddList);
            List<Source> sourcesToRemove = mapFilesToSources(toRemoveList);

            mapSources(sourcesToAdd, configRegistry);

            if (!sourcesToRemove.isEmpty()) {
                TempSourcesDAO.deleteTempSources(sourcesToRemove);
                ComparedTableService.clearTempTables();
            }

            if (!sourcesToAdd.isEmpty()) {
                TempSourcesDAO.saveTempSources(sourcesToAdd);
            }
        } catch (Exception e) {
            throw new Exception("Não foi possível processar as fontes de dados: " + e);
        }
    }

    public static List<Source> getSources() throws Exception {
        try {

            return TempSourcesDAO.selectSources();

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as fontes: " + e.getMessage());
        }
    }

    public static void getColumnsOfTables(List<SourceTable> sourceTableList) throws Exception {
        try {

            List<SourceTableColumn> sourceTableColumns = TempSourcesDAO.selectSourceColumns();

            for (SourceTable sourceTable : sourceTableList) {
                List<SourceTableColumn> columnsOfTable = sourceTableColumns.stream()
                        .filter(column -> column.getTableName().equals(sourceTable.getTableName()))
                        .filter(column -> column.getSourceId().equals(sourceTable.getSourceId()))
                        .toList();

                sourceTable.setSourceTableColumns(columnsOfTable);
            }

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas das fontes: " + e);
        }
    }

    public static List<SourceTable> getSourceTables() throws Exception {
        try {

            return TempSourcesDAO.selectSourceTables();

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as tabelas das fontes: " + e);
        }
    }

    public static List<File> getSourcesFiles() throws Exception {
        try {

            return TempSourcesDAO.selectSourcesFiles();

        } catch (Exception e) {
            throw new Exception("Não foi possível carregar as fontes: " + e);
        }
    }


    public static void clearTempTables() throws Exception {
        try {

            TempSourcesDAO.clearTables();

        } catch (Exception e) {
            throw new Exception("Não foi possível limpar as tabelas temporárias: " + e);
        }
    }


    /// PRIVATES

    public static void mapSources (List<Source> sourceList, ConfigRegistry configRegistry) {
        for (Source source : sourceList) {
            SourceLoader.mapSourceTable(source, configRegistry);
        }
    }

    private static List<Source> mapFilesToSources(List<File> fileList) {
        List<Source> sources = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {

            String sourceName = fileList.get(i).getName();
            String sourceId = FilenameUtils.removeExtension(sourceName);

            sources.add(new Source(sourceId, i, fileList.get(i)));
        }
        return sources;
    }

    /// HELPERS

    private static List<File> getDifferenceList(List<File> toCompareList, List<File> toRemoveList) {
        // Convert to set of canonical paths for reliable comparison
        Set<String> compareSet = toRemoveList.stream()
                .map(f -> {
                    try { return f.getCanonicalPath(); }
                    catch (Exception e) { return f.getAbsolutePath(); }
                })
                .collect(Collectors.toSet());

        // Keep only those in toCompareList that aren't in toRemoveList
        return toCompareList.stream()
                .filter(f -> {
                    try { return !compareSet.contains(f.getCanonicalPath()); }
                    catch (Exception e) { return !compareSet.contains(f.getAbsolutePath()); }
                })
                .collect(Collectors.toList());
    }
}
