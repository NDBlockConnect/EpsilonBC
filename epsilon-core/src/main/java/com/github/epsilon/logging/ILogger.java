package com.github.epsilon.logging;

/**
 * 日志接口抽象
 * 由 common 层提供具体实现
 */
public interface ILogger {
    void error(String message, Object... args);

    void warn(String message, Object... args);

    void info(String message, Object... args);

    void debug(String message, Object... args);
}
