package emptyvessel.worklist.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import emptyvessel.worklist.common.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(400, "字段校验失败"));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(400, ex.getMessage()));
        }

        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        public ResponseEntity<ApiResponse<?>> handleAccessDenied(
                        org.springframework.security.access.AccessDeniedException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(403, "权限不足"));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(500, "服务器内部错误"));
        }
}