package net.sploder12.potioncraft;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Function;

public class PotionCauldronBlock extends Block implements BlockEntityProvider {

    public static final PotionCauldronBlock POTION_CAULDRON_BLOCK = new PotionCauldronBlock(
            FabricBlockSettings.copyOf(Blocks.CAULDRON)
    );

    public static final HashMap<Item, Function<OnUseData, Boolean>> interactions = new HashMap<>();

    public static Function<OnUseData, Boolean> addInteraction(Item item, Function<OnUseData, Boolean> func) {
        return interactions.put(item, func);
    }

    public static boolean canInteract(Item item) {
        return interactions.containsKey(item);
    }

    public static Function<OnUseData, Boolean> getInteraction(Item item) {
        return interactions.get(item);
    }


    public static final Identifier POTION_CAULDRON_ID = new Identifier("potioncraft", "potion_cauldron_block");

    private static final VoxelShape RAYCAST_SHAPE = createCuboidShape(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape OUTLINE_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(createCuboidShape(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), createCuboidShape(4.0, 0.0, 0.0, 12.0, 3.0, 16.0), createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), RAYCAST_SHAPE), BooleanBiFunction.ONLY_FIRST);

    /** Behavior of Potion Cauldron */
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 3;

    private static final int BASE_FLUID_HEIGHT = 6;
    private static final double FLUID_HEIGHT_PER_LEVEL = (15.0 - BASE_FLUID_HEIGHT) / (MAX_LEVEL - MIN_LEVEL + 1.0);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    public static void register() {
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
        super(settings);
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

                // @TODO item use
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PotionCauldronBlockEntity cauldronEntity) {

                ItemStack items = player.getStackInHand(hand);
                if (items.getItem() == Items.POTION) {

                    if(cauldronEntity.addLevel(PotionUtil.getPotionEffects(items))) {

                        if (!player.isCreative()) {

                            items.decrement(1);
                            if (items.isEmpty()) {
                                player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
                            } else {
                                player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
                            }
                        }

                        return ActionResult.SUCCESS;
                    }

                    return ActionResult.PASS;
                }
                else if (items.getItem() == Items.GLASS_BOTTLE) {

                    ItemStack potion = cauldronEntity.pickupFluid();

                    if (!player.isCreative()) {
                        items.decrement(1);
                    }

                    if (items.isEmpty()) {
                        player.setStackInHand(hand, potion);
                    }
                    else {
                        player.getInventory().insertStack(potion);
                    }

                    // empty so force it to become cauldron
                    if (cauldronEntity.getLevel() < PotionCauldronBlock.MIN_LEVEL) {
                        BlockState cauldron = Blocks.CAULDRON.getDefaultState();
                        world.setBlockState(pos, cauldron);
                    }

                    return ActionResult.SUCCESS;
                }
                else if (canInteract(items.getItem()) && getInteraction(items.getItem()).apply(
                        new OnUseData(cauldronEntity, state, world, pos, player, hand, hit, true)
                )) {
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

}
