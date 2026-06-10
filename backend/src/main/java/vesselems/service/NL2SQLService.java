package vesselems.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import vesselems.model.Datasource;
import vesselems.model.Dialog;
import vesselems.model.Model;
import vesselems.model.Session;
import vesselems.repository.DatasourceRepository;
import vesselems.repository.DialogRepository;
import vesselems.repository.ModelRepository;
import vesselems.repository.SessionRepository;

@Service
public class NL2SQLService {

    private static final Pattern WRITE_SQL = Pattern.compile(
            "\\b(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE)\\b",
            Pattern.CASE_INSENSITIVE);

    private final DatasourceRepository dsRepo;
    private final ModelRepository modelRepo;
    private final DialogRepository dialogRepo;
    private final SessionRepository sessionRepo;
    private final DSManager dsManager;
    private final SchemaService schemaService;
    private final LLMService llmService;

    public NL2SQLService(DatasourceRepository dsRepo, ModelRepository modelRepo,
            DialogRepository dialogRepo, SessionRepository sessionRepo,
            DSManager dsManager, SchemaService schemaService, LLMService llmService) {
        this.dsRepo = dsRepo;
        this.modelRepo = modelRepo;
        this.dialogRepo = dialogRepo;
        this.sessionRepo = sessionRepo;
        this.dsManager = dsManager;
        this.schemaService = schemaService;
        this.llmService = llmService;
    }

    public Map<String, Object> query(String sessionId, Long dsId, Long modelId, String question) {
        Datasource ds = dsRepo.findById(dsId)
                .orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
        Model model = modelRepo.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在"));

        boolean isNew = (sessionId == null || sessionId.isEmpty());
        if (isNew) {
            sessionId = UUID.randomUUID().toString();
        }

        // Save/update session tracking
        Session session = sessionRepo.findBySessionId(sessionId).orElse(null);
        if (session == null) {
            session = new Session();
            session.setSessionId(sessionId);
            session.setDatasourceId(dsId);
            session.setModelId(modelId);
            session.setCreateTime(LocalDateTime.now());
        } else {
            session.setDatasourceId(dsId);
            session.setModelId(modelId);
        }
        sessionRepo.save(session);

        DataSource dataSource = dsManager.get(ds);
        List<Map<String, Object>> schema = schemaService.getSchema(dataSource);
        String schemaStr = schemaToString(schema);

        List<Map<String, String>> messages = buildMessages(sessionId, schemaStr, question);

        String llmResponse = llmService.chatMulti(model, messages);
        String sql = extractSQL(llmResponse);
        boolean isWrite = WRITE_SQL.matcher(sql).find();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("question", question);
        result.put("sql", sql);

        if (isWrite) {
            result.put("needConfirm", true);
            result.put("message", "该SQL包含写操作（INSERT/UPDATE/DELETE等），请确认后执行");

            Dialog dialog = saveDialog(sessionId, dsId, modelId,
                    Map.of("question", question, "sql", sql, "status", "pending"));
            result.put("dialogId", dialog.getId());
        } else {
            try {
                List<Map<String, Object>> rows = executeSQL(dataSource, sql);
                result.put("needConfirm", false);
                result.put("result", rows);

                Map<String, Object> content = new LinkedHashMap<>();
                content.put("question", question);
                content.put("sql", sql);
                content.put("result", rows);
                content.put("status", "success");

                Dialog dialog = saveDialog(sessionId, dsId, modelId, content);
                result.put("dialogId", dialog.getId());
            } catch (Exception e) {
                result.put("needConfirm", false);
                result.put("error", e.getMessage());
                result.put("result", List.of());

                Map<String, Object> content = new LinkedHashMap<>();
                content.put("question", question);
                content.put("sql", sql);
                content.put("error", e.getMessage());
                content.put("status", "error");

                Dialog dialog = saveDialog(sessionId, dsId, modelId, content);
                result.put("dialogId", dialog.getId());
            }
        }

        return result;
    }

    public Map<String, Object> execute(Long dialogId) {
        Dialog dialog = dialogRepo.findById(dialogId)
                .orElseThrow(() -> new IllegalArgumentException("记录不存在"));

        Datasource ds = dsRepo.findById(dialog.getDatasourceId())
                .orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
        DataSource dataSource = dsManager.get(ds);

        String sql = extractJsonField(dialog.getContent(), "sql");

        try {
            List<Map<String, Object>> rows = executeSQL(dataSource, sql);

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("question", extractJsonField(dialog.getContent(), "question"));
            content.put("sql", sql);
            content.put("result", rows);
            content.put("status", "success");
            dialog.setContent(toJsonStr(content));
            dialogRepo.save(dialog);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("sessionId", dialog.getSessionId());
            result.put("dialogId", dialogId);
            result.put("sql", sql);
            result.put("result", rows);
            return result;
        } catch (Exception e) {
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("question", extractJsonField(dialog.getContent(), "question"));
            content.put("sql", sql);
            content.put("error", e.getMessage());
            content.put("status", "error");
            dialog.setContent(toJsonStr(content));
            dialogRepo.save(dialog);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("sessionId", dialog.getSessionId());
            result.put("dialogId", dialogId);
            result.put("sql", sql);
            result.put("error", e.getMessage());
            return result;
        }
    }

    public List<Map<String, Object>> getSessions() {
        List<Object[]> rows = dialogRepo.findSessions();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            String sid = (String) r[0];
            m.put("sessionId", sid);
            m.put("count", r[1]);
            m.put("firstTime", r[2] != null ? r[2].toString() : "");
            String firstQuestion = r[3] != null ? r[3].toString() : "";
            try {
                firstQuestion = extractJsonField(firstQuestion, "question");
            } catch (Exception ignore) {
            }
            m.put("firstQuestion", firstQuestion);

            // Attach dsId/modelId from session table
            sessionRepo.findBySessionId(sid).ifPresent(s -> {
                m.put("datasourceId", s.getDatasourceId());
                m.put("modelId", s.getModelId());
            });
            list.add(m);
        }
        return list;
    }

    public String summary(String sessionId, Long modelId) {
        Model model = modelRepo.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在"));
        List<Dialog> dialogs = dialogRepo.findBySessionIdOrderByCreateTimeAsc(sessionId);
        if (dialogs.isEmpty()) {
            return "该会话无对话记录。";
        }

        StringBuilder conversation = new StringBuilder();
        int idx = 0;
        final int MAX_PROMPT = 15000;
        for (Dialog d : dialogs) {
            idx++;
            String q = extractJsonField(d.getContent(), "question");
            String s = extractJsonField(d.getContent(), "sql");
            String err = extractJsonField(d.getContent(), "error");
            String result = extractJsonField(d.getContent(), "result");

            StringBuilder entry = new StringBuilder();
            entry.append("## 查询 ").append(idx).append("\n");
            entry.append("- 问题: ").append(q).append("\n");
            entry.append("- SQL: ").append(s).append("\n");
            if (err != null && !err.isEmpty()) {
                entry.append("- 错误: ").append(err).append("\n");
            }
            if (result != null && !result.isEmpty() && !"null".equals(result)) {
                // Limit per-entry result to 1500 chars to avoid token explosion
                String truncated = result.length() > 1500
                        ? result.substring(0, 1500) + "...(共约" + result.length() + "字符)"
                        : result;
                entry.append("- 查询结果数据:\n```json\n").append(truncated).append("\n```\n");
            }
            entry.append("\n");

            // Global truncation: stop adding entries if total would exceed limit
            if (conversation.length() + entry.length() > MAX_PROMPT) {
                conversation.append("\n...(后续").append(dialogs.size() - idx + 1).append("轮对话已截断)\n");
                break;
            }
            conversation.append(entry);
        }

        String prompt = """
                你是数据分析助手。请根据以下SQL查询对话记录生成结构化的Markdown分析报告，必须包含以下部分：
                1. **查询概要** — 用户问了什么类型的问题，涉及哪些业务领域
                2. **涉及的表和字段** — 列出所有被查询的表及其关键字段
                3. **关键SQL列表** — 逐条列出SQL语句
                4. **数据结果分析** — 对每条查询返回的实际数据进行解读：数据量、关键指标、趋势、异常值等。如果数据中包含数值字段，请计算总数、平均值等统计指标。如果包含分类字段，请分析分布情况
                5. **综合结论与建议** — 基于全部查询结果，给出总结性结论和可行性建议

                对话记录：
                %s

                请生成Markdown报告：""".formatted(conversation.toString());

        return llmService.chat(model, prompt);
    }

    public List<Dialog> getSessionDialogs(String sessionId) {
        return dialogRepo.findBySessionIdOrderByCreateTimeAsc(sessionId);
    }

    private List<Map<String, String>> buildMessages(String sessionId, String schemaStr, String question) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                "你是SQL专家。根据以下数据库表结构生成SQL。只输出SQL，不要解释和代码块。\n" + schemaStr));

        List<Dialog> history = dialogRepo.findBySessionIdOrderByCreateTimeAsc(sessionId);
        for (Dialog d : history) {
            String q = extractJsonField(d.getContent(), "question");
            String s = extractJsonField(d.getContent(), "sql");
            String err = extractJsonField(d.getContent(), "error");
            messages.add(Map.of("role", "user", "content", q));
            if (err != null && !err.isEmpty()) {
                messages.add(Map.of("role", "assistant", "content",
                        "SQL: " + s + "\n错误: " + err));
            } else {
                messages.add(Map.of("role", "assistant", "content", s));
            }
        }
        messages.add(Map.of("role", "user", "content", question));
        return messages;
    }

    private String extractSQL(String llmResponse) {
        String sql = llmResponse.trim();
        if (sql.startsWith("```sql"))
            sql = sql.substring(6);
        else if (sql.startsWith("```"))
            sql = sql.substring(3);
        if (sql.endsWith("```"))
            sql = sql.substring(0, sql.length() - 3);
        return sql.trim();
    }

    private List<Map<String, Object>> executeSQL(DataSource ds, String sql) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = ds.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                rows.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
        }
        return rows;
    }

    private Dialog saveDialog(String sessionId, Long dsId, Long modelId, Map<String, Object> content) {
        Dialog d = new Dialog();
        d.setSessionId(sessionId);
        d.setDatasourceId(dsId);
        d.setModelId(modelId);
        d.setContent(toJsonStr(content));
        d.setCreateTime(LocalDateTime.now());
        return dialogRepo.save(d);
    }

    private String schemaToString(List<Map<String, Object>> schema) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> table : schema) {
            sb.append("表: ").append(table.get("table")).append("\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cols = (List<Map<String, Object>>) table.get("columns");
            for (Map<String, Object> col : cols) {
                sb.append("  - ").append(col.get("name"))
                        .append(" (").append(col.get("type")).append(")\n");
            }
        }
        return sb.toString();
    }

    private String extractJsonField(String json, String key) {
        if (json == null)
            return "";
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0)
            return "";
        start += search.length();
        while (start < json.length() && json.charAt(start) == ' ')
            start++;
        if (start >= json.length())
            return "";
        char c = json.charAt(start);
        if (c == '"') {
            int end = json.indexOf('"', start + 1);
            return end > start ? json.substring(start + 1, end).replace("\\\"", "\"") : "";
        }
        if (c == '[' || c == '{') {
            int depth = 0;
            for (int i = start; i < json.length(); i++) {
                if (json.charAt(i) == '[' || json.charAt(i) == '{')
                    depth++;
                else if (json.charAt(i) == ']' || json.charAt(i) == '}')
                    depth--;
                if (depth == 0)
                    return json.substring(start, i + 1);
            }
            return "";
        }
        int end = start;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}')
            end++;
        return json.substring(start, end).trim();
    }

    private String toJsonStr(Object obj) {
        if (obj == null)
            return "null";
        if (obj instanceof String s) {
            return "\"" + escape(s) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (i++ > 0)
                    sb.append(",");
                sb.append("\"").append(escape(String.valueOf(e.getKey()))).append("\":");
                sb.append(toJsonStr(e.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0)
                    sb.append(",");
                sb.append(toJsonStr(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(obj.toString()) + "\"";
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}