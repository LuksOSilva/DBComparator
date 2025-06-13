WITH last_comparison_id AS (
			SELECT IFNULL(MAX(CAST(COMPARISON_ID AS INTEGER)), 0) AS LAST_ID
			FROM DBC_COMPARISON_HEADER
	),
	
	next_comparison_id AS (
			SELECT LAST_ID + 1 AS NEXT_ID FROM last_comparison_id
	)
	
SELECT printf('%04d', NEXT_ID) AS NEXT_COMPARISON_ID
FROM next_comparison_id;