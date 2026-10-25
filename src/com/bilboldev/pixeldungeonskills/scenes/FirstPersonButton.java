package com.bilboldev.pixeldungeonskills.scenes;

import android.graphics.Bitmap;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.input.GameActions;
import com.bilboldev.input.GameAction;
import com.bilboldev.input.ControlSettings;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.RebornHud;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;

final class FirstPersonButton extends Button {
    private NinePatch background;
    private Image icon;

    @Override
    protected void createChildren() {
        super.createChildren();
        SmartTexture art = artwork();
        background = new NinePatch(art, 48, 0, 3, 3, 1);
        add(background);
        icon = new Image(art);
        add(icon);
        viewMode(2);
        bindAction(GameAction.VIEW_MODE);
    }

    @Override
    protected void layout() {
        super.layout();

        background.x = x;
        background.y = y + 1;
        background.size(width, height - 1);
        icon.scale.set(Math.min(1, Math.min((width - 4) / 16, (height - 2) / 14)));
        icon.x = x + (float)Math.floor((width - icon.width()) / 2);
        icon.y = y + (float)Math.floor((height - icon.height()) / 2);
    }

    void viewMode(int mode) {
        int next = (mode + 1) % 3;
        icon.frame(next * 16, 0, 16, 14);
    }

    @Override
    protected void onClick() {
        GameScene.cycleView();
    }

    @Override
    protected void onTouchDown() {
        background.brightness(1.4f);
        icon.brightness(1.4f);
        Sample.INSTANCE.play(Assets.SND_CLICK);
    }

    @Override
    protected void onTouchUp() {
        background.resetColor();
        icon.resetColor();
    }

    @Override
    protected boolean onLongClick() {
        GameScene.show(new WndMessage(helpText()));
        return true;
    }

    static String helpText() {
        String help = Messages.get(FirstPersonButton.class, "title") + "\n\n"
                + Messages.get(FirstPersonButton.class, "cycle");
        if (GameActions.enabled && ControlSettings.backend != null) {
            help += "\n\n" + Messages.get(FirstPersonButton.class, "mouse_help") + "\n\n"
                    + Messages.get(FirstPersonButton.class, "controller_help",
                    ControlSettings.backend.bindingName(GameAction.POINTER_SECONDARY, true),
                    ControlSettings.backend.bindingName(GameAction.POINTER_PRIMARY, true));
        }
        return help + "\n\n" + Messages.get(FirstPersonButton.class, "visibility");
    }

    private static SmartTexture artwork() {
        if (!TextureCache.contains(FirstPersonButton.class)) {

            String[][] glyphs = {
                    {
                        "................", "................", ".....oooooo.....", "...ooSSSSSSoo...", "..oSSSSSSSSSSo..", ".oSSSSddddSSSSo.", "oSSSSddGGddSSSSo", ".oSSSddGGddSSSo.", "..oSSSddddSSSo..", "...ooSSSSSSoo...", ".....oooooo.....", "................", "................", "................"
                    },
                    {
                        "................", ".SS...oooo...SS.", "..S..oddddo..S..", "..S..oddddo..S..", "...S.oddddo.S...", "...S..oooo..S...", "....SoGGGGoS....", "....oGGGGGGo....", "...odGGGGGGdo...", "...odGGGGGGdo...", "..So.oooooo.oS..", ".S...oSooSo...S.", "SS...oSooSo...SS", ".....oo..oo....."
                    },
                    {
                        "................", ".oooooooooooooo.", ".oSSSoddddoSSSo.", ".oSSSoddddoSSSo.", ".oooooooooooooo.", ".odddoGGGGodddo.", ".odddoGGGGodddo.", ".odddoGGGGodddo.", ".oooooooooooooo.", ".oSSSoddddoSSSo.", ".oSSSoddddoSSSo.", ".oooooooooooooo.", "................", "................"
                    }
            };
            Bitmap bitmap = Bitmap.createBitmap(51, 14, Bitmap.Config.ARGB_8888);
            for (int mode = 0; mode < glyphs.length; mode++) {
                for (int y = 0; y < 14; y++) for (int x = 0; x < 16; x++) {
                    char pixel = glyphs[mode][y].charAt(x);
                    int color = pixel == 'o' ? 0xFF313329 : pixel == 'S' ? 0xFFD0D2B2
                            : pixel == 'G' ? 0xFFE6C86E : pixel == 'd' ? 0xFF52564C : 0;
                    bitmap.setPixel(mode * 16 + x, y, color);
                }
            }

            Bitmap toolbar = TextureCache.get(Assets.TOOLBAR).bitmap;
            for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++)
                bitmap.setPixel(48 + x, y, toolbar.getPixel(107 + (x == 2 ? 19 : x), 8 + (y == 2 ? 23 : y)));
            TextureCache.add(FirstPersonButton.class, new SmartTexture(bitmap));
        }
        return RebornHud.texture(FirstPersonButton.class);
    }
}
