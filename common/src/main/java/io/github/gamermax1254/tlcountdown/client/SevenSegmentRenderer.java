package io.github.gamermax1254.tlcountdown.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class SevenSegmentRenderer {

    private SevenSegmentRenderer() {}

    private static final int[] DIGIT_SEGMENTS = {
            0b0111111, // 0
            0b0000110, // 1
            0b1011011, // 2
            0b1001111, // 3
            0b1100110, // 4
            0b1101101, // 5
            0b1111101, // 6
            0b0000111, // 7
            0b1111111, // 8
            0b1101111  // 9
    };

    public static void renderDigit(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int digit,
            float width,
            float height,
            float thickness,
            int color,
            int packedLight
    ) {
        if (digit < 0 || digit > 9) return;
        int mask = DIGIT_SEGMENTS[digit];

        VertexConsumer vc = buffers.getBuffer(RenderType.debugQuads());

        float w = width;
        float h = height;
        float t = thickness;
        float mid = h / 2.0f;

        if ((mask & 0b0000001) != 0) drawRect(vc, poseStack, 0,     0,           w,     t,     color, packedLight); // a
        if ((mask & 0b0000010) != 0) drawRect(vc, poseStack, w - t, 0,           t,     mid,   color, packedLight); // b
        if ((mask & 0b0000100) != 0) drawRect(vc, poseStack, w - t, mid,         t,     mid,   color, packedLight); // c
        if ((mask & 0b0001000) != 0) drawRect(vc, poseStack, 0,     h - t,       w,     t,     color, packedLight); // d
        if ((mask & 0b0010000) != 0) drawRect(vc, poseStack, 0,     mid,         t,     mid,   color, packedLight); // e
        if ((mask & 0b0100000) != 0) drawRect(vc, poseStack, 0,     0,           t,     mid,   color, packedLight); // f
        if ((mask & 0b1000000) != 0) drawRect(vc, poseStack, 0,     mid - t/2,   w,     t,     color, packedLight); // g
    }

    public static void renderDash(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float width,
            float height,
            float thickness,
            int color,
            int packedLight
    ) {
        VertexConsumer vc = buffers.getBuffer(RenderType.debugQuads());
        float mid = height / 2.0f;
        drawRect(vc, poseStack, 0, mid - thickness / 2.0f, width, thickness, color, packedLight);
    }

    private static void drawRect(
            VertexConsumer vc,
            PoseStack poseStack,
            float x, float y, float w, float h,
            int color,
            int packedLight
    ) {
        Matrix4f m = poseStack.last().pose();
        Matrix3f n = poseStack.last().normal();

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        vc.vertex(m, x,     y,     0).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(n, 0, 0, 1).endVertex();
        vc.vertex(m, x,     y + h, 0).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(n, 0, 0, 1).endVertex();
        vc.vertex(m, x + w, y + h, 0).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(n, 0, 0, 1).endVertex();
        vc.vertex(m, x + w, y,     0).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(n, 0, 0, 1).endVertex();
    }
}