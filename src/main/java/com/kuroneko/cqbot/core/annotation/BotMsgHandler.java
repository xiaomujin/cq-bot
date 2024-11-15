package com.kuroneko.cqbot.core.annotation;

import com.kuroneko.cqbot.enums.sysPluginRegex;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotMsgHandler {
    String cmd() default "";

    sysPluginRegex model() default sysPluginRegex.OTHER;
}
