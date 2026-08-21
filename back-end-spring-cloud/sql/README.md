# Chronicles of Time Database SQL

This folder keeps executable SQL in initialization and migration files:

## Execute Order

```sql
source 01_schema.sql;
source 03_test_data.sql;
source 05_growth_path_simulator.sql;
source 08_chat_message_lifecycle.sql;
source 08_university_campus_organizations.sql;
```

## Files

- `01_schema.sql`: creates databases, tables, indexes, base schema objects, and current-field upgrades. Uses MySQL 8.0.19+ row-alias upserts.
- `03_test_data.sql`: inserts repeatable test data, including high-school course-selection subjects and combinations.
- `05_growth_path_simulator.sql`: adds the growth path simulator catalog, user plans, version snapshots, and action checkpoints under `cot_advanced`. Safe to rerun.
- `08_chat_message_lifecycle.sql`: adds per-user chat-message deletion storage used by delete and two-minute recall workflows.
- `08_university_campus_organizations.sql`: adds student club, student union and campus activity records under `cot_university`.

Design and operations notes remain in the Markdown documents in this folder.


