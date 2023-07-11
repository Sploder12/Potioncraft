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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionCauldronBlockEntity;

@Environment(EnvType.CLIENT)
public class PotionCauldronRenderer implements BlockEntityRenderer<PotionCauldronBlockEntity> {

    private static final Sprite water = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, SimpleFluidRenderHandler.WATER_STILL).getSprite();
    private static final int AtlasId = MinecraftClient.getInstance().getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getGlId();

    private static final LightmapTextureManager lightmapManager = MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager();

    private static final float waterWidth = (12.0f / 16.0f) / 2.0f;

    public PotionCauldronRenderer(BlockEntityRendererFactory.Context ctx) {
        //water = MinecraftClient.getInstance().getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getSprite(SimpleFluidRenderHandler.WATER_STILL);
    }

    @Override
    public void render(PotionCauldronBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        BlockPos posUp = entity.getPos().add(0, 1, 0);

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

        buffer.vertex(matrices.peek().getPositionMatrix(), -waterWidth, 0.0f, waterWidth).color(color).texture(water.getMinU(), water.getMaxV()).light(lightAbove).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), waterWidth, 0.0f, waterWidth).color(color).texture( water.getMaxU(), water.getMaxV()).light(lightAbove).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), waterWidth, 0.0f, -waterWidth).color(color).texture(water.getMaxU(), water.getMinV()).light(lightAbove).normal(0.0f, 1.0f, 0.0f).next();
        buffer.vertex(matrices.peek().getPositionMatrix(), -waterWidth, 0.0f, -waterWidth).color(color).texture(water.getMinU(), water.getMinV()).light(lightAbove).normal(0.0f, 1.0f, 0.0f).next();

        tessellator.draw();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        matrices.pop();
    }
}
