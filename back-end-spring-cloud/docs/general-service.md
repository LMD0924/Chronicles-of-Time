# general-service

## 定位

通用成长、学习和内容服务，覆盖成长记录、题库、错题、答题记录、成绩记录、知识图谱、内容社区和内容知识图谱。

## 数据库

- `cot_content`
- `cot_learning`

## 主要对象

- 成长记录：`Growth`
- 题库：`QuestionBank`
- 错题：`MistakeRecord`
- 答题记录：`AnswerRecords`
- 成绩记录：`ScoreRecord`
- 内容：`Content`、`Comment`、`LikeRecord`、`FavoriteRecord`
- 图谱：`KnowledgeGraph`、`KnowledgeNode`、`KnowledgeEdge`、`ContentKnowledgeGraph`

## 路由

- `/api/growth/**`
- `/api/question/**`
- `/api/mistake/**`
- `/api/score/**`
- `/api/content/**`
- `/api/graph/**`