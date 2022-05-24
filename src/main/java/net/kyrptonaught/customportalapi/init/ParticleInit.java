package net.kyrptonaught.customportalapi.init;

import com.mojang.serialization.Codec;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleInit {
    public static DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CustomPortalsMod.MOD_ID);

    public static final RegistryObject<ParticleType<BlockStateParticleEffect>> CUSTOMPORTALPARTICLE = PARTICLES.register("customportalparticle", () -> new ParticleType<BlockStateParticleEffect>(false, BlockStateParticleEffect.PARAMETERS_FACTORY) {
        private Codec<BlockStateParticleEffect> codec = BlockStateParticleEffect.createCodec(this);
        @Override
        public Codec<BlockStateParticleEffect> getCodec() {
            return codec;
        }
    });
}
