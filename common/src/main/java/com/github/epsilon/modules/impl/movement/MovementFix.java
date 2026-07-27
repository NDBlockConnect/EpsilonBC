package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.impl.KeyboardInputEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import net.minecraft.util.Mth;

public class MovementFix extends Module {

    public static final MovementFix INSTANCE = new MovementFix();

    private MovementFix() {
        super("Movement Fix", Category.MOVEMENT);
    }

    private float getDirection(float playerYaw, float forward, float strafe) {
        float direction = playerYaw;

        boolean isMovingForward = forward > 0.0f;
        boolean isMovingBack = forward < 0.0f;
        boolean isMovingRight = strafe > 0.0f;
        boolean isMovingLeft = strafe < 0.0f;
        boolean isMovingSideways = isMovingRight || isMovingLeft;
        boolean isMovingStraight = isMovingForward || isMovingBack;

        if (forward != 0.0F || strafe != 0.0F) {
            if (isMovingBack && !isMovingSideways) {
                return direction + 180.0f;
            }
            if (isMovingForward && isMovingLeft) {
                return direction + 45.0f;
            }
            if (isMovingForward && isMovingRight) {
                return direction - 45.0f;
            }
            if (!isMovingStraight && isMovingLeft) {
                return direction + 90.0f;
            }
            if (!isMovingStraight && isMovingRight) {
                return direction - 90.0f;
            }
            if (isMovingBack && isMovingLeft) {
                return direction + 135.0f;
            }
            if (isMovingBack) {
                return direction - 135.0f;
            }
        }

        return direction;
    }

    public void fixMovement(KeyboardInputEvent event, float playerYaw, float serverYaw) {
        float forward = event.getForward();
        float strafe = event.getStrafe();

        if ((forward == 0.0f && strafe == 0.0f)
                || !Float.isFinite(playerYaw)
                || !Float.isFinite(serverYaw)) return;

        float magnitude = Math.max(Math.abs(forward), Math.abs(strafe));
        float difference = Mth.wrapDegrees(getDirection(playerYaw, forward, strafe) - serverYaw);
        int sector = Math.floorMod(Math.round(difference / 45.0f), 8);

        event.setForward(0.0f);
        event.setStrafe(0.0f);

        switch (sector) {
            case 0 -> event.setForward(magnitude);
            case 1 -> {
                event.setForward(magnitude);
                event.setStrafe(-magnitude);
            }
            case 2 -> event.setStrafe(-magnitude);
            case 3 -> {
                event.setForward(-magnitude);
                event.setStrafe(-magnitude);
            }
            case 4 -> event.setForward(-magnitude);
            case 5 -> {
                event.setForward(-magnitude);
                event.setStrafe(magnitude);
            }
            case 6 -> event.setStrafe(magnitude);
            case 7 -> {
                event.setForward(magnitude);
                event.setStrafe(magnitude);
            }
        }
    }

}
