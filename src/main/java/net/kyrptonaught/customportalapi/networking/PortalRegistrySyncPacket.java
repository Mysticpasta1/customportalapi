package net.kyrptonaught.customportalapi.networking;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.util.Identifier;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class PortalRegistrySyncPacket {
    private final PortalLink link;

    public PortalRegistrySyncPacket(PortalLink link) {
        this.link = link;
    }

    public PortalLink link() {
        return link;
    }

    public static void sendForcePacket(ServerPlayerEntity player, BlockPos pos) {
        NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new net.kyrptonaught.customportalapi.networking.ForcePlacePortalPacket(pos));
    }

    public static PortalRegistrySyncPacket decode(PacketByteBuf buf) {
        Identifier block = buf.readBoolean() ? buf.readIdentifier() : null;
        Identifier dimID = buf.readBoolean() ? buf.readIdentifier() : null;
        Identifier returnDimID = buf.readBoolean() ? buf.readIdentifier() : null;
        int colorID = buf.readInt();
        PortalLink link = new PortalLink(block, dimID, returnDimID, colorID);
        if (buf.readBoolean()) link.portalFrameTester = buf.readIdentifier();
        if (buf.readBoolean()) {
            net.minecraft.block.Block portalBlock = net.minecraft.registry.Registries.BLOCK.get(buf.readIdentifier());
            if (portalBlock instanceof CustomPortalBlock)
                link.setPortalBlock((CustomPortalBlock) portalBlock);
        }
        return new PortalRegistrySyncPacket(link);
    }

    public static void encode(PortalRegistrySyncPacket packet, PacketByteBuf buf) {
        buf.writeBoolean(packet.link().block != null);
        if (packet.link().block != null) buf.writeIdentifier(packet.link().block);
        buf.writeBoolean(packet.link().dimID != null);
        if (packet.link().dimID != null) buf.writeIdentifier(packet.link().dimID);
        buf.writeBoolean(packet.link().returnDimID != null);
        if (packet.link().returnDimID != null) buf.writeIdentifier(packet.link().returnDimID);
        buf.writeInt(packet.link().colorID);
        buf.writeBoolean(packet.link().portalFrameTester != null);
        if (packet.link().portalFrameTester != null) buf.writeIdentifier(packet.link().portalFrameTester);

        boolean hasCustomBlock = packet.link().getPortalBlock() != null && !net.minecraft.registry.Registries.BLOCK.getId(packet.link().getPortalBlock()).equals(net.minecraft.registry.Registries.BLOCK.getId(CustomPortalsMod.getDefaultPortalBlock()));
        buf.writeBoolean(hasCustomBlock);
        if (hasCustomBlock)
            buf.writeIdentifier(net.minecraft.registry.Registries.BLOCK.getId(packet.link().getPortalBlock()));
    }

    public static void handle(PortalRegistrySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> PerWorldPortals.registerWorldPortal(packet.link()));
        contextSupplier.get().setPacketHandled(true);
    }

    public static void register(SimpleChannel channel, Integer id) {
        channel.registerMessage(id, PortalRegistrySyncPacket.class, PortalRegistrySyncPacket::encode, PortalRegistrySyncPacket::decode, PortalRegistrySyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void registerSyncOnPlayerJoin() {
        MinecraftForge.EVENT_BUS.addListener(PortalRegistrySyncPacket::onPlayerJoinWorld);
    }

    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity player) {
            for (PortalLink link : CustomPortalApiRegistry.getAllPortalLinks()) {
                NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PortalRegistrySyncPacket(link));
            }
        }
    }
}
