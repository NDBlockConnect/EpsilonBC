package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PacketEvent;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.events.impl.RightClickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.SettingGroup;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.KeybindSetting;
import com.github.epsilon.utils.client.KeybindUtils;
import com.github.epsilon.utils.math.MathUtils;
import com.github.epsilon.utils.player.FindItemResult;
import com.github.epsilon.utils.player.InvUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrystalAura extends Module {

    public static final CrystalAura INSTANCE = new CrystalAura();

    private CrystalAura() {
        super("Crystal Aura", Category.COMBAT);
    }

    private final KeybindSetting activateKey = keybindSetting("Activate Key", GLFW.GLFW_KEY_UNKNOWN);
    private final DoubleSetting placeDelay = doubleSetting("Place Delay", 0.0, 0.0, 20.0, 1.0);
    private final DoubleSetting breakDelay = doubleSetting("Break Delay", 0.0, 0.0, 20.0, 1.0);
    private final DoubleSetting placeChance = doubleSetting("Place Chance", 100.0, 0.0, 100.0, 1.0);
    private final DoubleSetting breakChance = doubleSetting("Break Chance", 100.0, 0.0, 100.0, 1.0);
    private final BoolSetting stopOnKill = boolSetting("Stop on Kill", false);
    private final BoolSetting fakePunch = boolSetting("Fake Punch", false);
    private final BoolSetting clickSimulation = boolSetting("Click Simulation", false);
    private final BoolSetting damageTick = boolSetting("Damage Tick", false);
    private final BoolSetting antiWeakness = boolSetting("Anti-Weakness", false);
    private final BoolSetting strictAirCheck = boolSetting("Strict Air Check", false);
    private final DoubleSetting particleChance = doubleSetting("Particle Chance", 20.0, 0.0, 100.0, 1.0);
    private final BoolSetting swingHand = boolSetting("Swing Hand", true);

    private final SettingGroup sgGrim = settingGroup("Grim");

    // Grim 绕过：仅当服务器已看到视线与目标夹角小于该值时才攻击（参考 Meteor doYawSteps）
    private final DoubleSetting yawSteps = doubleSetting("Yaw Steps", 180.0, 0.0, 180.0, 5.0, () -> true).group(sgGrim);
    // 快速爆破：水晶生成当 tick 立即攻击（不依赖准星）
    private final BoolSetting fastBreak = boolSetting("Fast Break", false, () -> true).group(sgGrim);

    private int placeClock;
    private int breakClock;
    public boolean crystalling;

    private float lastServerYaw;
    private final Set<Integer> seenCrystals = new HashSet<>();

    public void resetClocks() {
        this.placeClock = 0;
        this.breakClock = 0;
    }

    @Override
    protected void onEnable() {
        resetClocks();
        crystalling = false;
        seenCrystals.clear();
        if (mc.player != null) lastServerYaw = mc.player.getYRot();
    }

    @Override
    protected void onDisable() {
        resetClocks();
        crystalling = false;
        seenCrystals.clear();
    }

    /**
     * 记录服务器实际看到的 yaw（来自最新移动感包），供 yawSteps 门控使用。
     */
    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet && packet.hasRotation()) {
            lastServerYaw = packet.getYRot(lastServerYaw);
        }
    }

    /**
     * Grim yawSteps 门控：服务器看到的 yaw 与当前视线 yaw 差值是否在阈值内。
     * 180 = 禁用。
     */
    private boolean yawStepGate() {
        float limit = yawSteps.getValue().floatValue();
        if (limit >= 180.0f) return true;
        float delta = Math.abs(Mth.wrapDegrees(mc.player.getYRot() - lastServerYaw));
        return delta <= limit;
    }

    @EventHandler
    public void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck() || mc.screen != null) return;

        boolean dontPlace = placeClock != 0;
        boolean dontBreak = breakClock != 0;

        if (stopOnKill.getValue() && isDeadBodyNearby()) return;

        int randomInt = MathUtils.getRandom(1, 100);

        if (dontPlace) placeClock--;
        if (dontBreak) breakClock--;

        if (mc.player.isUsingItem()) return;
        if (damageTick.getValue() && damageTickCheck()) return;

        if (activateKey.getValue() != GLFW.GLFW_KEY_UNKNOWN && !KeybindUtils.isPressed(activateKey.getValue())) {
            resetClocks();
            crystalling = false;
            return;
        }

        crystalling = true;

        // Fast Break：水晶生成当 tick 立即攻击（不依赖准星），Grim 门控同样生效
        if (fastBreak.getValue()) {
            List<EndCrystal> crystals = mc.level.getEntitiesOfClass(EndCrystal.class, mc.player.getBoundingBox().inflate(4.5));
            Set<Integer> currentIds = new HashSet<>();
            boolean broke = false;
            for (EndCrystal crystal : crystals) {
                int id = crystal.getId();
                currentIds.add(id);
                if (!seenCrystals.contains(id) && !dontBreak && yawStepGate()) {
                    mc.gameMode.attack(mc.player, crystal);
                    if (swingHand.getValue()) {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    } else {
                        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                    breakClock = breakDelay.getValue().intValue();
                    broke = true;
                }
            }
            seenCrystals.clear();
            seenCrystals.addAll(currentIds);
            if (broke && !dontBreak) {
                // 本 tick 已爆破，放置流程照常继续（0 延迟时同 tick 放置）
            }
        }

        if (!mc.player.getMainHandItem().is(Items.END_CRYSTAL)) return;

        if (mc.hitResult instanceof BlockHitResult hit) {
            if (!dontPlace
                    && randomInt <= placeChance.getValue().intValue()
                    && (mc.level.getBlockState(hit.getBlockPos()).is(Blocks.OBSIDIAN)
                    || mc.level.getBlockState(hit.getBlockPos()).is(Blocks.BEDROCK))
                    && canPlaceCrystalAssumeObsidian(hit.getBlockPos())) {

                if (clickSimulation.getValue()) {
                    KeyMapping.click(mc.options.keyUse.getDefaultKey());
                }

                BlockHitResult placeHit = hit;
                if (hit.getDirection() != Direction.UP) {
                    placeHit = new BlockHitResult(
                            Vec3.atCenterOf(hit.getBlockPos()).add(0.0, 0.5, 0.0),
                            Direction.UP,
                            hit.getBlockPos(),
                            false
                    );
                }

                InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, placeHit);
                if (result.consumesAction()) {
                    if (swingHand.getValue()) {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    } else {
                        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                }

                if (fakePunch.getValue() && randomInt <= particleChance.getValue().intValue() && hit.getDirection() == Direction.UP) {
                    // Placeholder for old block break particles behavior.
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }

                placeClock = placeDelay.getValue().intValue();
            }
        }

        randomInt = MathUtils.getRandom(1, 100);

        if (mc.hitResult instanceof EntityHitResult hit) {
            if (!dontBreak && randomInt <= breakChance.getValue().intValue()) {
                Entity entity = hit.getEntity();

                if (!(fakePunch.getValue() || entity instanceof EndCrystal || entity instanceof Slime)) {
                    return;
                }

                if (entity instanceof EndCrystal && !yawStepGate()) {
                    return;
                }

                int prevSlot = mc.player.getInventory().getSelectedSlot();
                boolean swappedForWeakness = false;

                if ((entity instanceof EndCrystal || entity instanceof Slime) && antiWeakness.getValue() && cantBreakCrystal()) {
                    swappedForWeakness = selectSword();
                }

                if (clickSimulation.getValue()) {
                    KeyMapping.click(mc.options.keyAttack.getDefaultKey());
                }

                mc.gameMode.attack(mc.player, entity);

                if (swingHand.getValue()) {
                    mc.player.swing(InteractionHand.MAIN_HAND);
                } else {
                    mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }

                breakClock = breakDelay.getValue().intValue();

                if (antiWeakness.getValue() && swappedForWeakness) {
                    InvUtils.swap(prevSlot, false);
                }
            }
        }
    }

    @EventHandler
    public void onClick(RightClickEvent event) {
        if (nullCheck()) return;
        if (!mc.mouseHandler.isRightPressed()) return;
        if (!mc.player.getMainHandItem().is(Items.END_CRYSTAL)) return;

        if (mc.hitResult instanceof BlockHitResult hit) {
            if ((mc.level.getBlockState(hit.getBlockPos()).is(Blocks.OBSIDIAN) || mc.level.getBlockState(hit.getBlockPos()).is(Blocks.BEDROCK))
                    && canPlaceCrystalAssumeObsidian(hit.getBlockPos())) {
                event.cancel();
            }
        }
    }

    private boolean selectSword() {
        FindItemResult sword = InvUtils.findInHotbar(stack -> stack.is(ItemTags.SWORDS));

        if (!sword.found()) return false;
        InvUtils.swap(sword.slot(), false);
        return true;
    }

    private boolean canPlaceCrystalAssumeObsidian(BlockPos supportPos) {
        BlockPos crystalPos = supportPos.above();
        // 水晶实体高度 2 格，crystalPos 与其上方都要在建筑高度内，否则服务器会踢 "建筑高度上限：319"
        if (mc.level.isOutsideBuildHeight(crystalPos) || mc.level.isOutsideBuildHeight(crystalPos.above())) return false;
        if (!mc.level.getBlockState(crystalPos).isAir()) return false;
        if (strictAirCheck.getValue() && !mc.level.getBlockState(crystalPos.above()).isAir()) return false;
        AABB box = new AABB(
                crystalPos.getX(), crystalPos.getY(), crystalPos.getZ(),
                crystalPos.getX() + 1, crystalPos.getY() + 2, crystalPos.getZ() + 1
        );
        return mc.level.getEntities(null, box).isEmpty();
    }

    private boolean cantBreakCrystal() {
        MobEffectInstance weakness = mc.player.getEffect(MobEffects.WEAKNESS);
        MobEffectInstance strength = mc.player.getEffect(MobEffects.STRENGTH);

        return weakness != null
                && (strength == null || strength.getAmplifier() <= weakness.getAmplifier())
                && !isToolLike(mc.player.getMainHandItem());
    }


    private boolean isToolLike(ItemStack stack) {
        return stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES);
    }

    private boolean isDeadBodyNearby() {
        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (player.distanceToSqr(mc.player) > 36.0) continue;
            if (!player.isAlive() || player.deathTime > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean damageTickCheck() {
        boolean found = mc.level.players().stream()
                .filter(p -> p != mc.player)
                .filter(p -> p.distanceToSqr(mc.player) < 36.0)
                .filter(p -> !p.onGround())
                .anyMatch(p -> p.hurtTime >= 2);

        if (!found) return false;

        return !(mc.hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof Player);
    }

}
