package io.github.gamermax1254.tlcountdown.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;

public final class CountdownTextRenderer {

    private CountdownTextRenderer() {}

    private static final int BLINK_VISIBLE_TICKS = 10;
    private static final int BLINK_HIDDEN_TICKS = 10;

    public static void renderOnSlot(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int slotIndex,
            int segments,
            int seconds,
            boolean stale,
            int color,
            int packedLight,
            long clientTick
    ) {
        final float digitWidth = 1.4f;
        final float digitHeight = 2.8f;
        final float digitThickness = 0.4f;
        final float digitSpacing = 0.4f;

        float centerX = 8.0f;
        float centerY = 5.5f + slotIndex * 5.0f - 2.0f;
        float centerZ = 14.1f;

        boolean isDash = stale;
        boolean blink = stale || seconds > 99;
        int displayNumber = Math.min(seconds, 99);

        if (blink) {
            long phase = clientTick % (BLINK_VISIBLE_TICKS + BLINK_HIDDEN_TICKS);
            if (phase >= BLINK_VISIBLE_TICKS) return;
        }

        int len = 2;
        float totalWidth = len * digitWidth + (len - 1) * digitSpacing;

        poseStack.pushPose();
        poseStack.translate(centerX, centerY, centerZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(-1.0f, 1.0f, 1.0f);
        poseStack.translate(-totalWidth / 2.0f, -digitHeight / 2.0f, 0);

        for (int i = 0; i < len; i++) {
            if (isDash) {
                SevenSegmentRenderer.renderDash(
                        poseStack, buffers,
                        digitWidth, digitHeight, digitThickness,
                        color, packedLight
                );
            } else {
                int digit = (i == 0) ? (displayNumber / 10) : (displayNumber % 10);
                SevenSegmentRenderer.renderDigit(
                        poseStack, buffers,
                        digit,
                        digitWidth, digitHeight, digitThickness,
                        color, packedLight
                );
            }
            poseStack.translate(digitWidth + digitSpacing, 0, 0);
        }

        poseStack.popPose();
    }
}