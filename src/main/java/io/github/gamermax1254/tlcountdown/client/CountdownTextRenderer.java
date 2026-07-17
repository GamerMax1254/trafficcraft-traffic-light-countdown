package io.github.gamermax1254.tlcountdown.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public final class CountdownTextRenderer {

    private CountdownTextRenderer() {}

    private static final int BLINK_VISIBLE_TICKS = 10;
    private static final int BLINK_HIDDEN_TICKS = 10;

    public static void renderOnSlot(
            MatrixStack poseStack,
            VertexConsumerProvider buffers,
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

        // Мигание
        if (blink) {
            long phase = clientTick % (BLINK_VISIBLE_TICKS + BLINK_HIDDEN_TICKS);
            if (phase >= BLINK_VISIBLE_TICKS) return; // невидимая фаза
        }

        int len = 2;
        float totalWidth = len * digitWidth + (len - 1) * digitSpacing;

        poseStack.push();
        poseStack.translate(centerX, centerY, centerZ);

// Разворот к игроку: rotate(180°) поворачивает лицом,
// а scale(-1,1,1) компенсирует зеркалирование по X
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        poseStack.scale(-1.0f, 1.0f, 1.0f);

// Начало отрисовки — левый-верхний угол
        poseStack.translate(-totalWidth / 2.0f, -digitHeight / 2.0f, 0);

// Прямой порядок: i=0 → десятки (слева), i=1 → единицы (справа)
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

        poseStack.pop();
    }
}