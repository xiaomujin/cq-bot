package com.kuroneko.cqbot.core.func;

import java.lang.FunctionalInterface;

@FunctionalInterface
public interface TConsumer<T, U, I> {

    void accept(T t, U u, I i);
}
