package net.sploder12.potioncraft;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.sploder12.potioncraft.meta.MetaMixing;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.sploder12.potioncraft.meta.MetaMixing.interactions;

public class PotionCauldronBlock extends AbstractCauldronBlock implements BlockEntityProvider {

    //public static final MapCodec<PotionCauldronBlock> CODEC = createCodec(PotionCauldronBlock::new);

    public static final PotionCauldronBlock POTION_CAULDRON_BLOCK = new PotionCauldronBlock(
            FabricBlockSettings.copyOf(Blocks.CAULDRON)
    );

    public static final Identifier POTION_CAULDRON_ID = new Identifier("potioncraft", "potion_cauldron_block");


    /** Behavior of Potion Cauldron */
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 3;

    private static final int BASE_FLUID_HEIGHT = 6;
    private static final double FLUID_HEIGHT_PER_LEVEL = (15.0 - BASE_FLUID_HEIGHT) / (MAX_LEVEL - MIN_LEVEL + 1.0);

    /*
    public MapCodec<PotionCauldronBlock> getCodec() {
        return CODEC;
    }
     */

    public static void register() {
        Main.log("Registering Potion Cauldron...");

        Registry.register(
                Registries.BLOCK,
                POTION_CAULDRON_ID,
                POTION_CAULDRON_BLOCK);

        Registry.register(
                Registries.ITEM,
                POTION_CAULDRON_ID,
                new BlockItem(POTION_CAULDRON_BLOCK, new FabricItemSettings()));

        PotionCauldronBlockEntity.register();
    }
    /********************************/

    public PotionCauldronBlock(AbstractBlock.Settings settings) {
        super(settings, interactions);
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PotionCauldronBlockEntity cauldronEntity) {
            return cauldronEntity.getLevel();
        }

        return 0;
    }

    @Override
    public boolean isFull(BlockState state) {
        // Can't get block entity from here.
        // Luckily, this function doesn't do anything
        return false;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PotionCauldronBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }


    static public double getFluidHeight(int level) {
        return (BASE_FLUID_HEIGHT + level * FLUID_HEIGHT_PER_LEVEL) / 16.0;
    }

    protected boolean isEntityTouchingFluid(int level, BlockPos pos, Entity entity) {
        return entity.getY() < (double)pos.getY() + getFluidHeight(level) && entity.getBoundingBox().maxY > (double)pos.getY() + 0.25;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {

        if (!world.isClient()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PotionCauldronBlockEntity cauldronEntity
                    && isEntityTouchingFluid(cauldronEntity.getLevel(), pos, entity)
                    && entity instanceof ItemEntity itemEntity) {

                ItemStack items = itemEntity.getStack();

                Map<Item, CauldronBehavior> behaviorMap = MetaMixing.getBehavior(PotionCauldronBlock.POTION_CAULDRON_ID);

                if (behaviorMap.containsKey(items.getItem())) {
                    CauldronBehavior behavior = behaviorMap.get(items.getItem());

                    behavior.interact(state, world, pos, null, null, items);
                }
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {}

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (
                blockEntity instanceof PotionCauldronBlockEntity cauldronEntity &&
                cauldronEntity.getPotency() < PotionCauldronBlockEntity.getMaxPotency()) {

            double d = pos.getX();
            double e = pos.getY() + getFluidHeight(cauldronEntity.getLevel());
            double f = pos.getZ();

            world.addImportantParticle(ParticleTypes.SPLASH, d + 0.25 + world.random.nextDouble() * 0.5, e, f + 0.25 + world.random.nextDouble() * 0.5, world.random.nextDouble() * 0.02 - 0.01, 0.02, world.random.nextDouble() * 0.02 - 0.01);
            world.addImportantParticle(ParticleTypes.SPLASH, d + 0.25 + world.random.nextDouble() * 0.5, e, f + 0.25 + world.random.nextDouble() * 0.5, world.random.nextDouble() * 0.02 - 0.01, 0.02, world.random.nextDouble() * 0.02 - 0.01);
            world.addImportantParticle(ParticleTypes.SPLASH, d + 0.25 + world.random.nextDouble() * 0.5, e, f + 0.25 + world.random.nextDouble() * 0.5, world.random.nextDouble() * 0.02 - 0.01, 0.02, world.random.nextDouble() * 0.02 - 0.01);

            if (random.nextInt(2) == 0) {
                world.playSound(d + 0.5, e, f + 0.5, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }
    }
}
