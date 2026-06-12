package vesselems.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.pgvector.PGvector;

@Service
public class VectorStoreService {

    private final JdbcTemplate jdbc;

    public VectorStoreService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Long annotationId, float[] embedding) {
        String sql = "UPDATE annotation SET embedding = ?::vector WHERE id = ?";
        jdbc.update(sql, new PGvector(embedding), annotationId);
    }

    public List<SearchResult> search(float[] queryEmbedding, Long libraryId, int topK) {
        String sql = """
            SELECT a.id, a.content,
                   1 - (a.embedding <=> ?::vector) AS similarity
            FROM annotation a
            JOIN document d ON d.id = a.document_id
            WHERE d.library_id = ? AND a.embedding IS NOT NULL
            ORDER BY a.embedding <=> ?::vector
            LIMIT ?
            """;

        PGvector queryVec = new PGvector(queryEmbedding);
        return jdbc.query(sql,
                new SearchResultRowMapper(),
                queryVec, libraryId, queryVec, topK);
    }

    public void deleteByDocumentId(Long documentId) {
        String sql = "UPDATE annotation SET embedding = NULL WHERE document_id = ?";
        jdbc.update(sql, documentId);
    }

    public static class SearchResult {
        private Long annotationId;
        private String content;
        private double similarity;

        public SearchResult(Long annotationId, String content, double similarity) {
            this.annotationId = annotationId;
            this.content = content;
            this.similarity = similarity;
        }

        public Long getAnnotationId() { return annotationId; }
        public String getContent() { return content; }
        public double getSimilarity() { return similarity; }
    }

    private static class SearchResultRowMapper implements RowMapper<SearchResult> {
        @Override
        public SearchResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SearchResult(
                    rs.getLong("id"),
                    rs.getString("content"),
                    rs.getDouble("similarity")
            );
        }
    }
}