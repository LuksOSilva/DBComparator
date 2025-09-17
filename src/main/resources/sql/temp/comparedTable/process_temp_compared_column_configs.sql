--	Rules for generating configs:
--	1. if column doesn't exist in all sources, it is neither identifier nor comparable
--	2. if config DBC_PRIORITIZE_USER_COLUMN_SETTINGS is active and the config from the table is saved, get info from DBC_COLUMN_DEFAULTS
--	3. if table has no pks, all columns are identifiers
--	4. if column is pk in at least 1 source, it is identifier. If not, it is comparable
INSERT OR REPLACE INTO TEMP_COMPARED_TABLE_COLUMN_CONFIGS
SELECT
    COD_COMPARED_COLUMN,
    IS_IDENTIFIER,
    IS_COMPARABLE
FROM
(
WITH
config AS
    (
    SELECT ? AS PRIORITIZE_USER_CONFIGS
    ),
table_pks AS
    (
    SELECT
        COD_COMPARED_TABLE,
        CASE WHEN SUM(IS_PK_ANY_SOURCE) >= 1 THEN 1 ELSE 0 END AS TABLE_HAS_PK
    FROM
        TEMP_COMPARED_TABLE_COLUMNS
    GROUP BY COD_COMPARED_TABLE
    )
SELECT
    cco.COD_COMPARED_COLUMN,
    CASE
        WHEN cco.EXISTS_ON_ALL_SOURCES = 0 THEN 0
        WHEN config.PRIORITIZE_USER_CONFIGS = 1 AND saved.IS_IDENTIFIER IS NOT NULL THEN saved.IS_IDENTIFIER
        WHEN tpk.TABLE_HAS_PK = 0 THEN 1
        WHEN cco.IS_PK_ANY_SOURCE = 1 THEN 1
        ELSE 0
    END AS IS_IDENTIFIER,
    CASE
        WHEN cco.EXISTS_ON_ALL_SOURCES = 0 THEN 0
        WHEN config.PRIORITIZE_USER_CONFIGS = 1 AND saved.IS_COMPARABLE IS NOT NULL THEN saved.IS_COMPARABLE
        WHEN tpk.TABLE_HAS_PK = 0 THEN 0
        WHEN cco.IS_PK_ANY_SOURCE = 0 THEN 1
        ELSE 0
    END AS IS_COMPARABLE
FROM
    TEMP_COMPARED_TABLE_COLUMNS cco
    INNER JOIN TEMP_COMPARED_TABLES cta ON cta.COD_COMPARED_TABLE = cco.COD_COMPARED_TABLE
    INNER JOIN table_pks tpk on tpk.COD_COMPARED_TABLE = cta.COD_COMPARED_TABLE
    INNER JOIN config
    LEFT JOIN DBC_COLUMN_DEFAULTS saved ON saved.TABLE_NAME = cta.TABLE_NAME AND saved.COLUMN_NAME = cco.COLUMN_NAME
WHERE
    cta.TABLE_NAME = ?
)