package com.github.epsilon.logging;

/**
 * Logger 提供者
 * 由 common 层在启动时注入具体实现
 */
public class LoggerProvider {
    private static ILogger instance;

    public static void setLogger(ILogger logger) {
        instance = logger;
    }

    public static ILogger getLogger() {
        if (instance == null) {
            // 降级到 System.err，避免 NPE
            return new ILogger() {
                @Override
                public void error(String message, Object... args) {
                    System.err.printf("[ERROR] " + message + "%n", args);
                }

                @Override
                public void warn(String message, Object... args) {
                    System.err.printf("[WARN] " + message + "%n", args);
                }

                @Override
                public void info(String message, Object... args) {
                    System.out.printf("[INFO] " + message + "%n", args);
                }

                @Override
                public void debug(String message, Object... args) {
                    System.out.printf("[DEBUG] " + message + "%n", args);
                }
            };
        }
        return instance;
    }
}
