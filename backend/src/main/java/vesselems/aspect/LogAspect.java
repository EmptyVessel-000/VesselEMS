package vesselems.aspect;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import vesselems.annotation.OperateLog;
import vesselems.model.Log;
import vesselems.repository.LogRepository;

@Aspect
@Component
public class LogAspect {

    private final LogRepository logRepository;

    public LogAspect(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        long start = System.currentTimeMillis();

        String username = "";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof String) {
                username = (String) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            } else {
                username = String.valueOf(principal);
            }
        }

        String errorMsg = null;
        int status = 1;
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;

            Log log = new Log();
            log.setUsername(username);
            log.setModule(operateLog.module());
            log.setOperation(operateLog.operation());
            log.setDuration((int) duration);
            log.setStatus(status);
            log.setErrorMsg(errorMsg);
            log.setCreateTime(LocalDateTime.now());

            logRepository.save(log);
        }

        return result;
    }
}