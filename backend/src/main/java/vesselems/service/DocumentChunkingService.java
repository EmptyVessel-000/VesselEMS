package vesselems.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

@Service
public class DocumentChunkingService {

    private static final int CHUNK_SIZE = 512;
    private static final int OVERLAP = 50;

    private final Tika tika = new Tika();

    public List<String> chunk(byte[] fileBytes, String fileType) {
        try (InputStream in = new ByteArrayInputStream(fileBytes)) {
            String text = tika.parseToString(in);
            List<String> chunks = new ArrayList<>();
            if (text == null || text.isEmpty()) {
                return chunks;
            }

            int start = 0;
            while (start < text.length()) {
                int end = Math.min(start + CHUNK_SIZE, text.length());
                chunks.add(text.substring(start, end));
                start = end - OVERLAP;
                if (start >= text.length() || start + OVERLAP >= text.length()) {
                    break;
                }
            }
            return chunks;
        } catch (Exception e) {
            throw new RuntimeException("文档解析失败: " + e.getMessage(), e);
        }
    }
}