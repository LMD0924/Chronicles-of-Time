package org.example.authcenter.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDataService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    private record ModuleSpec(String source, String id, String title, String owner, String category,
                              String status, String metric, String createdAt, String updatedAt,
                              String condition) {
    }

    private record FieldSpec(String name, String label, String type, boolean required,
                             boolean createOnly, Object defaultValue, List<String> options) {
    }

    private record CrudSpec(String table, String idColumn, String softDeleteColumn) {
    }

    private static final Set<String> READ_ONLY_MODULES = Set.of(
            "system-log", "monitor-api", "monitor-online", "monitor-service",
            "stage-high", "stage-university", "stage-work", "stage-advanced"
    );
    private static final Set<String> PROTECTED_COLUMNS = Set.of(
            "id", "created_at", "updated_at", "deleted_at", "recalled_at",
            "password", "password_hash", "token_jti"
    );
    private static final Map<String, ModuleSpec> MODULES = createModules();
    private static final Map<String, CrudSpec> CRUD_MODULES = createCrudModules();

    public Map<String, Object> list(String moduleKey, String keyword, String status, int page, int pageSize) {
        if ("monitor-service".equals(moduleKey)) {
            return serviceStatus(keyword, status, page, pageSize);
        }
        ModuleSpec spec = MODULES.get(moduleKey);
        if (spec == null) {
            throw new IllegalArgumentException("不支持的后台数据模块: " + moduleKey);
        }

        int safePage = Math.max(1, page);
        int safePageSize = Math.min(100, Math.max(1, pageSize));
        List<Object> args = new ArrayList<>();
        String filter = buildFilter(spec, keyword, status, args);
        String from = " FROM " + spec.source + " t WHERE " + spec.condition + filter;
        long total = number("SELECT COUNT(*)" + from, args.toArray());

        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add((safePage - 1) * safePageSize);
        listArgs.add(safePageSize);
        String sql = "SELECT " + spec.id + " AS row_id, " + spec.title + " AS row_title, "
                + spec.owner + " AS row_name, " + spec.category + " AS row_category, "
                + spec.status + " AS row_status, " + spec.metric + " AS row_metric, "
                + spec.createdAt + " AS row_created, " + spec.updatedAt + " AS row_updated"
                + from + " ORDER BY " + spec.updatedAt + " DESC LIMIT ?, ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, listArgs.toArray()).stream()
                .map(this::normalizeRow)
                .toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("list", rows);
        result.put("stats", statistics(spec));
        result.put("capabilities", capabilities(moduleKey));
        return result;
    }

    public Map<String, Object> capabilities(String moduleKey) {
        CrudSpec crud = CRUD_MODULES.get(moduleKey);
        if (crud == null) {
            return Map.of("view", false, "create", false, "update", false, "delete", false, "fields", List.of());
        }
        boolean messageModule = "chat-message".equals(moduleKey);
        return Map.of("view", true, "create", !messageModule, "update", !messageModule, "delete", true, "fields", fieldSpecs(crud));
    }

    public Map<String, Object> detail(String moduleKey, String id) {
        CrudSpec crud = requireCrud(moduleKey);
        validateId(id);
        List<FieldSpec> fields = fieldSpecs(crud);
        String columns = fields.stream().map(FieldSpec::name).collect(Collectors.joining(","));
        String sql = "SELECT " + crud.idColumn + " AS id" + (columns.isBlank() ? "" : "," + columns)
                + " FROM " + crud.table + " WHERE " + crud.idColumn + " = ?" + activeRow(crud);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
        if (rows.isEmpty()) throw new IllegalArgumentException("记录不存在或已被删除");
        return normalizeDetail(rows.get(0));
    }

    @Transactional(rollbackFor = Exception.class)
    public String create(String moduleKey, Map<String, Object> payload, long adminUserId,
                         String requestUri, String clientIp, String userAgent) {
        CrudSpec crud = requireCrud(moduleKey);
        if ("chat-message".equals(moduleKey)) throw new IllegalArgumentException("聊天消息不允许由管理员伪造发送");
        List<FieldSpec> fields = fieldSpecs(crud);
        Map<String, Object> values = sanitizePayload(fields, withModuleDefaults(moduleKey, payload), true);
        String id = String.valueOf(IdWorker.getId());
        List<String> columns = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        columns.add(crud.idColumn);
        args.add(id);
        values.forEach((column, value) -> {
            columns.add(column);
            args.add(value);
        });
        String placeholders = columns.stream().map(column -> "?").collect(Collectors.joining(","));
        jdbcTemplate.update("INSERT INTO " + crud.table + " (" + String.join(",", columns) + ") VALUES ("
                + placeholders + ")", args.toArray());
        audit(adminUserId, moduleKey, "CREATE", "新增记录 " + id, "POST", requestUri,
                clientIp, userAgent, 200);
        return id;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(String moduleKey, String id, Map<String, Object> payload, long adminUserId,
                       String requestUri, String clientIp, String userAgent) {
        CrudSpec crud = requireCrud(moduleKey);
        if ("chat-message".equals(moduleKey)) throw new IllegalArgumentException("聊天消息内容不可篡改，只能删除");
        validateId(id);
        Map<String, Object> values = sanitizePayload(fieldSpecs(crud), payload, false);
        if (values.isEmpty()) throw new IllegalArgumentException("没有可更新的字段");
        List<Object> args = new ArrayList<>(values.values());
        args.add(id);
        String assignments = values.keySet().stream().map(column -> column + " = ?")
                .collect(Collectors.joining(","));
        int updated = jdbcTemplate.update("UPDATE " + crud.table + " SET " + assignments + " WHERE "
                + crud.idColumn + " = ?" + activeRow(crud), args.toArray());
        if (updated == 0) throw new IllegalArgumentException("记录不存在或已被删除");
        audit(adminUserId, moduleKey, "UPDATE", "更新记录 " + id, "PUT", requestUri,
                clientIp, userAgent, 200);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String moduleKey, String id, long adminUserId, String requestUri,
                       String clientIp, String userAgent) {
        CrudSpec crud = requireCrud(moduleKey);
        validateId(id);
        int changed;
        if (crud.softDeleteColumn != null) {
            changed = jdbcTemplate.update("UPDATE " + crud.table + " SET " + crud.softDeleteColumn
                    + " = NOW() WHERE " + crud.idColumn + " = ? AND " + crud.softDeleteColumn + " IS NULL", id);
        } else {
            changed = jdbcTemplate.update("DELETE FROM " + crud.table + " WHERE " + crud.idColumn + " = ?", id);
        }
        if (changed == 0) throw new IllegalArgumentException("记录不存在或已被删除");
        audit(adminUserId, moduleKey, "DELETE", "删除记录 " + id, "DELETE", requestUri,
                clientIp, userAgent, 200);
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cards", List.of(
                card("用户总数", count("cot_identity.iam_user", "deleted_at IS NULL"), "人", "User"),
                card("今日注册", count("cot_identity.iam_user", "deleted_at IS NULL AND DATE(created_at)=CURDATE()"), "人", "UserFilled"),
                card("内容总数", count("cot_content.content_article", "deleted_at IS NULL"), "篇", "Notebook"),
                card("待审题目", count("cot_learning.question", "deleted_at IS NULL AND LOWER(COALESCE(audit_status,'')) NOT IN ('approved','active')"), "题", "Bell"),
                card("论文数量", count("cot_university.thesis_paper", "deleted_at IS NULL"), "份", "Document"),
                card("接口访问", count("cot_platform.sys_api_access_log", "1=1"), "次", "Histogram")
        ));
        result.put("userTrend", dailyCounts("cot_identity.iam_user", "created_at", "deleted_at IS NULL"));
        result.put("contentTrend", dailyCounts("cot_content.content_article", "created_at", "deleted_at IS NULL"));
        result.put("contentDistribution", List.of(
                namedValue("文章", count("cot_content.content_article", "deleted_at IS NULL")),
                namedValue("评论", count("cot_content.content_comment", "deleted_at IS NULL")),
                namedValue("题目", count("cot_learning.question", "deleted_at IS NULL")),
                namedValue("错题", count("cot_learning.mistake_record", "deleted_at IS NULL")),
                namedValue("论文", count("cot_university.thesis_paper", "deleted_at IS NULL")),
                namedValue("文件", count("cot_platform.file_asset", "deleted_at IS NULL"))
        ));
        result.put("moduleDistribution", List.of(
                namedValue("学习中心", count("cot_learning.answer_record", "1=1")),
                namedValue("升学规划", count("cot_highschool.hs_student_selection", "deleted_at IS NULL")),
                namedValue("内容中心", count("cot_content.content_article", "deleted_at IS NULL")),
                namedValue("大学学业", count("cot_university.uni_student_course", "deleted_at IS NULL")),
                namedValue("职场成长", count("cot_workplace.career_task", "1=1")),
                namedValue("进阶成长", count("cot_advanced.advancement_milestone", "1=1"))
        ));
        result.put("accessHeatmap", accessHeatmap());
        result.put("logs", latestLogs());
        result.put("todos", todos());
        return result;
    }

    private Map<String, Object> statistics(ModuleSpec spec) {
        String from = " FROM " + spec.source + " t WHERE " + spec.condition;
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", number("SELECT COUNT(*)" + from));
        stats.put("metricTotal", number("SELECT COALESCE(SUM(" + spec.metric + "),0)" + from));
        stats.put("categories", groupedCounts(spec.category, from));
        stats.put("statuses", groupedCounts(spec.status, from));
        stats.put("trend", sevenDayTrend(spec));
        return stats;
    }

    private List<Map<String, Object>> groupedCounts(String expression, String from) {
        String sql = "SELECT " + expression + " AS item_name, COUNT(*) AS item_value" + from
                + " GROUP BY " + expression + " ORDER BY item_value DESC LIMIT 12";
        return jdbcTemplate.queryForList(sql).stream()
                .map(row -> namedValue(text(row.get("item_name")), ((Number) row.get("item_value")).longValue()))
                .toList();
    }

    private List<Map<String, Object>> sevenDayTrend(ModuleSpec spec) {
        return dailyCounts(spec.source, spec.createdAt, spec.condition);
    }

    private List<Map<String, Object>> dailyCounts(String source, String dateExpression, String condition) {
        Map<String, Long> counts = new LinkedHashMap<>();
        String sql = "SELECT DATE(" + dateExpression + ") AS item_day, COUNT(*) AS item_value FROM " + source
                + " t WHERE " + condition + " AND " + dateExpression + " >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)"
                + " GROUP BY DATE(" + dateExpression + ")";
        jdbcTemplate.queryForList(sql).forEach(row -> counts.put(
                String.valueOf(row.get("item_day")), ((Number) row.get("item_value")).longValue()));
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            LocalDate date = LocalDate.now().minusDays(offset);
            trend.add(Map.of("date", date.toString(), "label", date.getMonthValue() + "/" + date.getDayOfMonth(),
                    "value", counts.getOrDefault(date.toString(), 0L)));
        }
        return trend;
    }

    private String buildFilter(ModuleSpec spec, String keyword, String status, List<Object> args) {
        StringBuilder filter = new StringBuilder();
        if (keyword != null && !keyword.isBlank()) {
            filter.append(" AND (CAST(").append(spec.id).append(" AS CHAR) LIKE ? OR CAST(")
                    .append(spec.title).append(" AS CHAR) LIKE ? OR CAST(").append(spec.owner)
                    .append(" AS CHAR) LIKE ? OR CAST(").append(spec.category).append(" AS CHAR) LIKE ?)");
            String value = "%" + keyword.trim() + "%";
            args.add(value);
            args.add(value);
            args.add(value);
            args.add(value);
        }
        if (status != null && !status.isBlank() && !"全部".equals(status)) {
            filter.append(" AND CAST(").append(spec.status).append(" AS CHAR) = ?");
            args.add(status);
        }
        return filter.toString();
    }

    private Map<String, Object> normalizeRow(Map<String, Object> source) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", text(source.get("row_id")));
        row.put("title", text(source.get("row_title")));
        row.put("name", text(source.get("row_name")));
        row.put("category", text(source.get("row_category")));
        row.put("status", text(source.get("row_status")));
        Object metric = source.get("row_metric");
        row.put("metric", metric instanceof Number ? metric : 0);
        row.put("createdAt", format(source.get("row_created")));
        row.put("updatedAt", format(source.get("row_updated")));
        return row;
    }

    private Map<String, Object> serviceStatus(String keyword, String status, int page, int pageSize) {
        List<ServicePort> services = List.of(
                new ServicePort("gateway", 8500), new ServicePort("auth-center", 8080),
                new ServicePort("user-center", 8081), new ServicePort("high-service", 8082),
                new ServicePort("university-service", 8083), new ServicePort("general-service", 8084),
                new ServicePort("workplace-service", 8085), new ServicePort("advanced-service", 8086),
                new ServicePort("file-upload", 8090)
        );
        List<Map<String, Object>> filtered = services.stream().map(service -> {
            boolean online = portOpen(service.port);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", service.name);
            row.put("title", service.name);
            row.put("name", "127.0.0.1:" + service.port);
            row.put("category", online ? "在线" : "离线");
            row.put("status", online ? "正常" : "离线");
            row.put("metric", apiCount(service.name));
            row.put("createdAt", "-");
            row.put("updatedAt", LocalDateTime.now().format(DATE_TIME_FORMAT));
            return row;
        }).filter(row -> keyword == null || keyword.isBlank()
                || String.valueOf(row.get("title")).toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)))
                .filter(row -> status == null || status.isBlank() || "全部".equals(status) || status.equals(row.get("status")))
                .toList();
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, pageSize));
        int from = Math.min(filtered.size(), (safePage - 1) * safeSize);
        int to = Math.min(filtered.size(), from + safeSize);
        long online = filtered.stream().filter(row -> "正常".equals(row.get("status"))).count();
        long metricTotal = filtered.stream().mapToLong(row -> ((Number) row.get("metric")).longValue()).sum();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", (long) filtered.size());
        stats.put("metricTotal", metricTotal);
        stats.put("categories", List.of(namedValue("在线", online), namedValue("离线", filtered.size() - online)));
        stats.put("statuses", stats.get("categories"));
        stats.put("trend", dailyCounts("cot_platform.sys_api_access_log", "t.created_at", "1=1"));
        return Map.of("total", (long) filtered.size(), "list", filtered.subList(from, to), "stats", stats,                "capabilities", capabilities("monitor-service"));
    }

    private boolean portOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 300);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private long apiCount(String service) {
        return number("SELECT COUNT(*) FROM cot_platform.sys_api_access_log WHERE service_name = ?", service);
    }

    private List<Map<String, Object>> accessHeatmap() {
        return jdbcTemplate.queryForList(
                        "SELECT WEEKDAY(created_at) AS item_weekday, FLOOR(HOUR(created_at)/4) AS item_period, COUNT(*) AS item_value "
                                + "FROM cot_platform.sys_api_access_log WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) "
                                + "GROUP BY WEEKDAY(created_at), FLOOR(HOUR(created_at)/4)")
                .stream().map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("weekday", ((Number) row.get("item_weekday")).intValue());
                    item.put("period", ((Number) row.get("item_period")).intValue());
                    item.put("value", ((Number) row.get("item_value")).longValue());
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> latestLogs() {
        String sql = "SELECT event_type, event_text, event_time FROM ("
                + "SELECT '后台操作' event_type, operation_desc event_text, created_at event_time FROM cot_platform.admin_operation_log "
                + "UNION ALL SELECT '用户登录', CONCAT(username,' · ',login_result,COALESCE(CONCAT(' · ',fail_reason),'')), created_at FROM cot_identity.iam_login_audit "
                + "UNION ALL SELECT '接口访问', CONCAT(request_method,' ',request_uri,' · ',response_code), created_at FROM cot_platform.sys_api_access_log"
                + ") events ORDER BY event_time DESC LIMIT 8";
        return jdbcTemplate.queryForList(sql).stream().map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", text(row.get("event_type")));
                    item.put("text", text(row.get("event_text")));
                    item.put("time", format(row.get("event_time")));
                    return item;
                })
                .toList();
    }

    private List<String> todos() {
        List<String> result = new ArrayList<>();
        long questions = count("cot_learning.question", "deleted_at IS NULL AND LOWER(COALESCE(audit_status,'')) NOT IN ('approved','active')");
        long audits = count("cot_content.content_audit", "LOWER(COALESCE(audit_status,'')) IN ('pending','todo','0')");
        long overdue = count("cot_workplace.career_task", "status <> 'DONE' AND due_date < CURDATE()" );
        long expiring = count("cot_identity.iam_refresh_token", "revoked_at IS NULL AND expires_at BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 1 DAY)");
        if (questions > 0) result.add("待审核题目 " + questions + " 条");
        if (audits > 0) result.add("待审核内容 " + audits + " 条");
        if (overdue > 0) result.add("逾期职场任务 " + overdue + " 条");
        if (expiring > 0) result.add("24 小时内过期会话 " + expiring + " 个");
        if (result.isEmpty()) result.add("当前没有待处理事项");
        return result;
    }

    private CrudSpec requireCrud(String moduleKey) {
        CrudSpec crud = CRUD_MODULES.get(moduleKey);
        if (crud == null) throw new IllegalArgumentException("该模块为只读数据，不能执行写操作");
        return crud;
    }

    private List<FieldSpec> fieldSpecs(CrudSpec crud) {
        String[] tableParts = crud.table.split("\\.");
        String schema = tableParts[0];
        String table = tableParts[1];
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA "
                + "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
        return jdbcTemplate.queryForList(sql, schema, table).stream()
                .filter(row -> !PROTECTED_COLUMNS.contains(String.valueOf(row.get("COLUMN_NAME"))))
                .map(row -> toFieldSpec(row, crud))
                .toList();
    }

    private FieldSpec toFieldSpec(Map<String, Object> row, CrudSpec crud) {
        String name = String.valueOf(row.get("COLUMN_NAME"));
        String dataType = String.valueOf(row.get("DATA_TYPE")).toLowerCase(Locale.ROOT);
        boolean required = "NO".equals(row.get("IS_NULLABLE")) && row.get("COLUMN_DEFAULT") == null
                && !String.valueOf(row.get("EXTRA")).toLowerCase(Locale.ROOT).contains("auto_increment");
        String type = switch (dataType) {
            case "tinyint" -> name.startsWith("is_") || name.endsWith("_enabled") || name.equals("searchable")
                    || name.equals("muted_all") ? "boolean" : "number";
            case "bigint" -> name.equals("id") || name.endsWith("_id") ? "text" : "number";
            case "smallint", "mediumint", "int", "decimal", "float", "double" -> "number";
            case "date" -> "date";
            case "datetime", "timestamp" -> "datetime";
            case "text", "mediumtext", "longtext", "json" -> "textarea";
            default -> "text";
        };
        List<String> options = fieldOptions(crud.table, name);
        if (!options.isEmpty()) type = "select";
        Object defaultValue = row.get("COLUMN_DEFAULT");
        if ("boolean".equals(type)) defaultValue = "1".equals(String.valueOf(defaultValue));
        return new FieldSpec(name, fieldLabel(name), type, required, false, defaultValue, options);
    }

    private List<String> fieldOptions(String table, String name) {
        if ("cot_workplace.career_task".equals(table)) {
            return switch (name) {
                case "status" -> List.of("TODO", "DOING", "DONE", "CANCELLED");
                case "priority" -> List.of("LOW", "MEDIUM", "HIGH", "URGENT");
                case "quadrant" -> List.of("IMPORTANT_URGENT", "IMPORTANT_NOT_URGENT", "NOT_IMPORTANT_URGENT", "NOT_IMPORTANT_NOT_URGENT");
                case "repeat_rule" -> List.of("NONE", "DAILY", "WEEKLY", "MONTHLY");
                default -> List.of();
            };
        }
        if ("cot_content.chat_group_member".equals(table) && "role".equals(name)) {
            return List.of("OWNER", "ADMIN", "MEMBER");
        }
        if ("cot_content.chat_group_member".equals(table) && "status".equals(name)) {
            return List.of("ACTIVE", "LEFT", "REMOVED");
        }
        if ("cot_content.chat_friend".equals(table) && "status".equals(name)) {
            return List.of("ACTIVE", "BLOCKED", "DELETED");
        }
        if ("cot_content.chat_message".equals(table)) {
            if ("conversation_type".equals(name)) return List.of("PRIVATE", "GROUP");
            if ("content_type".equals(name)) return List.of("TEXT", "IMAGE", "FILE");
        }
        if ("cot_platform.sys_notification".equals(table) && "read_status".equals(name)) {
            return List.of("0", "1");
        }
        if (name.equals("status") || name.endsWith("_status")) return List.of("0", "1");
        return List.of();
    }

    private String fieldLabel(String name) {
        return Map.ofEntries(
                Map.entry("user_id", "用户ID"), Map.entry("goal_id", "目标ID"),
                Map.entry("task_name", "任务名称"), Map.entry("task_type", "任务类型"),
                Map.entry("status", "状态"), Map.entry("priority", "优先级"),
                Map.entry("quadrant", "四象限"), Map.entry("start_date", "开始日期"),
                Map.entry("due_date", "截止日期"), Map.entry("estimated_minutes", "预计分钟"),
                Map.entry("actual_minutes", "实际分钟"), Map.entry("outcome", "成果"),
                Map.entry("notes", "备注"), Map.entry("tags", "标签"),
                Map.entry("reminder_enabled", "启用提醒"), Map.entry("reminder_at", "提醒时间"),
                Map.entry("repeat_rule", "重复规则"), Map.entry("completed_at", "完成时间"),
                Map.entry("title", "标题"), Map.entry("content", "内容"),
                Map.entry("content_md", "Markdown内容"), Map.entry("summary", "摘要"),
                Map.entry("content_type", "内容类型"), Map.entry("visibility", "可见范围"),
                Map.entry("category_id", "分类ID"), Map.entry("comment_text", "评论内容"),
                Map.entry("category_name", "分类名称"), Map.entry("category_code", "分类编码"),
                Map.entry("description", "说明"), Map.entry("sort_order", "排序"),
                Map.entry("group_no", "群号"), Map.entry("name", "名称"),
                Map.entry("announcement", "公告"), Map.entry("owner_id", "群主ID"),
                Map.entry("member_count", "成员数"), Map.entry("searchable", "允许搜索"),
                Map.entry("muted_all", "全员禁言"), Map.entry("pinned_message_id", "置顶消息ID"),
                Map.entry("group_id", "群组ID"), Map.entry("friend_id", "好友ID"),
                Map.entry("remark", "备注"), Map.entry("role", "角色"),
                Map.entry("muted_until", "禁言截止"), Map.entry("joined_at", "加入时间"),
                Map.entry("last_read_at", "最后已读时间"), Map.entry("conversation_type", "会话类型"),
                Map.entry("sender_id", "发送者ID"), Map.entry("receiver_id", "接收者ID"),
                Map.entry("notice_type", "通知类型"), Map.entry("biz_type", "业务类型"),
                Map.entry("biz_id", "业务ID"), Map.entry("dedupe_key", "去重标识"),
                Map.entry("action_path", "跳转路径"), Map.entry("due_at", "到期时间"),
                Map.entry("read_status", "已读状态"), Map.entry("read_at", "已读时间"),
                Map.entry("dismissed_at", "忽略时间"), Map.entry("week_start", "周开始"),
                Map.entry("week_end", "周结束"), Map.entry("report_json", "周报数据"),
                Map.entry("reflection", "本周反思"), Map.entry("next_week_focus", "下周重点")
        ).getOrDefault(name, name.replace('_', ' '));
    }

    private Map<String, Object> withModuleDefaults(String moduleKey, Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (payload != null) result.putAll(payload);
        if (!result.containsKey("content_type")) {
            if ("content-note".equals(moduleKey)) result.put("content_type", "journal");
            if ("content-moment".equals(moduleKey)) result.put("content_type", "moment");
            if ("content-letter".equals(moduleKey)) result.put("content_type", "letter");
        }
        return result;
    }
    private Map<String, Object> sanitizePayload(List<FieldSpec> fields, Map<String, Object> payload, boolean creating) {
        if (payload == null) throw new IllegalArgumentException("请求数据不能为空");
        Map<String, FieldSpec> allowed = fields.stream().collect(Collectors.toMap(FieldSpec::name, field -> field));
        Map<String, Object> result = new LinkedHashMap<>();
        for (FieldSpec field : fields) {
            boolean supplied = payload.containsKey(field.name);
            Object raw = supplied ? payload.get(field.name) : null;
            if (creating && field.required && (raw == null || String.valueOf(raw).isBlank())) {
                throw new IllegalArgumentException(field.label + "不能为空");
            }
            if (!supplied) continue;
            if (raw == null || (raw instanceof String text && text.isBlank())) {
                if (creating && field.required) throw new IllegalArgumentException(field.label + "不能为空");
                if (supplied) result.put(field.name, null);
                continue;
            }
            result.put(field.name, convertValue(field, raw));
        }
        for (String key : payload.keySet()) {
            if (!allowed.containsKey(key)) throw new IllegalArgumentException("字段不允许修改: " + key);
        }
        return result;
    }

    private Object convertValue(FieldSpec field, Object raw) {
        if ("boolean".equals(field.type)) {
            return Boolean.TRUE.equals(raw) || "true".equalsIgnoreCase(String.valueOf(raw)) || "1".equals(String.valueOf(raw)) ? 1 : 0;
        }
        if ("number".equals(field.type)) {
            String value = String.valueOf(raw).trim();
            if (!value.matches("-?\\d+(\\.\\d+)?")) throw new IllegalArgumentException(field.label + "必须是数字");
            return value;
        }
        String value = String.valueOf(raw).trim();
        if (!field.options.isEmpty() && field.options.stream().noneMatch(value::equals)) {
            throw new IllegalArgumentException(field.label + "不是有效选项");
        }
        return value;
    }

    private Map<String, Object> normalizeDetail(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (value instanceof Timestamp timestamp) result.put(key, timestamp.toLocalDateTime().format(DATE_TIME_FORMAT));
            else if (value instanceof Long || value instanceof java.math.BigInteger) result.put(key, String.valueOf(value));
            else result.put(key, value);
        });
        return result;
    }

    private String activeRow(CrudSpec crud) {
        return crud.softDeleteColumn == null ? "" : " AND " + crud.softDeleteColumn + " IS NULL";
    }

    private void validateId(String id) {
        if (id == null || !id.matches("\\d{1,20}")) throw new IllegalArgumentException("记录编号格式错误");
    }

    private void audit(long adminUserId, String module, String operation, String description,
                       String method, String uri, String clientIp, String userAgent, int responseCode) {
        jdbcTemplate.update("INSERT INTO cot_platform.admin_operation_log "
                        + "(id,admin_user_id,module_name,operation_type,operation_desc,request_method,request_uri,response_code,client_ip,user_agent,created_at) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,NOW())",
                IdWorker.getId(), adminUserId, module, operation, description, method, uri, responseCode,
                clientIp, userAgent == null ? null : userAgent.substring(0, Math.min(512, userAgent.length())));
    }
    private long count(String table, String condition) {
        return number("SELECT COUNT(*) FROM " + table + " WHERE " + condition);
    }

    private long number(String sql, Object... args) {
        Number result = jdbcTemplate.queryForObject(sql, Number.class, args);
        return result == null ? 0L : result.longValue();
    }

    private String text(Object value) {
        return value == null || String.valueOf(value).isBlank() ? "-" : String.valueOf(value);
    }

    private String format(Object value) {
        if (value == null) return "-";
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime().format(DATE_TIME_FORMAT);
        if (value instanceof LocalDateTime dateTime) return dateTime.format(DATE_TIME_FORMAT);
        return String.valueOf(value);
    }

    private Map<String, Object> card(String label, long value, String unit, String icon) {
        return Map.of("label", label, "value", value, "unit", unit, "icon", icon);
    }

    private Map<String, Object> namedValue(String name, long value) {
        return Map.of("name", name, "value", value);
    }

    private record ServicePort(String name, int port) {
    }

    private static ModuleSpec spec(String source, String title, String owner, String category,
                                   String status, String metric, String condition) {
        return new ModuleSpec(source, "t.id", title, owner, category, status, metric,
                "t.created_at", "t.updated_at", condition);
    }

    private static ModuleSpec timedSpec(String source, String title, String owner, String category,
                                        String status, String metric, String createdAt,
                                        String updatedAt, String condition) {
        return new ModuleSpec(source, "t.id", title, owner, category, status, metric,
                createdAt, updatedAt, condition);
    }

    private static ModuleSpec article(String condition) {
        return spec("cot_content.content_article", "COALESCE(t.title,'未命名内容')", "CAST(t.user_id AS CHAR)",
                "COALESCE(t.content_type,'未分类')", "CAST(t.status AS CHAR)", "COALESCE(t.view_count,0)",
                "t.deleted_at IS NULL AND " + condition);
    }

    private static Map<String, ModuleSpec> createModules() {
        Map<String, ModuleSpec> modules = new LinkedHashMap<>();
        modules.put("system-role", spec("cot_identity.iam_role", "t.role_name", "t.role_code", "CAST(t.role_scope AS CHAR)", "CAST(t.status AS CHAR)", "t.sort_order", "1=1"));
        modules.put("system-menu", spec("cot_identity.iam_permission", "t.permission_name", "COALESCE(t.route_path,'-')", "CAST(t.permission_type AS CHAR)", "CAST(t.status AS CHAR)", "t.sort_order", "t.route_path IS NOT NULL"));
        modules.put("system-perm", spec("cot_identity.iam_permission", "t.permission_name", "t.permission_code", "CAST(t.permission_type AS CHAR)", "CAST(t.status AS CHAR)", "t.sort_order", "1=1"));
        modules.put("system-log", timedSpec("cot_platform.admin_operation_log", "COALESCE(t.operation_desc,t.operation_type)", "CAST(t.admin_user_id AS CHAR)", "COALESCE(t.module_name,'操作日志')", "CASE WHEN t.response_code BETWEEN 200 AND 399 THEN '正常' ELSE '异常' END", "COALESCE(t.cost_ms,0)", "t.created_at", "t.created_at", "1=1"));
        modules.put("monitor-api", timedSpec("cot_platform.sys_api_access_log", "CONCAT(t.request_method,' ',t.request_uri)", "COALESCE(t.service_name,'未知服务')", "COALESCE(t.service_name,'未知服务')", "CASE WHEN t.response_code BETWEEN 200 AND 399 THEN '正常' ELSE '异常' END", "COALESCE(t.cost_ms,0)", "t.created_at", "t.created_at", "1=1"));
        modules.put("monitor-online", timedSpec("cot_identity.iam_refresh_token", "CONCAT('会话 ',LEFT(t.token_jti,8))", "CAST(t.user_id AS CHAR)", "COALESCE(t.device_name,'未知设备')", "CASE WHEN t.revoked_at IS NULL AND t.expires_at > NOW() THEN '在线' ELSE '离线' END", "0", "t.created_at", "COALESCE(t.revoked_at,t.expires_at)", "1=1"));
        modules.put("learn-answer", timedSpec("cot_learning.answer_record", "COALESCE(t.knowledge_point,t.subject_name,'答题记录')", "CAST(t.user_id AS CHAR)", "t.question_type", "CASE WHEN t.is_correct=1 THEN '正确' ELSE '错误' END", "COALESCE(t.answer_time_seconds,0)", "t.created_at", "COALESCE(t.answer_at,t.created_at)", "1=1"));
        modules.put("learn-mistake", spec("cot_learning.mistake_record", "COALESCE(t.mistake_name,t.knowledge_point,'错题')", "CAST(t.user_id AS CHAR)", "t.subject_name", "CASE WHEN t.mastered=1 THEN '已掌握' ELSE '未掌握' END", "COALESCE(t.mistake_count,0)", "t.deleted_at IS NULL"));
        modules.put("learn-score", spec("cot_learning.score_record", "t.exam_name", "CAST(t.user_id AS CHAR)", "t.exam_type", "'已记录'", "COALESCE(t.score,0)", "t.deleted_at IS NULL"));
        modules.put("learn-graph", spec("cot_learning.knowledge_point", "t.point_name", "CAST(t.subject_id AS CHAR)", "t.difficulty_level", "CAST(t.status AS CHAR)", "t.sort_order", "1=1"));
        modules.put("learn-heat", timedSpec("cot_learning.knowledge_mastery_stat", "CONCAT('知识点 ',t.knowledge_point_id)", "CAST(t.user_id AS CHAR)", "CONCAT('学科 ',t.subject_id)", "CASE WHEN t.mastery_score>=80 THEN '已掌握' WHEN t.mastery_score>=60 THEN '学习中' ELSE '薄弱' END", "COALESCE(t.answer_count,0)", "t.updated_at", "t.updated_at", "1=1"));
        modules.put("content-article", article("1=1"));
        modules.put("content-note", article("LOWER(COALESCE(t.content_type,'')) IN ('journal','markdown','note')"));
        modules.put("content-moment", article("LOWER(COALESCE(t.content_type,'')) IN ('moment','dynamic','social')"));
        modules.put("content-letter", article("LOWER(COALESCE(t.content_type,'')) IN ('quote','letter','message')"));
        modules.put("content-comment", spec("cot_content.content_comment", "LEFT(t.comment_text,80)", "CAST(t.user_id AS CHAR)", "CONCAT('内容 ',t.content_id)", "CAST(t.status AS CHAR)", "COALESCE(t.like_count,0)", "t.deleted_at IS NULL"));
        modules.put("content-album", timedSpec("cot_content.content_media", "COALESCE(t.media_url,'媒体文件')", "CAST(t.user_id AS CHAR)", "t.media_type", "'有效'", "t.sort_order", "t.created_at", "t.created_at", "1=1"));
        modules.put("content-graph", spec("cot_content.content_category", "t.category_name", "CAST(t.user_id AS CHAR)", "t.category_code", "CAST(t.status AS CHAR)", "t.sort_order", "1=1"));
        modules.put("plan-selection", spec("cot_highschool.hs_student_selection", "COALESCE(t.combination_name,'选科记录')", "COALESCE(t.user_name,CAST(t.user_id AS CHAR))", "t.grade", "CAST(t.status AS CHAR)", "COALESCE(t.total_score,0)", "t.deleted_at IS NULL"));
        modules.put("plan-approval", timedSpec("cot_highschool.hs_selection_history", "COALESCE(t.change_reason,t.change_type)", "COALESCE(t.user_name,CAST(t.user_id AS CHAR))", "t.change_type", "t.approve_status", "0", "COALESCE(t.change_time,t.created_at)", "COALESCE(t.change_time,t.created_at)", "1=1"));
        modules.put("plan-grading", spec("cot_highschool.hs_grading_scale", "CONCAT(t.subject_name,' ',t.grade_level)", "t.province", "t.grade_level", "CAST(t.status AS CHAR)", "COALESCE(t.assigned_score_max,0)", "1=1"));
        modules.put("plan-guidance", spec("cot_highschool.hs_course_guidance", "t.title", "COALESCE(t.advisor_name,t.user_name)", "t.guidance_type", "CAST(t.status AS CHAR)", "0", "1=1"));
        modules.put("plan-volunteer", spec("cot_highschool.user_volunteer_plan", "t.plan_name", "CAST(t.user_id AS CHAR)", "t.student_type", "CASE WHEN t.is_final=1 THEN '已定稿' ELSE '编辑中' END", "COALESCE(t.score,0)", "t.deleted_at IS NULL"));
        modules.put("plan-major", spec("cot_highschool.gaokao_major", "t.major_name", "t.major_code", "t.category", "CAST(t.status AS CHAR)", "COALESCE(t.tuition_fee,0)", "1=1"));
        modules.put("academic-major", spec("cot_university.uni_major", "t.major_name", "CAST(t.user_id AS CHAR)", "t.degree_type", "CAST(t.status AS CHAR)", "COALESCE(t.total_credits,0)", "1=1"));
        modules.put("academic-course-tree", spec("cot_university.uni_course", "t.course_name", "CAST(t.user_id AS CHAR)", "t.course_type", "CAST(t.status AS CHAR)", "COALESCE(t.total_hours,0)", "t.deleted_at IS NULL"));
        modules.put("academic-student-course", spec("cot_university.uni_student_course", "CONCAT('课程 ',t.course_id)", "CAST(t.user_id AS CHAR)", "t.semester", "t.status", "COALESCE(t.score,0)", "t.deleted_at IS NULL"));
        modules.put("academic-progress", spec("cot_university.uni_graduation_requirement", "CONCAT('毕业要求 ',t.major_id)", "CAST(t.user_id AS CHAR)", "CONCAT('GPA ',COALESCE(t.gpa,0))", "t.status", "COALESCE(t.progress_percent,0)", "1=1"));
        modules.put("academic-gap", modules.get("academic-progress"));
        modules.put("academic-gpa", modules.get("academic-progress"));
        modules.put("academic-cert", timedSpec("cot_profile.resume_certificate", "t.certificate_name", "CAST(t.user_id AS CHAR)", "COALESCE(t.issue_authority,'证书')", "CASE WHEN t.issue_date IS NULL OR t.issue_date<=CURDATE() THEN '有效' ELSE '待取得' END", "COALESCE(t.score,0)", "t.created_at", "t.created_at", "1=1"));
        modules.put("academic-paper", spec("cot_university.thesis_paper", "t.title", "COALESCE(t.supervisor,CAST(t.user_id AS CHAR))", "t.stage", "t.status", "COALESCE(t.version_no,0)", "t.deleted_at IS NULL"));
        modules.put("resource-file", spec("cot_platform.file_asset", "t.original_name", "CAST(t.owner_user_id AS CHAR)", "COALESCE(t.biz_type,t.file_ext)", "CAST(t.status AS CHAR)", "COALESCE(t.file_size,0)", "t.deleted_at IS NULL"));
        modules.put("work-task", spec("cot_workplace.career_task", "t.task_name", "CAST(t.user_id AS CHAR)", "COALESCE(t.priority,'MEDIUM')", "COALESCE(t.status,'TODO')", "COALESCE(t.actual_minutes,0)", "1=1"));
        modules.put("ops-notification", timedSpec("cot_platform.sys_notification", "t.title", "CAST(t.user_id AS CHAR)", "t.notice_type", "CASE WHEN t.dismissed_at IS NOT NULL THEN '已忽略' WHEN t.read_status=1 THEN '已读' ELSE '未读' END", "0", "t.created_at", "t.created_at", "1=1"));
        modules.put("ops-weekly-report", spec("cot_content.growth_weekly_report", "CONCAT(t.week_start,' 至 ',t.week_end)", "CAST(t.user_id AS CHAR)", "'成长周报'", "'已生成'", "0", "1=1"));
        modules.put("chat-group", spec("cot_content.chat_group", "t.name", "CAST(t.owner_id AS CHAR)", "CONCAT('群号 ',t.group_no)", "CASE WHEN t.muted_all=1 THEN '全员禁言' WHEN t.searchable=1 THEN '正常' ELSE '不可搜索' END", "COALESCE(t.member_count,0)", "1=1"));
        modules.put("chat-group-member", timedSpec("cot_content.chat_group_member", "CONCAT('群 ',t.group_id,' / 用户 ',t.user_id)", "CAST(t.user_id AS CHAR)", "t.role", "t.status", "0", "t.joined_at", "COALESCE(t.last_read_at,t.joined_at)", "1=1"));
        modules.put("chat-friend", spec("cot_content.chat_friend", "COALESCE(t.remark,CONCAT('好友 ',t.friend_id))", "CAST(t.user_id AS CHAR)", "CONCAT('好友 ',t.friend_id)", "t.status", "0", "1=1"));
        modules.put("chat-message", timedSpec("cot_content.chat_message", "LEFT(t.content,80)", "CAST(t.sender_id AS CHAR)", "CONCAT(t.conversation_type,' / ',t.content_type)", "CASE WHEN t.recalled_at IS NULL THEN '正常' ELSE '已撤回' END", "0", "t.created_at", "COALESCE(t.recalled_at,t.created_at)", "1=1"));
        modules.put("stage-high", unionSpec("(SELECT CONCAT('INT-',id) id, COALESCE(target_university,'选科意向') title, COALESCE(user_name,CAST(user_id AS CHAR)) owner, '选科意向' category, CAST(status AS CHAR) status, 0 metric, created_at, updated_at FROM cot_highschool.hs_selection_intention UNION ALL SELECT CONCAT('VOL-',id), plan_name, CAST(user_id AS CHAR), '志愿方案', CASE WHEN is_final=1 THEN '已定稿' ELSE '编辑中' END, COALESCE(score,0), created_at, updated_at FROM cot_highschool.user_volunteer_plan WHERE deleted_at IS NULL)"));
        modules.put("stage-university", unionSpec("(SELECT CONCAT('COURSE-',id) id, CONCAT('课程 ',course_id) title, CAST(user_id AS CHAR) owner, '学生课程' category, status, COALESCE(score,0) metric, created_at, updated_at FROM cot_university.uni_student_course WHERE deleted_at IS NULL UNION ALL SELECT CONCAT('PAPER-',id), title, COALESCE(supervisor,CAST(user_id AS CHAR)), '论文', status, COALESCE(version_no,0), created_at, updated_at FROM cot_university.thesis_paper WHERE deleted_at IS NULL)"));
        modules.put("stage-work", unionSpec("(SELECT CONCAT('GOAL-',id) id, goal_name title, CAST(user_id AS CHAR) owner, '职业目标' category, status, progress metric, created_at, updated_at FROM cot_workplace.career_goal UNION ALL SELECT CONCAT('TASK-',id), task_name, CAST(user_id AS CHAR), '职业任务', status, COALESCE(actual_minutes,0), created_at, updated_at FROM cot_workplace.career_task UNION ALL SELECT CONCAT('INT-',id), CONCAT(company_name,' ',position_name), CAST(user_id AS CHAR), '面试准备', status, COALESCE(confidence_score,0), created_at, updated_at FROM cot_workplace.interview_prep)"));
        modules.put("stage-advanced", unionSpec("(SELECT CONCAT('ROAD-',id) id, roadmap_name title, CAST(user_id AS CHAR) owner, '成长路线' category, status, progress metric, created_at, updated_at FROM cot_advanced.advancement_roadmap UNION ALL SELECT CONCAT('MILE-',id), milestone_name, CAST(user_id AS CHAR), '里程碑', status, weight, created_at, updated_at FROM cot_advanced.advancement_milestone UNION ALL SELECT CONCAT('SKILL-',id), skill_name, CAST(user_id AS CHAR), '技能进度', current_level, progress, created_at, updated_at FROM cot_advanced.skill_progress)"));
        return Map.copyOf(modules);
    }

    private static Map<String, CrudSpec> createCrudModules() {
        Map<String, CrudSpec> modules = new LinkedHashMap<>();
        MODULES.forEach((key, spec) -> {
            if (!READ_ONLY_MODULES.contains(key) && !spec.source.startsWith("(")) {
                String softDelete = spec.condition.contains("deleted_at") ? "deleted_at" : null;
                modules.put(key, new CrudSpec(spec.source, "id", softDelete));
            }
        });
        modules.put("work-task", new CrudSpec("cot_workplace.career_task", "id", null));
        modules.put("ops-notification", new CrudSpec("cot_platform.sys_notification", "id", null));
        modules.put("ops-weekly-report", new CrudSpec("cot_content.growth_weekly_report", "id", null));
        modules.put("chat-group", new CrudSpec("cot_content.chat_group", "id", null));
        modules.put("chat-group-member", new CrudSpec("cot_content.chat_group_member", "id", null));
        modules.put("chat-friend", new CrudSpec("cot_content.chat_friend", "id", null));
        modules.put("chat-message", new CrudSpec("cot_content.chat_message", "id", null));
        return Map.copyOf(modules);
    }
    private static ModuleSpec unionSpec(String source) {
        return spec(source, "t.title", "t.owner", "t.category", "t.status", "t.metric", "1=1");
    }
}
