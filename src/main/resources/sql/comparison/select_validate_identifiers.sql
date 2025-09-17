select
    1
from
    "%s"."%s"
group by
    %s
having
    count(1) >1
LIMIT 1;