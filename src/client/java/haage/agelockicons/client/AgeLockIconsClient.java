package haage.agelockicons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class AgeLockIconsClient implements ClientModInitializer {
	private static final String HELD_ITEM_PATH = "golden_dandelion";
	private static final int SEARCH_RADIUS_BLOCKS = 15;
	private static final Identifier ICON_TEXTURE =
			Identifier.fromNamespaceAndPath("agelock-icons", "textures/age_lock.png");
	private static final int FULL_BRIGHT_LIGHT = 0x00F000F0;

	@Override
	public void onInitializeClient() {
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(AgeLockIconsClient::renderAgeLockIcons);
	}

	private static void renderAgeLockIcons(LevelRenderContext context) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null) {
			return;
		}

		if (!isHoldingGoldenDandelion(minecraft.player)) {
			return;
		}

		PoseStack poseStack = context.poseStack();
		MultiBufferSource.BufferSource bufferSource = context.bufferSource();
		if (poseStack == null || bufferSource == null) {
			return;
		}

		Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
		double maxDistanceSqr = SEARCH_RADIUS_BLOCKS * SEARCH_RADIUS_BLOCKS;

		for (Entity entity : minecraft.level.entitiesForRendering()) {
			if (!(entity instanceof AgeableMob ageableMob)) {
				continue;
			}

			if (ageableMob.isRemoved() || ageableMob.distanceToSqr(minecraft.player) > maxDistanceSqr) {
				continue;
			}

			if (ageableMob.isAgeLocked()) {
				renderIconAboveMob(minecraft, poseStack, bufferSource, cameraPos, ageableMob, 0.85D);
			}
		}
	}

	private static boolean isHoldingGoldenDandelion(Player player) {
		return isGoldenDandelion(player.getMainHandItem()) || isGoldenDandelion(player.getOffhandItem());
	}

	private static void renderIconAboveMob(
			Minecraft minecraft,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			Vec3 cameraPos,
			AgeableMob mob,
			double yOffset
	) {
		double x = mob.getX() - cameraPos.x;
		double y = mob.getY() + mob.getBbHeight() + yOffset - cameraPos.y;
		double z = mob.getZ() - cameraPos.z;

		poseStack.pushPose();
		poseStack.translate(x, y, z);
		poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
		poseStack.scale(0.025F, -0.025F, 0.025F);

		VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.entityTranslucentEmissive(ICON_TEXTURE));
		PoseStack.Pose lastPose = poseStack.last();
		float s = 8.0F;

		consumer.addVertex(lastPose, -s, -s, 0f)
				.setColor(255, 255, 255, 255).setUv(0f, 0f)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT_LIGHT)
				.setNormal(0f, 1f, 0f);
		consumer.addVertex(lastPose, s, -s, 0f)
				.setColor(255, 255, 255, 255).setUv(1f, 0f)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT_LIGHT)
				.setNormal(0f, 1f, 0f);
		consumer.addVertex(lastPose, s, s, 0f)
				.setColor(255, 255, 255, 255).setUv(1f, 1f)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT_LIGHT)
				.setNormal(0f, 1f, 0f);
		consumer.addVertex(lastPose, -s, s, 0f)
				.setColor(255, 255, 255, 255).setUv(0f, 1f)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT_LIGHT)
				.setNormal(0f, 1f, 0f);

		poseStack.popPose();
	}

	private static boolean isGoldenDandelion(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		Identifier identifier = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return identifier != null && HELD_ITEM_PATH.equals(identifier.getPath());
	}
}