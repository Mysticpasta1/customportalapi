package net.kyrptonaught.customportalapi.client;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.networking.ForcePlacePortalPacket;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

public class ClientHandler {
    public static void forcePortal(ForcePlacePortalPacket packet) {
        MinecraftClient.getInstance().execute(() -> {
            @SuppressWarnings("resource")
			World world = MinecraftClient.getInstance().world;
            BlockState oldState = world.getBlockState(packet.pos());
            world.setBlockState(packet.pos(), CustomPortalHelper.blockWithAxis(CustomPortalsMod.getDefaultPortalBlock().getDefaultState(), CustomPortalHelper.getAxisFrom(oldState)));
        });
    }
}
