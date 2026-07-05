# Chronicles of Time Database Design

This folder contains the database redesign for the whole project.

## Status

`01_cot_enterprise_schema.sql` is complete as the current enterprise-level schema draft. It is designed for MySQL 8.0+ and covers the current Spring Cloud services and frontend business modules.

## Files

- `01_cot_enterprise_schema.sql`: executable schema script. It creates databases, tables, indexes, and minimal seed data.
- `02_database_design_document.md`: design notes for business boundaries and table relationships.
- `03_sharding_and_migration_guide.md`: sharding and migration guidance.
- `04_ops_redis_indexing.md`: indexing, Redis key, cache, and operations guidance.
- `05_activity_chat_upgrade.sql`: incremental script for check-in, online duration, medals, friends, groups, messages, and read receipts.

## Database Boundaries

- `cot_identity`: accounts, roles, permissions, refresh tokens, login audit.
- `cot_profile`: user profile, life stage, resume and resume submodules.
- `cot_content`: content publishing, media, tags, comments, likes, favorites, audits, growth records.
- `cot_learning`: subjects, knowledge graph, questions, options, practice sessions, answer records, mistakes, scores.
- `cot_highschool`: high school subject selection, grading scale, guidance, gaokao university/major/admission data, volunteer plans.
- `cot_university`: university majors, course tree, student course progress, graduation progress, thesis papers.
- `cot_platform`: file assets, dictionaries, notifications, admin operation logs, API access logs.

## Execute

Run in MySQL 8.0+:

```sql
source 01_cot_enterprise_schema.sql;
source 05_activity_chat_upgrade.sql;
```

The design intentionally avoids physical foreign keys across service databases. Cross-service relations are enforced through logical IDs, unique indexes, service-level validation, and scheduled consistency checks.
