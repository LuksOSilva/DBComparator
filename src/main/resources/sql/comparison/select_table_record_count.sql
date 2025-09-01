SELECT
	'{{table_name}}' AS TABLE_NAME,
	COUNT(*) AS RECORD_COUNT
FROM
	'{{source_id}}'.'{{table_name}}';