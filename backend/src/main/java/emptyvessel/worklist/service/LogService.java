package emptyvessel.worklist.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import emptyvessel.worklist.model.Log;
import emptyvessel.worklist.repository.LogRepository;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public Log record(Log log) {
        log.setCreateTime(LocalDateTime.now());
        log.setStatus(log.getStatus() != null ? log.getStatus() : 1);
        return logRepository.save(log);
    }

    public List<Log> listLogs() {
        return logRepository.findAll();
    }

    public Log getLogById(Long id) {
        return logRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("日志不存在: " + id));
    }

    public List<Log> findByUserId(Long userId) {
        return logRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public List<Log> findByModule(String module) {
        return logRepository.findByModuleOrderByCreateTimeDesc(module);
    }

    public List<Log> findByOperation(String operation) {
        return logRepository.findByOperationOrderByCreateTimeDesc(operation);
    }

    public List<Log> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return logRepository.findByCreateTimeBetweenOrderByCreateTimeDesc(start, end);
    }

    public void cleanOldLogs(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<Log> oldLogs = logRepository.findByCreateTimeBetweenOrderByCreateTimeDesc(
                LocalDateTime.of(2000, 1, 1, 0, 0), cutoff);
        logRepository.deleteAll(oldLogs);
    }
}