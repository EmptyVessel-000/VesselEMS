package vesselems.aspect;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import vesselems.annotation.OperateLog;
import vesselems.model.Log;
import vesselems.repository.UserRepository;
import vesselems.service.LogService;

@Aspect
@Component
public class LogAspect {

    private final LogService logService;
    private final UserRepository userRepository;

    public LogAspect(LogService logService, UserRepository userRepository) {
        this.logService = logService;
        this.userRepository = userRepository;
    }

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        long start = System.currentTimeMillis();

        String username = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long userId) {
            username = userRepository.findById(userId)
                    .map(u -> u.getUsername())
                    .orElse("");
        }

        String module = operateLog.module();
        String operation = operateLog.operation();

        Throwable error = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;

            Log log = new Log();
            log.setUsername(username);
            log.setModule(module);
            log.setOperation(operation);
            log.setDuration((int) duration);
            log.setStatus(error == null ? 1 : 0);
            log.setErrorMsg(error != null ? error.getMessage() : null);
            log.setCreateTime(LocalDateTime.now());

            logService.record(log);
        }

        return result;
    }
}