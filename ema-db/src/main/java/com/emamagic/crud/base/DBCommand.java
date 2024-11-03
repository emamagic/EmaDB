package com.emamagic.crud.base;

public interface DBCommand<T, R> {
    R execute(T entity);
}
