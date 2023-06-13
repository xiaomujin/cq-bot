package com.kuroneko.cqbot.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PluginAspect {

    @Pointcut("execution(* com.mikuac.shiro.handler.injection.InjectionHandler.*(..)) || execution(* com.mikuac.shiro.handler.EventHandler.handler(..))")
    private void pointcut() {
    }

    @Around("pointcut()")
    private Object logHandler(ProceedingJoinPoint pjp) {
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
