package io.github.gamermax1254.tlcountdown.client;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

/**
 * Рисует одну 7-сегментную цифру.
 * Координаты рисуются в СВОЕЙ локальной системе: (0,0) — левый-верхний угол цифры.
 * Размеры одной цифры: width x height (задаются извне).
 */
public final class SevenSegmentRenderer {

    private SevenSegmentRenderer() {}

    // Битмаска сегментов для цифр 0-9: биты a=1, b=2, c=4, d=8, e=16, f=32, g=64
    private static final int[] DIGIT_SEGMENTS = {
            0b0111111, // 0: a,b,c,d,e,f
            0b0000110, // 1: b,c
            0b1011011, // 2: a,b,d,e,g
            0b1001111, // 3: a,b,c,d,g
            0b1100110, // 4: b,c,f,g
            0b1101101, // 5: a,c,d,f,g
            0b1111101, // 6: a,c,d,e,f,g
            0b0000111, // 7: a,b,c
            0b1111111, // 8: all
            0b1101111  // 9: a,b,c,d,f,g
    };

    /**
     * Рисует одну цифру.
     *
     * @param width       ширина цифры в тех же единицах, что и остальные координаты
     * @param height      высота цифры
     * @param thickness   толщина сегмента (обычно ~1/5 от высоты)
     * @param color       ARGB
     */
    public static void renderDigit(
            MatrixStack poseStack,
            VertexConsumerProvider buffers,
            int digit,
            float width,
            float height,
            float thickness,
            int color,
            int packedLight
    ) {
        if (digit < 0 || digit > 9) return;
        int mask = DIGIT_SEGMENTS[digit];

        VertexConsumer vc = buffers.getBuffer(RenderLayer.getGui()); // подойдёт для плоских квадов
        // На самом деле для 3D-квадов лучше использовать иной RenderLayer,
        // но getGui даст плоский цветной результат. Ниже — правильный вариант:

        // Используем layer из "no-texture" — подходит для цветных квадов в мире
        vc = buffers.getBuffer(RenderLayer.getDebugQuads());

        float w = width;
        float h = height;
        float t = thickness;
        float mid = h / 2.0f;

        // Сегменты рисуются как прямоугольники в плоскости XY, лицом в +Z
        //  a — верхний горизонтальный
        //  b — правый верхний вертикальный
        //  c — правый нижний вертикальный
        //  d — нижний горизонтальный
        //  e — левый нижний вертикальный
        //  f — левый верхний вертикальный
        //  g — средний горизонтальный

        if ((mask & 0b0000001) != 0) drawRect(vc, poseStack, 0,          0,          w,          t,          color, packedLight); // a
        if ((mask & 0b0000010) != 0) drawRect(vc, poseStack, w - t,      0,          t,          mid,        color, packedLight); // b
        if ((mask & 0b0000100) != 0) drawRect(vc, poseStack, w - t,      mid,        t,          mid,        color, packedLight); // c
        if ((mask & 0b0001000) != 0) drawRect(vc, poseStack, 0,          h - t,      w,          t,          color, packedLight); // d
        if ((mask & 0b0010000) != 0) drawRect(vc, poseStack, 0,          mid,        t,          mid,        color, packedLight); // e
        if ((mask & 0b0100000) != 0) drawRect(vc, poseStack, 0,          0,          t,          mid,        color, packedLight); // f
        if ((mask & 0b1000000) != 0) drawRect(vc, poseStack, 0,          mid - t/2,  w,          t,          color, packedLight); // g
    }

    private static void drawRect(
            VertexConsumer vc,
            MatrixStack poseStack,
            float x, float y, float w, float h,
            int color,
            int packedLight
    ) {
        Matrix4f m = poseStack.peek().getPositionMatrix();
        Matrix3f n = poseStack.peek().getNormalMatrix();

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // 2 треугольника (4 вершины) в плоскости Z=0
        vc.vertex(m, x,     y,     0).color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(n, 0, 0, 1).next();
        vc.vertex(m, x,     y + h, 0).color(r, g, b, a).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(n, 0, 0, 1).next();
        vc.vertex(m, x + w, y + h, 0).color(r, g, b, a).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(n, 0, 0, 1).next();
        vc.vertex(m, x + w, y,     0).color(r, g, b, a).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(n, 0, 0, 1).next();
    }

    /**
     * Рисует одиночный горизонтальный сегмент (дефис "-") в центре области.
     */
    public static void renderDash(
            MatrixStack poseStack,
            VertexConsumerProvider buffers,
            float width,
            float height,
            float thickness,
            int color,
            int packedLight
    ) {
        // Только сегмент "g" — по центру по вертикали
        VertexConsumer vc = buffers.getBuffer(RenderLayer.getDebugQuads());
        float mid = height / 2.0f;
        drawRect(vc, poseStack, 0, mid - thickness / 2.0f, width, thickness, color, packedLight);
    }
}