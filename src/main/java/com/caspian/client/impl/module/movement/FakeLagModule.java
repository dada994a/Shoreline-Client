package com.caspian.client.impl.module.movement;

import com.caspian.client.api.config.Config;
import com.caspian.client.api.config.setting.BooleanConfig;
import com.caspian.client.api.config.setting.EnumConfig;
import com.caspian.client.api.event.EventStage;
import com.caspian.client.api.event.listener.EventListener;
import com.caspian.client.api.module.ModuleCategory;
import com.caspian.client.api.module.ToggleModule;
import com.caspian.client.impl.event.TickEvent;
import com.caspian.client.impl.event.network.DisconnectEvent;
import com.caspian.client.impl.event.network.PacketEvent;
import com.caspian.client.util.world.FakePlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * @author linus
 * @since 1.0
 */
public class FakeLagModule extends ToggleModule
{
    //
    Config<LagMode> modeConfig = new EnumConfig<>("Mode", "The mode for " +
            "caching packets", LagMode.BLINK, LagMode.values());
    Config<Boolean> renderConfig = new BooleanConfig("Render", "Renders the " +
            "serverside player postion", true);
    //
    private final Set<Packet<?>> packets = new HashSet<>();
    //
    private FakePlayerEntity serverModel;

    /**
     *
     */
    public FakeLagModule()
    {
        super("FakeLag", "Withholds packets from the server, creating " +
                "clientside lag", ModuleCategory.MOVEMENT);
    }

    /**
     *
     */
    @Override
    public void onEnable()
    {
        if (renderConfig.getValue())
        {
            serverModel = new FakePlayerEntity(mc.player);
            serverModel.spawnPlayer();
        }
    }

    /**
     *
     */
    @Override
    public void onDisable()
    {
        if (mc.player == null)
        {
            return;
        }
        if (!packets.isEmpty())
        {
            for (Packet<?> p : packets)
            {
                mc.player.networkHandler.sendPacket(p);
            }
            packets.clear();
        }
        if (serverModel != null)
        {
            serverModel.despawnPlayer();
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == EventStage.PRE)
        {
            // packets.add()
        }
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onDisconnectEvent(DisconnectEvent event)
    {
        // packets.clear();
        disable();
    }

    /**
     *
     * @param event
     */
    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player.isRiding())
        {
            return;
        }
        if (!(event.getPacket() instanceof ChatMessageC2SPacket
                || event.getPacket() instanceof TeleportConfirmC2SPacket
                || event.getPacket() instanceof KeepAliveC2SPacket
                || event.getPacket() instanceof ClientStatusC2SPacket))
        {
            event.cancel();
            packets.add(event.getPacket());
        }
    }

    public enum LagMode
    {
        BLINK
    }
}