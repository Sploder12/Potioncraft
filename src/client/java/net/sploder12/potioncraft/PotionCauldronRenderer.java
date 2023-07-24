package net.sploder12.potioncraft;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class PotionCauldronRenderer implements BlockEntityRenderer<PotionCauldronBlockEntity> {

    private static final Sprite water = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, SimpleFluidRenderHandler.WATER_STILL).getSprite();
    private static final int AtlasId = MinecraftClient.getInstance().getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getGlId();

    private static final LightmapTextureManager lightmapManager = MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager();

    private static final float waterPixWidth = 12.0f;
    private static final float texPixWidth = 16.0f;

    private static final float waterWidth = (waterPixWidth / texPixWidth) / 2.0f;

    private static final float uvCorrection = ((texPixWidth - waterPixWidth) / texPixWidth) / 2.0f;


    public PotionCauldronRenderer(BlockEntityRendererFactory.Context ctx) {
        //water = MinecraftClient.getInstance().getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getSprite(SimpleFluidRenderHandler.WATER_STILL);
    }

    @Override
    public void render(PotionCauldronBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        World world = entity.getWorld();

        int lightAbove = light;
        if (world != null) {
            lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());
        }

        final int alpha = (int)(255.0 * 0.8);

        int color = entity.getColor() + (alpha << 24);

        double yOffset = PotionCauldronBlock.getFluidHeight(entity.getLevel());

        matrices.push();
        matrices.translate(0.5, yOffset, 0.5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeTranslucentProgram);
        RenderSystem.setShaderTexture(0, AtlasId);
        lightmapManager.enable();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);

        float uOffset = (water.getMaxU() - water.getMinU()) * uvCorrection;
        float vOffset = (water.getMaxV() - water.getMinV()) * uvCorrection;

        buffer.vertex(matrices.peek().getPositionMatrix(), -waterWidth, 0.0f, waterWidth).color(color).texture(water.getMinU() + uOffset, water.getMaxV() - vOffset).light(lightAbove).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), waterWidth, 0.0f, waterWidth).color(color).texture(water.getMaxU() - uOffset, water.getMaxV() - vOffset).light(lightAbove).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), waterWidth, 0.0f, -waterWidth).color(color).texture(water.getMaxU() - uOffset, water.getMinV() + vOffset).light(lightAbove).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), -waterWidth, 0.0f, -waterWidth).color(color).texture(water.getMinU() + uOffset, water.getMinV() + vOffset).light(lightAbove).normal(0.0f, 1.0f, 0.0f).next();

        tessellator.draw();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        matrices.pop();
    }
}
