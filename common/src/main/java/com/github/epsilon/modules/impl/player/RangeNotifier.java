package com.github.epsilon.modules.impl.player;

import com.github.epsilon.elements.impl.notification.NotificationMode;
import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.RegistryListSetting;
import com.github.epsilon.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RangeNotifier extends Module {

    public static final RangeNotifier INSTANCE = new RangeNotifier();

    private RangeNotifier() {
        super("RangeNotifier", Category.PLAYER);
    }

    private enum AlertMode {
        Chat,
        Notification,
        Both
    }

    private final DoubleSetting range = doubleSetting("Range", 20.0, 1.0, 128.0, 1.0);
    private final EnumSetting<AlertMode> alertMode = enumSetting("Alert Mode", AlertMode.Both);
    private final BoolSetting notifyLeave = boolSetting("Notify Leave", false);
    private final BoolSetting ignoreFriends = boolSetting("Ignore Friends", true);
    private final BoolSetting playSound = boolSetting("Play Sound", true);
    private final DoubleSetting volume = doubleSetting("Volume", 1.0, 0.0, 1.0, 0.05, playSound::getValue);
    private final DoubleSetting pitch = doubleSetting("Pitch", 1.0, 0.5, 2.0, 0.05, playSound::getValue);
    private final RegistryListSetting<SoundEvent> sound = soundEventListSetting("Sound",
            List.of(SoundEvents.ARROW_HIT_PLAYER), playSound::getValue);

    // Players currently known to be within range.
    private final Set<UUID> inRange = new HashSet<>();

    @Override
    protected void onEnable() {
        inRange.clear();
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;

        double maxDist = range.getValue();
        double maxDistSq = maxDist * maxDist;

        Set<UUID> current = new HashSet<>();
        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player) || entity == mc.player) continue;
            if (ignoreFriends.getValue() && Managers.FRIEND.isFriend(player)) continue;

            if (mc.player.distanceToSqr(player) <= maxDistSq) {
                UUID id = player.getUUID();
                current.add(id);
                if (!inRange.contains(id)) {
                    alert(player.getGameProfile().name(), true);
                }
            }
        }

        if (notifyLeave.getValue()) {
            for (UUID id : inRange) {
                if (!current.contains(id)) {
                    Player left = mc.level.getPlayerByUUID(id);
                    String name = left != null ? left.getGameProfile().name() : id.toString();
                    alert(name, false);
                }
            }
        }

        inRange.clear();
        inRange.addAll(current);
    }

    private void alert(String name, boolean entered) {
        String msg = name + (entered ? " entered range" : " left range");
        ChatFormatting color = entered ? ChatFormatting.RED : ChatFormatting.GREEN;
        AlertMode mode = alertMode.getValue();

        if (mode == AlertMode.Chat || mode == AlertMode.Both) {
            ChatUtils.addChatMessage(Component.literal("[Range] " + msg).withStyle(color));
        }
        if (mode == AlertMode.Notification || mode == AlertMode.Both) {
            int hash = java.util.Objects.hash(name, entered);
            Managers.NOTIFICATION.notifyHud(msg, "", entered ? NotificationMode.Error : NotificationMode.Success, hash);
        }
        if (entered && playSound.getValue() && !sound.getValue().isEmpty()) {
            mc.level.playLocalSound(mc.player.blockPosition(), sound.getValue().get(0),
                    SoundSource.PLAYERS, volume.getValue().floatValue(), pitch.getValue().floatValue(), false);
        }
    }
}
