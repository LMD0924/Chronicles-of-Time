# Chronicles of Time Database SQL

This folder keeps executable SQL in two main files:

## Execute Order

```sql
source 01_schema.sql;
source 03_test_data.sql;
```

## Files

- `01_schema.sql`: creates databases, tables, indexes, base schema objects, and current-field upgrades. Uses MySQL 8.0.19+ row-alias upserts.
- `03_test_data.sql`: inserts repeatable test data, including high-school course-selection subjects and combinations.

Design and operations notes remain in the Markdown documents in this folder.


