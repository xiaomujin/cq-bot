package com.kuroneko.cqbot.core;

import com.kuroneko.cqbot.core.annotation.BotHandler;
import com.kuroneko.cqbot.core.annotation.BotMsgHandler;
import com.kuroneko.cqbot.core.dto.HandlerMethod;
import com.kuroneko.cqbot.core.dto.MsgInfo;
import com.kuroneko.cqbot.enums.sysPluginRegex;
import com.kuroneko.cqbot.utils.RegexUtil;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.regex.Matcher;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotPluginHandler implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final MultiValueMap<sysPluginRegex, HandlerMethod> annotationHandler = new LinkedMultiValueMap<>();


    @Override
    public void run(ApplicationArguments args) {
        initAnnotationHandler();
    }

    /**
     * 事件分发
     */
    public void handler(sysPluginRegex value, MsgInfo msgInfo, Bot bot, AnyMessageEvent event) {
        Optional.ofNullable(annotationHandler.get(value)).ifPresent(methods -> methods.forEach(method -> {
            BotMsgHandler annotation = method.getMethod().getAnnotation(BotMsgHandler.class);
            String cmd = annotation.cmd();
            Optional<Matcher> matcher = RegexUtil.matcher(cmd, msgInfo.getMsg());
            if (matcher.isEmpty()) {
                return;
            }
            Map<Class<?>, Object> params = new HashMap<>();
            params.put(MsgInfo.class, msgInfo);
            params.put(Bot.class, bot);
            params.put(AnyMessageEvent.class, event);
            params.put(Matcher.class, matcher.get());
            invokeMethod(method, params);
        }));
    }

    private void invokeMethod(HandlerMethod method, Map<Class<?>, Object> params) {
        Class<?>[] types = method.getMethod().getParameterTypes();
        Object[] objects = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            objects[i] = params.get(type);
        }
//        Arrays.stream(types).forEach(consumerWithIndex((item, index) -> {
//            if (params.containsKey(item)) {
//                objects[index] = params.remove(item);
//                return;
//            }
//            objects[index] = null;
//        }));
        try {
            method.getMethod().invoke(method.getObject(), objects);
        } catch (Exception e) {
            log.error("Invoke method exception: {}", e.getMessage(), e);
        }
    }

//    private <T> Consumer<T> consumerWithIndex(ObjIntConsumer<T> consumer) {
//        class O {
//            int i;
//        }
//        O object = new O();
//        return item -> {
//            int index = object.i++;
//            consumer.accept(item, index);
//        };
//    }

    private void initAnnotationHandler() {
        log.debug("Starting to collect beans with @BotHandler annotation");
        Map<String, Object> beans = new HashMap<>(applicationContext.getBeansWithAnnotation(BotHandler.class));
        log.debug("Found {} beans with @BotHandler annotation", beans.size());

        beans.values().forEach(obj -> {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(obj);
            log.debug("Processing class: {}", targetClass.getName());
            Arrays.stream(targetClass.getMethods()).forEach(method -> {
                HandlerMethod handlerMethod = new HandlerMethod();
                handlerMethod.setMethod(method);
                handlerMethod.setType(targetClass);
                handlerMethod.setObject(obj);
                log.debug("Processing method: {}.{}", targetClass.getSimpleName(), method.getName());
                BotMsgHandler anno = method.getAnnotation(BotMsgHandler.class);
                if (anno != null) {
                    annotationHandler.add(anno.model(), handlerMethod);
                }
            });
        });
        log.debug("Starting handler method sorting");
        log.debug("Handler methods sorted, total size: {}", annotationHandler.size());
    }

}
