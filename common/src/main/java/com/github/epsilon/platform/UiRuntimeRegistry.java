package com.github.epsilon.platform;

/**
 * EpsilonShot 平台注册表（OpenLumin LuminShot PlatformRegistry 同构）。
 *
 * <p>持有当前活跃的 {@link UiRuntime}；未注册任何实现时回退到内建
 * {@link InternalUiRuntime}，保证调用面永远可用。</p>
 */
public final class UiRuntimeRegistry {

    private static volatile UiRuntime active;
    private static volatile boolean explicit;

    private UiRuntimeRegistry() {
    }

    /** 当前运行时；未显式注册时返回内建单例。 */
    public static UiRuntime current() {
        UiRuntime runtime = active;
        if (runtime != null) return runtime;
        return InternalUiRuntime.get();
    }

    /** 当前运行时；未显式注册时返回 null（上游 currentOrNull 语义）。 */
    public static UiRuntime currentOrNull() {
        if (!explicit) return null;
        return active;
    }

    /**
     * 注册当前运行时。传 null 恢复为"无显式运行时"状态
     * （current() 回退内建，currentOrNull() 返回 null）。
     */
    public static void set(UiRuntime runtime) {
        if (runtime == null) {
            explicit = false;
            active = null;
            return;
        }
        explicit = true;
        active = runtime;
    }
}
