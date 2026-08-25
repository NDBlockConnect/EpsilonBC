package com.github.epsilon.scripting.lua.render;

import com.github.epsilon.Constants;
import com.github.epsilon.events.bus.EventBus;
import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.Render2DEvent;
import com.github.epsilon.gui.lib.UiTree;
import com.github.epsilon.gui.lib.scene.UiLayer;
import com.github.epsilon.gui.lib.scene.UiScene;
import com.github.epsilon.gui.theme.EpsilonUiTheme;
import com.github.epsilon.scripting.lua.event.LuaEventListener;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lua 2D 渲染服务。
 * <p>
 * 上游实现依赖外部 Lumin Graphics 运行时；此处适配为内建
 * {@link UiScene} 帧提交管线，HUD 树由 HudElementHolder 注入，
 * Level 树在本服务自己的场景中独立提交。
 */
public final class LuaRender2DService implements AutoCloseable {
    public static final LuaRender2DService INSTANCE = new LuaRender2DService();
    private final CopyOnWriteArrayList<LuaEventListener> hudListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<LuaEventListener> levelListeners = new CopyOnWriteArrayList<>();
    private UiScene levelScene;

    private LuaRender2DService() {
        EventBus.INSTANCE.subscribe(this);
    }

    public void register(LuaEventListener listener) {
        CopyOnWriteArrayList<LuaEventListener> target = listener.getTarget() == Render2DEvent.HUD.class
                ? hudListeners : levelListeners;
        target.addIfAbsent(listener);
        target.sort(Comparator.comparingInt(LuaEventListener::getPriority).reversed());
    }

    public void unregister(LuaEventListener listener) {
        hudListeners.remove(listener);
        levelListeners.remove(listener);
    }

    public void appendHud(UiTree.Scope scope, Render2DEvent.HUD event) {
        append(scope, event, hudListeners);
    }

    @EventHandler(priority = -500)
    private void onLevel(Render2DEvent.Level event) {
        if (levelListeners.isEmpty()) return;
        try {
            UiTree.Scope scope = new UiTree.Scope();
            append(scope, event, levelListeners);
            UiTree tree = UiTree.from(scope);
            if (tree.nodeCount() > 0) {
                UiScene scene = scene();
                scene.beginFrame();
                scene.submit(UiLayer.CONTENT, 0, tree);
                scene.endFrame();
            }
        } catch (RuntimeException failure) {
            releaseScene(failure);
            Constants.LOGGER.error("Lua Level 2D frame failed", failure);
        }
    }

    private void append(UiTree.Scope scope, Render2DEvent event,
                        CopyOnWriteArrayList<LuaEventListener> listeners) {
        for (LuaEventListener listener : listeners) {
            try {
                scope.layer(0, child -> listener.callUi(child, event));
            } catch (Throwable failure) {
                Constants.LOGGER.error("Lua 2D callback 失败: {}", listener.runtimeId(), failure);
            }
        }
    }

    private UiScene scene() {
        if (levelScene == null) {
            levelScene = new UiScene(EpsilonUiTheme.INSTANCE);
        }
        return levelScene;
    }

    private void releaseScene() {
        UiScene previous = levelScene;
        levelScene = null;
        if (previous != null) previous.close();
    }

    private void releaseScene(RuntimeException frameFailure) {
        UiScene previous = levelScene;
        levelScene = null;
        if (previous == null) return;
        try {
            previous.close();
        } catch (RuntimeException cleanupFailure) {
            frameFailure.addSuppressed(cleanupFailure);
        }
    }

    @Override
    public void close() {
        hudListeners.clear();
        levelListeners.clear();
        releaseScene();
    }
}
