package com.kuroneko.cqbot.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
//@Aspect
//@Component
public class PluginAspect {

    @Pointcut("execution(* com.mikuac.shiro.handler.EventHandler.handler(..))")
    private void pointcut() {
    }

    @AfterThrowing(value = "pointcut()", throwing = "e")
    private void throwableHandler(JoinPoint joinPoint, Throwable e) {
        log.error("捕获到异常", e);
//        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//        Method method = methodSignature.getMethod();
//        String methodName = method.getName();
//        Object[] arguments = joinPoint.getArgs();
//        Bot bot = CastUtils.cast(arguments[0]);
//        MsgUtils msg1 = MsgUtils.builder().text(e.getMessage());
//        String stStr = Arrays.stream(e.getStackTrace()).limit(4).map(StackTraceElement::toString).collect(Collectors.joining(Constant.XN));
//        MsgUtils msg2 = MsgUtils.builder().text(stStr);
//        List<String> list = Arrays.asList(msg1.build(), msg2.build());
//        List<Map<String, Object>> mapList = ShiroUtils.generateForwardMsg(bot.getSelfId(), "出错了", list);
//        bot.sendPrivateForwardMsg(1419229777L, mapList);
    }
}
