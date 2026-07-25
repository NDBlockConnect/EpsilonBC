package com.github.epsilon.gui.dropdown.component;

import com.github.epsilon.gui.lib.UiTextMetrics;
import com.github.epsilon.gui.lib.UiTree;

public interface DropdownPanel {

    String getId();

    void startIntro();

    default void beginRenderFrame(int frameId) {
    }

    float getIntroValue();

    void drawBackground(UiTree.Scope scope, UiTextMetrics textMetrics);

    void drawContent(UiTree.Scope scope, UiTextMetrics textMetrics, int mouseX, int mouseY);

    float getContentClipY();

    float getContentClipHeight();

    boolean requiresContentScissor();

    float getPanelHeight();

    boolean mouseClicked(double mouseX, double mouseY, int button);

    /**
     * 是否命中面板的标题栏区域。用于 DropdownScreen 决定要不要把面板拉到 z 堆栈最顶。
     * 点内容(设置项、下拉选项)不应触发 z-shuffle，否则视觉上会像闪烁。
     */
    default boolean isHeaderHovered(double mouseX, double mouseY) {
        return false;
    }

    boolean mouseReleased(double mouseX, double mouseY, int button);

    boolean mouseDragged(double mouseX, double mouseY);

    boolean mouseScrolled(double mouseX, double mouseY, double amount);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    boolean charTyped(String typedText);

    boolean hasActiveInput();

    void setPosition(float x, float y);

    void setMaxPanelHeight(float maxPanelHeight);

    float getX();

    float getY();

    float getWidth();

    boolean isOpened();

    void setOpened(boolean opened);

    boolean isVisible();

    void setVisible(boolean visible);

}
