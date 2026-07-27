package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.impl.KeyboardInputEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import net.minecraft.util.Mth;

public class MovementFix extends Module {

    public static final MovementFix INSTANCE = new MovementFix();

    private static final float DIAGONAL_ANGLE = 45.0f;
    private static final float SIDE_ANGLE = 90.0f;
    private static final float REVERSE_ANGLE = 180.0f;

    private MovementFix() {
        super("Movement Fix", Category.MOVEMENT);
    }

    private float getDirection(float playerYaw, float forward, float strafe) {
        if (forward > 0.0f) {
            if (strafe < 0.0f) return playerYaw - DIAGONAL_ANGLE;
            if (strafe > 0.0f) return playerYaw + DIAGONAL_ANGLE;
            return playerYaw;
        }

        if (forward < 0.0f) {
            if (strafe < 0.0f) return playerYaw + REVERSE_ANGLE - DIAGONAL_ANGLE;
            if (strafe > 0.0f) return playerYaw + REVERSE_ANGLE + DIAGONAL_ANGLE;
            return playerYaw + REVERSE_ANGLE;
        }

        if (strafe < 0.0f) return playerYaw - SIDE_ANGLE;
        if (strafe > 0.0f) return playerYaw + SIDE_ANGLE;

        return playerYaw;
    }

    public void fixMovement(KeyboardInputEvent event, float playerYaw, float serverYaw) {
        float forward = event.getForward();
        float strafe = event.getStrafe();

        if ((forward == 0.0f && strafe == 0.0f)
                || !Float.isFinite(playerYaw)
                || !Float.isFinite(serverYaw)) return;

        float magnitude = Math.max(Math.abs(forward), Math.abs(strafe));
        float difference = Mth.wrapDegrees(getDirection(playerYaw, forward, strafe) - serverYaw);
        // 修复：使用 Math.floorMod 正确处理负角度，避免向零截断导致的方向错误
        int sector = Math.floorMod(Math.round(difference / 45.0f), 8);

        float newForward = 0.0f;
        float newStrafe = 0.0f;

        switch (sector) {
            case 0 -> newForward = magnitude;
            case 1 -> {
                newForward = magnitude;
                newStrafe = -magnitude;
            }
            case 2 -> newStrafe = -magnitude;
            case 3 -> {
                newForward = -magnitude;
                newStrafe = -magnitude;
            }
            case 4 -> newForward = -magnitude;
            case 5 -> {
                newForward = -magnitude;
                newStrafe = magnitude;
            }
            case 6 -> newStrafe = magnitude;
            case 7 -> {
                newForward = magnitude;
                newStrafe = magnitude;
            }
        }

        event.setForward(newForward);
        event.setStrafe(newStrafe);
    }

}
