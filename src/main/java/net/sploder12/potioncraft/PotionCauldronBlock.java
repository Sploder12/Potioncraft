package net.sploder12.potioncraft;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.sploder12.potioncraft.util.FluidHelper;
import net.sploder12.potioncraft.meta.MetaMixing;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.sploder12.potioncraft.meta.MetaMixing.interactions;

public class PotionCauldronBlock extends AbstractCauldronBlock implements BlockEntityProvider {

    public static final MapCodec<PotionCauldronBlock> CODEC = createCodec(PotionCauldronBlock::new);

    public static final IntProperty LUMINANCE = IntProperty.of("luminance", 0, 15);

    public static final PotionCauldronBlock POTION_CAULDRON_BLOCK = new PotionCauldronBlock(
            FabricBlockSettings.copyOf(Blocks.CAULDRON).luminance(
                    (BlockState state) -> state.get(LUMINANCE)
            )
    );

    public static final Identifier POTION_CAULDRON_ID = new Identifier("potioncraft", "potion_cauldron_block");


    /** Behavior of Potion Cauldron */
    public static final int MIN_LEVEL = LeveledCauldronBlock.MIN_LEVEL;
    public static final int MAX_LEVEL = LeveledCauldronBlock.MAX_LEVEL;

    private static final int BASE_FLUID_HEIGHT = 6;
    private static final double FLUID_HEIGHT_PER_LEVEL = (15.0 - BASE_FLUID_HEIGHT) / (MAX_LEVEL - MIN_LEVEL + 1.0);


    public MapCodec<PotionCauldronBlock> getCodec() {
        return CODEC;
    }


    public static void register() {
        Main.debug("Registering Potion Cauldron...");

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
        setDefaultState(getDefaultState().with(LUMINANCE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LUMINANCE);
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

    private void updateLuminance(BlockState state, World world, BlockPos pos, PotionCauldronBlockEntity entity) {
        world.setBlockState(pos, state.with(LUMINANCE, entity.getLuminance()));
    }

    @Override
    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return true;
    }

    @Override
    protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
        if (!Config.getBoolean(Config.FieldID.FILL_FROM_DRIPSTONE)) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PotionCauldronBlockEntity cauldronEntity) {
            if (cauldronEntity.getFluid() == FluidHelper.getStill(fluid)) {
                cauldronEntity.addLevel(true);

                if (!cauldronEntity.hasEffects() && cauldronEntity.getLevel() >= PotionCauldronBlock.MAX_LEVEL) {
                    BlockState block = FluidHelper.getBlock(cauldronEntity.getFluid()).getDefaultState();

                    if (block.getBlock() instanceof LeveledCauldronBlock) {
                        world.setBlockState(pos, block.with(LeveledCauldronBlock.LEVEL, cauldronEntity.getLevel()));
                    }
                    else {
                        world.setBlockState(pos, block);
                    }
                }
            }
        }
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult out = super.onUse(state, world, pos, player, hand, hit);

        if (!world.isClient()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PotionCauldronBlockEntity cauldronEntity) {
                updateLuminance(state, world, pos, cauldronEntity);
            }
        }

        return out;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!Config.getBoolean(Config.FieldID.ALLOW_DROP_MIXING)) {
            return;
        }

        if (!world.isClient()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PotionCauldronBlockEntity cauldronEntity
                    && isEntityTouchingFluid(cauldronEntity.getLevel(), pos, entity)
                    && entity instanceof ItemEntity itemEntity) {

                ItemStack items = itemEntity.getStack();

                Map<Item, CauldronBehavior> behaviorMap = MetaMixing.getBehavior(PotionCauldronBlock.POTION_CAULDRON_BLOCK);

                if (behaviorMap.containsKey(items.getItem())) {
                    CauldronBehavior behavior = behaviorMap.get(items.getItem());

                    behavior.interact(state, world, pos, null, null, items);

                    BlockEntity resultEntity = world.getBlockEntity(pos);
                    if (resultEntity instanceof PotionCauldronBlockEntity cauldronResultEntity) {
                        updateLuminance(state, world, pos, cauldronResultEntity);
                    }
                }
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (Config.getBoolean(Config.FieldID.DO_BUBBLE_EFFECTS)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            final int maxPotency = PotionCauldronBlockEntity.getMaxPotency();
            if (blockEntity instanceof PotionCauldronBlockEntity cauldronEntity &&
                            (cauldronEntity.getPotency() < maxPotency || maxPotency < 0) &&
                            (cauldronEntity.getFluid() == Fluids.WATER)) {

                double d = pos.getX();
                double e = pos.getY() + getFluidHeight(cauldronEntity.getLevel());
                double f = pos.getZ();

                int color = cauldronEntity.getColor();

                double r = (double)(color >> 16 & 0xFF) / 255.0;
                double g = (double)(color >> 8 & 0xFF) / 255.0;
                double b = (double)(color & 0xFF) / 255.0;



                for (int i = 0; i < 3; ++i) {
                    world.addParticle(ParticleTypes.SPLASH, d + 0.25 + random.nextDouble() * 0.5, e, f + 0.25 + random.nextDouble() * 0.5, r, g, b);
                }

                if (random.nextInt(2) == 0) {
                    world.playSound(d + 0.5, e, f + 0.5, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
                }
            }
        }
    }
}
