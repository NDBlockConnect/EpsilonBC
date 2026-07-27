package com.github.epsilon.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class Render3DEvent {

    private final PoseStack poseStack;
    private final CameraRenderState cameraState;

    public Render3DEvent(PoseStack poseStack, CameraRenderState cameraState) {
        this.poseStack = poseStack;
        this.cameraState = cameraState;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public CameraRenderState getCameraState() {
        return cameraState;
    }

}
