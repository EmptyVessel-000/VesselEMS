package vesselems.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

@Service
public class SchemaService {

    public List<Map<String, Object>> getSchema(DataSource dataSource) {
        List<Map<String, Object>> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String catalog = conn.getCatalog();
            String schema = conn.getSchema();

            try (ResultSet rs = meta.getTables(catalog, schema, "%", new String[] { "TABLE" })) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    Map<String, Object> table = new LinkedHashMap<>();
                    table.put("table", tableName);
                    table.put("columns", getColumns(meta, catalog, schema, tableName));
                    tables.add(table);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("读取表结构失败: " + e.getMessage(), e);
        }
        return tables;
    }

    private List<Map<String, Object>> getColumns(DatabaseMetaData meta, String catalog, String schema,
            String tableName) throws SQLException {
        List<Map<String, Object>> cols = new ArrayList<>();
        try (ResultSet rs = meta.getColumns(catalog, schema, tableName, "%")) {
            while (rs.next()) {
                Map<String, Object> col = new LinkedHashMap<>();
                col.put("name", rs.getString("COLUMN_NAME"));
                col.put("type", rs.getString("TYPE_NAME"));
                col.put("size", rs.getInt("COLUMN_SIZE"));
                cols.add(col);
            }
        }
        return cols;
    }
}