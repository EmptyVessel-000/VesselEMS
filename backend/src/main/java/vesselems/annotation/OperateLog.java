package vesselems.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，标注在 Controller 方法上，AOP 切面会自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {
    /** 所属模块，如 "用户管理"、"角色管理" */
    String module() default "";

    /** 操作内容，如 "新增用户"、"修改配置" */
    String operation() default "";
}