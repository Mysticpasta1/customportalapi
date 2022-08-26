package net.kyrptonaught.customportalapi.client;

import com.mojang.bridge.game.GameSession;
import com.mojang.bridge.launcher.SessionEventListener;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.init.ParticleInit;
import net.kyrptonaught.customportalapi.mixin.client.ChunkRendererRegionAccessor;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CustomPortalsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPortalsModClient {




    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.getBlockColors().registerColorProvider((state, world, pos, tintIndex) -> {
            if (pos != null && world instanceof ChunkRendererRegion) {
                Block block = CustomPortalHelper.getPortalBase(((ChunkRendererRegionAccessor) world).getWorld(), pos);
                PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
                if (link != null) return link.colorID;
            }
            return 1908001;
        }, CustomPortalsMod.customPortalBlock.get());
    }



    @SubscribeEvent
    public static void onParticleFactoryRegistry(final RegisterParticleProvidersEvent event) {
        MinecraftClient.getInstance().particleManager.registerFactory(ParticleInit.CUSTOMPORTALPARTICLE.get(), CustomPortalParticle.Factory::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftClient.getInstance().getGame().setSessionEventListener(new SessionEventListener() {
            @Override
            public void onStartGameSession(GameSession session) {
            }

            @Override
            public void onLeaveGameSession(GameSession session) {
                PerWorldPortals.removeOldPortalsFromRegistry();
            }
        });
    }
}