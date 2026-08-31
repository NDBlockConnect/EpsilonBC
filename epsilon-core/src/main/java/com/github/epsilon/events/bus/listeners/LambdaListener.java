package com.github.epsilon.events.bus.listeners;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.logging.LoggerProvider;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

/**
 * Default implementation of a {@link IListener} that creates a lambda at runtime to call the target method.
 */
public class LambdaListener implements IListener {

    public interface Factory {
        MethodHandles.Lookup create(Method lookupInMethod, Class<?> klass) throws InvocationTargetException, IllegalAccessException;
    }

    private static Method privateLookupInMethod;

    private final Class<?> target;
    private final boolean isStatic;
    private final int priority;
    private final String description;
    private Consumer<Object> executor;

    /**
     * Creates a new lambda listener, can be used for both static and non-static methods.
     *
     * @param klass  Class of the object
     * @param object Object, null if static
     * @param method Method to create lambda for
     */
    @SuppressWarnings("unchecked")
    public LambdaListener(Factory factory, Class<?> klass, Object object, Method method) {
        this.target = method.getParameters()[0].getType();
        this.isStatic = Modifier.isStatic(method.getModifiers());
        this.priority = method.getAnnotation(EventHandler.class).priority();
        this.description = klass.getSimpleName() + "#" + method.getName();

        try {
            String name = method.getName();
            MethodHandles.Lookup lookup = factory.create(privateLookupInMethod, klass);

            MethodType methodType = MethodType.methodType(void.class, method.getParameters()[0].getType());

            MethodHandle methodHandle;
            MethodType invokedType;

            if (isStatic) {
                methodHandle = lookup.findStatic(klass, name, methodType);
                invokedType = MethodType.methodType(Consumer.class);
            } else {
                methodHandle = lookup.findVirtual(klass, name, methodType);
                invokedType = MethodType.methodType(Consumer.class, klass);
            }

            MethodHandle lambdaFactory = LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType(void.class, Object.class), methodHandle, methodType).getTarget();

            if (isStatic) this.executor = (Consumer<Object>) lambdaFactory.invoke();
            else this.executor = (Consumer<Object>) lambdaFactory.invoke(object);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to create event listener for " + klass.getName() + "#" + method.getName(), throwable);
        }
    }

    // 异常节流：同一 handler 每 5 秒最多打一条完整栈，避免每 tick 抛异常刷爆日志
    private long lastErrorLogMs;
    private long suppressedErrorCount;

    @Override
    public void call(Object event) {
        try {
            executor.accept(event);
        } catch (Throwable throwable) {
            // 异常隔离：单个 handler 抛异常绝不能中断 EventBus.post 循环，
            // 否则同一事件里优先级更低的所有模块 handler 都会被跳过（大面积功能失效）。
            handleException(throwable);
        }
    }

    private void handleException(Throwable throwable) {
        long now = System.currentTimeMillis();
        if (now - lastErrorLogMs >= 5_000L) {
            if (suppressedErrorCount > 0L) {
                LoggerProvider.getLogger().error("Event handler {} threw an exception (+{} more suppressed in the last 5s)",
                        description, suppressedErrorCount, throwable);
            } else {
                LoggerProvider.getLogger().error("Event handler {} threw an exception", description, throwable);
            }
            lastErrorLogMs = now;
            suppressedErrorCount = 0L;
        } else {
            suppressedErrorCount++;
        }
    }

    @Override
    public Class<?> getTarget() {
        return target;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    static {
        try {
            privateLookupInMethod = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            LoggerProvider.getLogger().warn("Failed to initialize LambdaListener reflection", e);
        }
    }

}
