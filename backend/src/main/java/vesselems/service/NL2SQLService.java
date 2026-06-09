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

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import vesselems.model.Datasource;
import vesselems.model.Dialog;
import vesselems.model.Model;
import vesselems.repository.DatasourceRepository;
import vesselems.repository.DialogRepository;
import vesselems.repository.ModelRepository;

@Service
public class NL2SQLService {

    private final DatasourceRepository dsRepo;
    private final ModelRepository modelRepo;
    private final DialogRepository dialogRepo;
    private final DSManager dsManager;
    private final SchemaService schemaService;
    private final LLMService llmService;

    public NL2SQLService(DatasourceRepository dsRepo, ModelRepository modelRepo,
            DialogRepository dialogRepo, DSManager dsManager,
            SchemaService schemaService, LLMService llmService) {
        this.dsRepo = dsRepo;
        this.modelRepo = modelRepo;
        this.dialogRepo = dialogRepo;
        this.dsManager = dsManager;
        this.schemaService = schemaService;
        this.llmService = llmService;
    }

    public Map<String, Object> query(Long dsId, Long modelId, String question) {
        Datasource ds = dsRepo.findById(dsId)
                .orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
        Model model = modelRepo.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在"));

        DataSource dataSource = dsManager.get(ds);
        List<Map<String, Object>> schema = schemaService.getSchema(dataSource);
        String schemaStr = schemaToString(schema);

        String prompt = buildPrompt(schemaStr, question);
        String llmResponse = llmService.chat(model, prompt);
        String sql = extractSQL(llmResponse);

        List<Map<String, Object>> result = executeSQL(dataSource, sql);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("question", question);
        content.put("sql", sql);
        content.put("result", result);

        Dialog dialog = new Dialog();
        dialog.setDatasourceId(dsId);
        dialog.setModelId(modelId);
        dialog.setContent(toJsonStr(content));
        dialog.setCreateTime(LocalDateTime.now());
        dialogRepo.save(dialog);

        content.put("dialogId", dialog.getId());
        return content;
    }

    private String buildPrompt(String schemaStr, String question) {
        return "你是一个SQL专家。根据以下数据库表结构，将用户的自然语言问题转换为SQL查询语句。\n"
                + "只返回SQL语句，不要任何解释，不要markdown代码块标记。\n\n"
                + "数据库表结构：\n" + schemaStr + "\n\n"
                + "用户问题：" + question;
    }

    private String extractSQL(String llmResponse) {
        String sql = llmResponse.trim();
        if (sql.startsWith("```sql")) {
            sql = sql.substring(6);
        }
        if (sql.startsWith("```")) {
            sql = sql.substring(3);
        }
        if (sql.endsWith("```")) {
            sql = sql.substring(0, sql.length() - 3);
        }
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