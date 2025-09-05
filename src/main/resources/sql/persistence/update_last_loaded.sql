UPDATE
    DBC_COMPARISONS
SET
    LAST_LOADED_AT = '{{last_loaded_at}}'
WHERE
    FILE_PATH = '{{file_path}}';