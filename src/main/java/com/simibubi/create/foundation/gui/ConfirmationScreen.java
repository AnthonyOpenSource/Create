package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

public class ConfirmationScreen extends AbstractSimiScreen {

	private Screen source;
	private Consumer<Boolean> action = _success -> {};
	private List<ITextProperties> text = new ArrayList<>();
	private boolean centered = false;
	private int x;
	private int y;
	private int textWidth;
	private int textHeight;

	private BoxWidget confirm;
	private BoxWidget cancel;
	private BoxElement textBackground;

	/*
	* Removes text lines from the back of the list
	* */
	public ConfirmationScreen removeTextLines(int amount) {
		if (amount > text.size())
			return clearText();

		text.subList(text.size() - amount, text.size()).clear();
		return this;
	}

	public ConfirmationScreen clearText() {
		this.text.clear();
		return this;
	}

	public ConfirmationScreen addText(ITextProperties text) {
		this.text.add(text);
		return this;
	}

	public ConfirmationScreen withText(ITextProperties text) {
		return clearText().addText(text);
	}

	public ConfirmationScreen at(int x, int y) {
		this.x = Math.max(x, 0);
		this.y = Math.max(y, 0);
		this.centered = false;
		return this;
	}

	public ConfirmationScreen centered() {
		this.centered = true;
		return this;
	}

	public ConfirmationScreen withAction(Consumer<Boolean> action) {
		this.action = action;
		return this;
	}

	public void open(@Nonnull Screen source) {
		this.source = source;
		Minecraft client = source.getMinecraft();
		this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
		this.client.currentScreen = this;
	}

	@Override
	public void tick() {
		super.tick();
		confirm.tick();
		cancel.tick();
	}

	@Override
	protected void init() {
		widgets.clear();

		ArrayList<ITextProperties> copy = new ArrayList<>(text);
		text.clear();
		copy.forEach(t -> text.addAll(client.fontRenderer.getTextHandler().wrapLines(t, 300, Style.EMPTY)));

		textHeight = text.size() * (client.fontRenderer.FONT_HEIGHT + 1) + 4;
		textWidth = 300;

		if (centered) {
			x = width/2 - textWidth/2 - 2;
			y = height/2 - textHeight/2 - 16;
		} else {
			x = Math.max(0, x - textWidth / 2);
			y = Math.max(0, y -= textHeight);
		}

		if (x + textWidth > width) {
			x = width - textWidth;
		}

		if (y + textHeight + 30 > height) {
			y = height - textHeight - 30;
		}

		TextStencilElement confirmText = new TextStencilElement(client.fontRenderer, "Confirm").centered(true, true);
		confirm = new BoxWidget(x + 4, y + textHeight + 2 , textWidth/2 - 10, 20)
				.withCallback(() -> accept(true));
		confirm.showingElement(confirmText.withElementRenderer(BoxWidget.gradientFactory.apply(confirm)));

		TextStencilElement cancelText = new TextStencilElement(client.fontRenderer, "Cancel").centered(true, true);
		cancel = new BoxWidget(x + textWidth/2 + 6, y + textHeight + 2, textWidth/2 - 10, 20)
				.withCallback(() -> accept(false));
		cancel.showingElement(cancelText.withElementRenderer(BoxWidget.gradientFactory.apply(cancel)));

		widgets.add(confirm);
		widgets.add(cancel);

		textBackground = new BoxElement()
				.gradientBorder(Theme.p(Theme.Key.BUTTON_DISABLE))
				.withBounds(textWidth, textHeight)
				.at(x, y);

	}

	@Override
	public void onClose() {
		accept(false);
	}

	private void accept(boolean success) {
		client.currentScreen = source;
		action.accept(success);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {

		textBackground.render(ms);
		int offset = client.fontRenderer.FONT_HEIGHT + 1;
		int lineY = y - offset;

		ms.push();
		ms.translate(0, 0, 200);

		for (ITextProperties line : text) {
			lineY = lineY + offset;

			if (line == null)
				continue;

			client.fontRenderer.draw(ms, line.getString(), x, lineY, 0xeaeaea);
		}

		ms.pop();

	}

	@Override
	protected void renderWindowBackground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {

		UIRenderHelper.framebuffer.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);

		ms.push();
		UIRenderHelper.framebuffer.bindFramebuffer(true);
		source.render(ms, mouseX, mouseY, 10);
		UIRenderHelper.framebuffer.unbindFramebuffer();
		Framebuffer mainBuffer = Minecraft.getInstance().getFramebuffer();
		ms.pop();

		//fixme replace with glVersioned-backend calls once they are merged from jozu's branch
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, UIRenderHelper.framebuffer.framebufferObject);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainBuffer.framebufferObject);
		GL30.glBlitFramebuffer(0, 0, mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, 0, 0,  mainBuffer.framebufferWidth, mainBuffer.framebufferHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
		mainBuffer.bindFramebuffer(true);

		this.fillGradient(ms, 0, 0, this.width, this.height, 0x70101010, 0x80101010);
	}

	@Override
	public void resize(@Nonnull Minecraft client, int width, int height) {
		super.resize(client, width, height);
		source.resize(client, width, height);
	}
}
