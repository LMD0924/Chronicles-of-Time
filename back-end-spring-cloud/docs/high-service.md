# high-service

## 定位

高中阶段与升学规划服务，覆盖新高考选科、赋分规则、选科指导、志愿填报、院校专业和录取模拟。

## 数据库

- `cot_highschool`

## 主要对象

- `Subject`、`SubjectCombination`
- `StudentCourseSelection`
- `CourseSelectionIntention`
- `CourseGuidance`
- `GradingScale`
- `University`、`Major`
- `AdmissionPlan`、`AdmissionSimulation`
- `UserVolunteer`、`VolunteerDetail`

## 路由

- `/api/selection/**`
- `/api/subject/**`
- `/api/subject-combination/**`
- `/api/intention/**`
- `/api/guidance/**`
- `/api/grading/**`
- `/api/volunteer/**`