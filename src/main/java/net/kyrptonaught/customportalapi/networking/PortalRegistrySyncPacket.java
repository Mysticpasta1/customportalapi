package net.kyrptonaught.customportalapi.networking;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.util.PortalLink;
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

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public record PortalRegistrySyncPacket(PortalLink link) {

        public static void sendForcePacket(ServerPlayerEntity player, BlockPos pos) {
            NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new net.kyrptonaught.customportalapi.networking.ForcePlacePortalPacket(pos));
        }

        public static PortalRegistrySyncPacket decode(PacketByteBuf buf) {
            return new PortalRegistrySyncPacket(new PortalLink(buf.readIdentifier(), buf.readIdentifier(), buf.readInt()));
        }

        public static void encode(PortalRegistrySyncPacket packet, PacketByteBuf buf) {
            buf.writeIdentifier(packet.link().block).writeIdentifier(packet.link().dimID).writeInt(packet.link().colorID);
        }

        public static void handle(PortalRegistrySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            PerWorldPortals.registerWorldPortal(packet.link());
            contextSupplier.get().setPacketHandled(true);
        }

        public static void register(SimpleChannel channel, Integer id) {
            channel.registerMessage(id, PortalRegistrySyncPacket.class, PortalRegistrySyncPacket::encode, PortalRegistrySyncPacket::decode, PortalRegistrySyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        }

    public static void registerSyncOnPlayerJoin() {
        MinecraftForge.EVENT_BUS.addListener(PortalRegistrySyncPacket::onPlayerJoinWorld);
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity player) {
            for (PortalLink link : CustomPortalApiRegistry.getAllPortalLinks()) {
                NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PortalRegistrySyncPacket(link));
            }
        }
    }
}
