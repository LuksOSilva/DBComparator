SELECT
    1
FROM
    "{{source_id}}"."{{table_name}}" as "{{source_id}}_data"
WHERE
    {{filter_sql}}
LIMIT 1;