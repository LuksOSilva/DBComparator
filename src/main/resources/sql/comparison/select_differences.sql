WITH unified AS (
    {{with_clause}}
    )
SELECT
    {{select_clause}}
FROM unified
GROUP BY
    {{group_by_clause}}
HAVING
    {{having_clause}}