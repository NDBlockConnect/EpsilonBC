package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.impl.KeyboardInputEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import net.minecraft.util.Mth;

public class MovementFix extends Module {

    public static final MovementFix INSTANCE = new MovementFix();

    private MovementFix() {
        super("Movement Fix", Category.MOVEMENT);
        setDefaultEnabled(true);
    }

    public void fixMovement(KeyboardInputEvent event, float playerYaw, float serverYaw) {
        float forward = event.getForward();
        float strafe = event.getStrafe();

        if ((forward == 0.0f && strafe == 0.0f)
                || !Float.isFinite(playerYaw)
                || !Float.isFinite(serverYaw)) return;

        float magnitude = Math.max(Math.abs(forward), Math.abs(strafe));
        float targetDirection = movementDirection(playerYaw, forward, strafe);
        float bestForward = 0.0f;
        float bestStrafe = 0.0f;
        float smallestDifference = Float.MAX_VALUE;

        // 根据服务器 yaw 枚举所有合法输入，选择世界运动方向最接近原始输入的组合。
        for (int candidateForward = -1; candidateForward <= 1; candidateForward++) {
            for (int candidateStrafe = -1; candidateStrafe <= 1; candidateStrafe++) {
                if (candidateForward == 0 && candidateStrafe == 0) continue;

                float difference = Math.abs(Mth.wrapDegrees(
                        movementDirection(serverYaw, candidateForward, candidateStrafe) - targetDirection));
                if (difference < smallestDifference) {
                    smallestDifference = difference;
                    bestForward = candidateForward * magnitude;
                    bestStrafe = candidateStrafe * magnitude;
                }
            }
        }

        event.setForward(bestForward);
        event.setStrafe(bestStrafe);
    }

    private static float movementDirection(float yaw, float forward, float strafe) {
        if (forward < 0.0f) yaw += 180.0f;
        float forwardScale = forward < 0.0f ? -0.5f : forward > 0.0f ? 0.5f : 1.0f;
        if (strafe > 0.0f) yaw -= 90.0f * forwardScale;
        if (strafe < 0.0f) yaw += 90.0f * forwardScale;
        return yaw;
    }

}
