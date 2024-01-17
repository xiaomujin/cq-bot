package com.kuroneko.cqbot.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
//@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 运行时异常
     *
     * @param e 异常
     */
//    @ExceptionHandler(value = Exception.class)
    public void allException(Exception e) {
        log.error(e.getMessage(), e);
    }

}
