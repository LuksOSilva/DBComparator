WITH
    {{with_clause}}
SELECT
    {{coalesce_identifier_columns}}
    {{select_comparable_columns}}
FROM
    {{from_clause}}
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