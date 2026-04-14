package com.tydic.filehub.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@Slf4j
public class TraceIdAspect {
    @Around("execution(* com.tydic.filehub.business..*.*(..))") // 根据实际包路径调整
    public Object around(ProceedingJoinPoint point) throws Throwable {
        boolean traceIdGenerated = false;  // 添加标记
        try {
            // 如果当前没有traceId则生成一个
            if (MDC.get("traceId") == null) {
                String traceId = generateTraceId();
                MDC.put("traceId", traceId);
                traceIdGenerated = true;  // 标记为本切面生成
                log.debug("方法[{}]新生成traceId: {}", point.getSignature().toShortString(), traceId);
            }

            return point.proceed();
        } finally {
            // 只清理本切面生成的traceId
            if (traceIdGenerated) {
                MDC.remove("traceId");
                log.debug("方法[{}]清理traceId", point.getSignature().toShortString());
            }
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
