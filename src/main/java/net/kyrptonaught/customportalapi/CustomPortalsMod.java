package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.init.ParticleInit;
import net.kyrptonaught.customportalapi.networking.NetworkManager;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.FlatPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.linking.PortalLinkingStorage;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.function.Supplier;

import static net.kyrptonaught.customportalapi.CustomPortalsMod.MOD_ID;

@Mod(MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPortalsMod {
    public static final String MOD_ID = "customportalapi";

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static HashMap<Identifier, RegistryKey<World>> dims = new HashMap<>();
    public static Identifier VANILLAPORTAL_FRAMETESTER = new Identifier(MOD_ID, "vanillanether");
    public static Identifier FLATPORTAL_FRAMETESTER = new Identifier(MOD_ID, "flat");
    public static PortalLinkingStorage portalLinkingStorage;

    public CustomPortalsMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(bus);

        ParticleInit.PARTICLES.register(bus);
        onInitialize(bus);
    }

    private void onServerStart(ServerStartedEvent event) {
        for (RegistryKey<World> registryKey : event.getServer().getWorldRegistryKeys()) {
            dims.put(registryKey.getValue(), registryKey);
        }
        portalLinkingStorage = (PortalLinkingStorage) event.getServer().getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(PortalLinkingStorage::fromNbt, PortalLinkingStorage::new, MOD_ID);
    }

    private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Entity entity = event.getEntity();
        World world = event.getLevel();
        Hand hand = event.getHand();

        if (entity instanceof PlayerEntity player) {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient) {
                Item item = stack.getItem();
                if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
                    HitResult hit = player.raycast(6, 1, false);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHit = (BlockHitResult) hit;
                        BlockPos usedBlockPos = blockHit.getBlockPos();
                        if (PortalPlacer.attemptPortalLight(world, usedBlockPos.offset(blockHit.getSide()), PortalIgnitionSource.ItemUseSource(item))) {
                            event.setResult(Event.Result.ALLOW);
                        }
                    }
                }
            }
        }
    }

    public void onInitialize(IEventBus bus) {
        MinecraftForge.EVENT_BUS.addListener(this::onServerStart);
        CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
        CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickItem);

//        CustomPortalBuilder.beginPortal().frameBlock(Blocks.GLOWSTONE).destDimID(new Identifier("the_nether")).lightWithWater().tintColor(46, 5, 25).registerPortal();
//        CustomPortalBuilder.beginPortal().frameBlock(Blocks.DIAMOND_BLOCK).destDimID(new Identifier("the_nether")).tintColor(66, 135, 245).registerPortal();
//        CustomPortalBuilder.beginPortal().frameBlock(Blocks.COBBLESTONE).lightWithItem(Items.STICK).destDimID(new Identifier("the_end")).tintColor(45, 24, 45).flatPortal().registerPortal();
//        CustomPortalBuilder.beginPortal().frameBlock(Blocks.EMERALD_BLOCK).lightWithWater().destDimID(new Identifier("the_end")).tintColor(25, 76, 156).flatPortal().registerPortal();
    }

    public static void logError(String message) {
        System.out.println("[" + MOD_ID + "]ERROR: " + message);
    }

    public static CustomPortalBlock getDefaultPortalBlock() {
        return customPortalBlock.get();
    }

    // to guarantee block exists before use, unsure how safe this is but works for now. Don't want to switch to using a custom entrypoint to break compatibility with existing mods just yet
    //todo fix this with CustomPortalBuilder?

    public static final RegistryObject<CustomPortalBlock> customPortalBlock = registerOnlyBlock("custom_portal_block", () -> new CustomPortalBlock(Block.Settings.of(Material.PORTAL).noCollision().strength(-1).sounds(BlockSoundGroup.GLASS).luminance(state -> 11)));

    public static <T extends Block> RegistryObject<T> registerOnlyBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    @SubscribeEvent
    public static void onCommonStartUp(FMLCommonSetupEvent event) {
        NetworkManager.register();
    }
}