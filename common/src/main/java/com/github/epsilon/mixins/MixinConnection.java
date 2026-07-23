package com.github.epsilon.mixins;

import com.github.epsilon.events.bus.EventBus;
import com.github.epsilon.events.impl.PacketEvent;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.utils.network.ClientIdentityHider;
import com.github.epsilon.utils.network.PacketUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Connection.class)
public class MixinConnection {

    @WrapOperation(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"))
    private void onReceivePacket(Packet<?> packet, PacketListener listener, Operation<Void> original) {
        // In singleplayer both the client and the integrated-server Connection are mixed in.
        // Only the client connection receives CLIENTBOUND packets; skip event dispatch for the
        // server-side connection so modules never touch serverbound traffic they can't handle.
        if (((Connection) (Object) this).getReceiving() != PacketFlow.CLIENTBOUND) {
            original.call(packet, listener);
            return;
        }
        if (Managers.S2CPACKET.onPacketReceive(packet)) {
            return;
        }
        PacketEvent.Receive event = EventBus.INSTANCE.post(new PacketEvent.Receive(packet));
        if (!event.isCancelled()) {
            original.call(event.getPacket(), listener);
        }
    }

    @WrapOperation(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"))
    private void onSendPacket(Connection instance, Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, Operation<Void> original) {
        // In singleplayer the integrated-server Connection also runs this mixin. It sends
        // CLIENTBOUND packets; dispatching them as PacketEvent.Send lets modules (e.g. Blink)
        // cache and re-send them through the client's serverbound codec, which throws
        // EncoderException and drops the connection. Only handle the client's serverbound sends.
        if (instance.getSending() != PacketFlow.SERVERBOUND) {
            original.call(instance, packet, listener, flush);
            return;
        }
        if (Managers.C2SPACKET.onPacketSend(packet)) {
            return;
        }
        if (PacketUtils.bypassedPackets.contains(packet)) {
            PacketUtils.bypassedPackets.remove(packet);
            Packet<?> filteredPacket = ClientIdentityHider.filterServerboundPacket(packet);
            if (filteredPacket != null) {
                original.call(instance, filteredPacket, listener, flush);
            }
        } else {
            PacketEvent.Send event = EventBus.INSTANCE.post(new PacketEvent.Send(packet));
            if (!event.isCancelled()) {
                Packet<?> filteredPacket = ClientIdentityHider.filterServerboundPacket(event.getPacket());
                if (filteredPacket != null) {
                    original.call(instance, filteredPacket, listener, flush);
                }
            }
        }
    }

}
