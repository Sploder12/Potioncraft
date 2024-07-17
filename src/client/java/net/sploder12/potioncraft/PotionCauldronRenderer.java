package net.sploder12.potioncraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class PotionCauldronRenderer implements BlockEntityRenderer<PotionCauldronBlockEntity> {
    
    // fluid is 12 pixels of the texture out o 16
    private static final float fluidPixWidth = 12.0f;
    private static final float texPixWidth = 16.0f;

    // width of the fluid as a floating point value
    private static final float fluidWidth = (fluidPixWidth / texPixWidth) / 2.0f;

    // correction so the fluid texture isn't squished
    private static final float uvCorrection = ((texPixWidth - fluidPixWidth) / texPixWidth) / 2.0f;

    public PotionCauldronRenderer(BlockEntityRendererFactory.Context ctx) {

        //fluid = MinecraftClient.getInstance().getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getSprite(SimpleFluidRenderHandler.fluid_STILL);
    }

    @Override
    public void render(PotionCauldronBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        World world = entity.getWorld();

        int lightAbove = light;
        if (world != null) {
            // fluid gets illuminated from the top, not the sides or bottom!
            lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());
        }

        int luminance = entity.getLuminance();
        if (luminance > 0) {
            // see WorldRenderer.getLightmapCoordinates to see why this works
            int blockLuminance = (lightAbove >> 4) & 0xf;
            lightAbove &= 0xffffff00;
            lightAbove |= Math.max(blockLuminance, entity.getLuminance()) << 4;
        }

        // alpha is set to opaque since the texture handles transluscency
        int color = entity.getColor() | (0xff << 24);

        // get the still texture of our fluid
        Fluid still = FluidHelper.getStill(entity.getFluid());
        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(still);

        if (fluidRenderHandler == null) return;

        Sprite fluid = fluidRenderHandler.getFluidSprites(world, entity.getPos(), still.getDefaultState())[0];

        double yOffset = PotionCauldronBlock.getFluidHeight(entity.getLevel());

        // note: Drawing from the center of the block instead of corner
        matrices.push();
        matrices.translate(0.5, yOffset, 0.5);

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getTranslucentMovingBlock());

        float uOffset = (fluid.getMaxU() - fluid.getMinU()) * uvCorrection;
        float vOffset = (fluid.getMaxV() - fluid.getMinV()) * uvCorrection;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        buffer.vertex(matrix, -fluidWidth, 0.0f, fluidWidth).color(color).texture(fluid.getMinU() + uOffset, fluid.getMaxV() - vOffset).light(lightAbove).overlay(overlay).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrix, fluidWidth, 0.0f, fluidWidth).color(color).texture(fluid.getMaxU() - uOffset, fluid.getMaxV() - vOffset).light(lightAbove).overlay(overlay).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrix, fluidWidth, 0.0f, -fluidWidth).color(color).texture(fluid.getMaxU() - uOffset, fluid.getMinV() + vOffset).light(lightAbove).overlay(overlay).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrix, -fluidWidth, 0.0f, -fluidWidth).color(color).texture(fluid.getMinU() + uOffset, fluid.getMinV() + vOffset).light(lightAbove).overlay(overlay).normal(0.0f, 1.0f, 0.0f).next();

        matrices.pop();
    }
}
