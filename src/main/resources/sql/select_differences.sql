WITH
    {{with_clause}}
SELECT
    {{coalesce_identifier_columns}}
    "rca_data"."config_value" AS "rca_config_value",
    "rca_data"."inherit"      AS "rca_inherit",
    "rca_data"."product_id"   AS "rca_product_id",
    "rca_data"."sync"         AS "rca_sync",
    "sup_data"."config_value" AS "sup_config_value",
    "sup_data"."inherit"      AS "sup_inherit",
    "sup_data"."product_id"   AS "sup_product_id",
    "sup_data"."sync"         AS "sup_sync",
    "tst_data"."config_value" AS "tst_config_value",
    "tst_data"."inherit"	  AS "tst_inherit",
    "tst_data"."product_id"   AS "tst_product_id",
    "tst_data"."sync"         AS "tst_sync"
FROM
    "rca_data"
    FULL JOIN "sup_data"
        ON
        ("rca_data"."codfilial" = "sup_data"."codfilial"
        AND "rca_data"."config_name" = "sup_data"."config_name"
        AND "rca_data"."config_type" = "sup_data"."config_type")
    FULL JOIN "tst_data"
        ON
        ("rca_data"."codfilial" = "tst_data"."codfilial"
        AND "rca_data"."config_name" = "tst_data"."config_name"
        AND "rca_data"."config_type" = "tst_data"."config_type")
        OR
        ("sup_data"."codfilial" = "tst_data"."codfilial"
        AND "sup_data"."config_name" = "tst_data"."config_name"
        AND "sup_data"."config_type" = "tst_data"."config_type")


WHERE
    (COALESCE("rca_data"."config_value", '') <> COALESCE("sup_data"."config_value", '')
    OR COALESCE("rca_data"."inherit", '') <> COALESCE("sup_data"."inherit", '')
    OR COALESCE("rca_data"."product_id", -1) <> COALESCE("sup_data"."product_id", -1)
    OR COALESCE("rca_data"."sync", '') <> COALESCE("sup_data"."sync", ''))

    OR

    (COALESCE("rca_data"."config_value", '') <> COALESCE("tst_data"."config_value", '')
    OR COALESCE("rca_data"."inherit", '') <> COALESCE("tst_data"."inherit", '')
    OR COALESCE("rca_data"."product_id", -1) <> COALESCE("tst_data"."product_id", -1)
    OR COALESCE("rca_data"."sync", '') <> COALESCE("tst_data"."sync", ''))

    OR

    (COALESCE("sup_data"."config_value", '') <> COALESCE("tst_data"."config_value", '')
    OR COALESCE("sup_data"."inherit", '') <> COALESCE("tst_data"."inherit", '')
    OR COALESCE("sup_data"."product_id", -1) <> COALESCE("tst_data"."product_id", -1)
    OR COALESCE("sup_data"."sync", '') <> COALESCE("tst_data"."sync", ''));