package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.VersionNewsInfo;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;

public final class WndRebornWelcome extends WndScrollable {
    private static final int PAD = 16;

    public WndRebornWelcome(String message) {
        super(0, 0, new NinePatch(TextureCache.createSolid(0xFF12101C), 0));
        ColorBlock dim = new ColorBlock(PixelScene.uiCamera.width, PixelScene.uiCamera.height, 0xAD05060D);
        dim.camera = PixelScene.uiCamera;
        addToBack(dim);
        shadow.am = .65f;

        boolean portrait = Game.height > Game.width;
        int width = Math.min(boundedWidth(portrait ? 232 : 272),
                (int)(Game.width / camera.zoom) - 32);
        int textWidth = width - PAD * 2 - SCROLL_GUTTER;

        Group frame = new Group();
        add(frame);
        Component header = new Component();
        BitmapText title = PixelScene.createText(Messages.get(WndWelcome.class, "title"), 18);
        title.hardlight(0xF1E8DB);
        title.measure();
        title.x = PAD;
        title.y = PixelScene.align(32 - title.height() / 2);
        header.add(title);
        Image emblem = Icons.SKILLS.get();
        emblem.scale.set(1.4f);
        emblem.x = PixelScene.align(width - PAD - emblem.width());
        emblem.y = PixelScene.align(32 - emblem.height() / 2);
        emblem.hardlight(0xD4BDD9);
        header.add(emblem);
        line(header, PAD, 53, width - PAD * 2, 1, 0xFF65513E);
        line(header, PAD, 54, width - PAD * 2, 1, 0xFF090910);
        header.setSize(width, 66);

        Component content = new Component();
        RenderedTextMultiline greeting = paragraph(message, 9, 0xE8E1F0, textWidth);
        greeting.setPos(PAD, 0);
        content.add(greeting);
        RenderedTextMultiline description = paragraph(Messages.get(WndWelcome.class, "reborn"),
                9, 0xBEB1CC, textWidth);
        description.setPos(PAD, greeting.bottom() + 12);
        content.add(description);
        content.setSize(width - SCROLL_GUTTER, description.bottom() + PAD);

        layoutContent(width, header, content, 0);
        decorateFrame(frame, width, height);
    }

    private static void decorateFrame(Group frame, int width, int height) {
        NinePatch rim = new NinePatch(Assets.CHROME, 0, 0, 22, 22, 7);
        rim.size(width, height);
        rim.hardlight(0xC6B5C9);
        frame.add(rim);

        masonry(frame, 7, 7, width - 14, height - 14);
        line(frame, 7, 7, width - 14, height - 14, 0xB80C0C16);
        line(frame, 11, 58, width - 22, height - 69, 0xC410101B);

        line(frame, 7, 7, width - 14, 1, 0xFF87735D);
        line(frame, 7, 8, 1, height - 16, 0xFF514650);
        line(frame, width - 8, 8, 1, height - 16, 0xFF080910);
        line(frame, 8, height - 8, width - 16, 1, 0xFF080910);
        line(frame, 10, 10, width - 20, 1, 0xFF18151E);
        line(frame, 10, 11, 1, height - 21, 0xFF18151E);
        line(frame, width - 11, 10, 1, height - 20, 0xFF3C333F);
        line(frame, 10, height - 11, width - 20, 1, 0xFF3C333F);
    }

    private static void masonry(Group group, int x, int y, int width, int height) {
        for (int row = 0; row < height; row += 16) {
            for (int col = 0; col < width; col += 16) {
                Image stone = new Image(Assets.TILES_CITY_3D, 0, 64,
                        Math.min(16, width - col), Math.min(16, height - row));
                stone.x = x + col;
                stone.y = y + row;
                stone.hardlight(0x94859D);
                group.add(stone);
            }
        }
    }

    private static RenderedTextMultiline paragraph(String message, int size, int color, int width) {
        RenderedTextMultiline text = PixelScene.renderMultiline(message, size);
        text.hardlight(color);
        text.maxWidth(width);
        return text;
    }

    private static void line(Group group, float x, float y, float w, float h, int color) {
        ColorBlock block = new ColorBlock(w, h, color);
        block.x = x;
        block.y = y;
        group.add(block);
    }

    @Override
    public void hide() {
        VersionNewsInfo.closeWelcome();
        super.hide();
    }
}
