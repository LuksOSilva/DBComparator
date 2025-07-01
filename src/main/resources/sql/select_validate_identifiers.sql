select
    distinct "{{source_id}}" as source_id
from
    "{{source_id}}"."{{table_name}}"
group by {{identifier_columns}} having count(*) >1;