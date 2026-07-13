# Chronicles of Time Database SQL

This folder keeps executable SQL in three main files:

## Execute Order

```sql
source 01_schema.sql;
source 02_field_upgrades.sql;
source 03_test_data.sql;
```

## Files

- `01_schema.sql`: creates databases, tables, indexes, and base schema objects.
- `02_field_upgrades.sql`: adds columns required by current entities and upgraded features.
- `03_test_data.sql`: inserts repeatable test data, including high-school course-selection subjects and combinations.

Design and operations notes remain in the Markdown documents in this folder.