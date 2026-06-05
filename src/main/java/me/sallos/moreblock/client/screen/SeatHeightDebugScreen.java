package me.sallos.moreblock.client.screen;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.network.message.ApplySeatHeightDebugMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SeatHeightDebugScreen extends Screen {
    private static final double MAX_ABSOLUTE_SEAT_PIXELS = 32.0d;
    private static final double PIXEL_STEP = 0.25d;

    private final String displayName;
    private final String registryName;
    private double currentSeatHeight;
    private double lastSentSeatHeight;

    public SeatHeightDebugScreen(String displayName, String registryName, double seatHeight) {
        super(Component.literal("MoreBlock 坐高调试"));
        this.displayName = displayName;
        this.registryName = registryName;
        currentSeatHeight = clampSeatHeight(seatHeight);
        lastSentSeatHeight = currentSeatHeight;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int top = height / 2 - 30;
        addRenderableWidget(new SeatHeightSlider(centerX - 110, top, 220, 20, currentSeatHeight));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(centerX - 40, top + 52, 80, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        int textY = height / 2 - 72;
        guiGraphics.drawCenteredString(font, title, centerX, textY, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, displayName + " [" + registryName + "]", centerX, textY + 14, 0xC8C8C8);
        guiGraphics.drawCenteredString(font, buildValueText(currentSeatHeight), centerX, textY + 28, 0xFFD37F);
        guiGraphics.drawCenteredString(font, "中间是 0，左边更低，右边更高", centerX, textY + 42, 0xA0A0A0);
        guiGraphics.drawCenteredString(font, "拖动滑条后会实时应用到当前正在坐的座椅", centerX, textY + 54, 0xA0A0A0);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updateSeatHeight(double seatHeight) {
        currentSeatHeight = clampSeatHeight(seatHeight);
        if (Math.abs(currentSeatHeight - lastSentSeatHeight) < 0.0001d) {
            return;
        }
        lastSentSeatHeight = currentSeatHeight;
        Moreblock.PACKET_HANDLER.sendToServer(new ApplySeatHeightDebugMessage(currentSeatHeight));
    }

    private static double clampSeatHeight(double seatHeight) {
        return Mth.clamp(seatHeight, -2.0d, 2.0d);
    }

    private static double snapPixels(double pixels) {
        return Math.round(Mth.clamp(pixels, -MAX_ABSOLUTE_SEAT_PIXELS, MAX_ABSOLUTE_SEAT_PIXELS) / PIXEL_STEP) * PIXEL_STEP;
    }

    private static double pixelsToHeight(double pixels) {
        return snapPixels(pixels) / 16.0d;
    }

    private static double heightToPixels(double seatHeight) {
        return snapPixels(clampSeatHeight(seatHeight) * 16.0d);
    }

    private static double sliderValueToPixels(double sliderValue) {
        return snapPixels((sliderValue - 0.5d) * 2.0d * MAX_ABSOLUTE_SEAT_PIXELS);
    }

    private static double pixelsToSliderValue(double pixels) {
        return (snapPixels(pixels) / (2.0d * MAX_ABSOLUTE_SEAT_PIXELS)) + 0.5d;
    }

    private static String formatValue(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }

    private static String buildValueText(double seatHeight) {
        double seatPixels = heightToPixels(seatHeight);
        return "当前坐高: " + formatValue(seatHeight) + " 格 / " + formatValue(seatPixels) + " px";
    }

    private final class SeatHeightSlider extends AbstractSliderButton {
        private SeatHeightSlider(int x, int y, int width, int height, double seatHeight) {
            super(x, y, width, height, CommonComponents.EMPTY, pixelsToSliderValue(heightToPixels(seatHeight)));
            updateMessage();
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            int centerX = getX() + getWidth() / 2;
            guiGraphics.fill(centerX, getY() - 2, centerX + 1, getY() + getHeight() + 2, 0xC0FFD37F);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("拖动调整: " + buildValueText(getSeatHeight())));
        }

        @Override
        protected void applyValue() {
            updateSeatHeight(getSeatHeight());
        }

        private double getSeatHeight() {
            return pixelsToHeight(sliderValueToPixels(value));
        }
    }
}
