/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.input.ControlSettings;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.Online.OnlineHero;
import com.bilboldev.pixeldungeonskills.actors.hero.GauntletHero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.messages.Languages;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.ClassicTitleScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;
import com.bilboldev.pixeldungeonskills.ui.CheckBox;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.Toolbar;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.Slider;

public class WndSettings extends WndScrollable {

	private static final String TXT_IMMERSIVE		= "Immersive mode";
	
	private static final String TXT_MUSIC	= "Music";
	
	private static final String TXT_SOUND	= "Sound FX";
	private static final String TXT_NO_DEGRADATION = "No Degradation";

	private static final String TXT_SWITCH_PORT	= "Switch to portrait";
	private static final String TXT_SWITCH_LAND	= "Switch to landscape";
	private static final String TXT_WINDOWED		= "Windowed Mode";
	private static final String TXT_BORDERLESS_WINDOWED = "Borderless Windowed";
	
	private static final int WIDTH		= 144;
	private static final int BTN_HEIGHT	= 20;
	private static final int GAP 		= 2;

	private RedButton btnBrightnessMinus;
	private RedButton btnBrightnessPlus;
	private RedButton btnBrightness;
	private RedButton btnLanguage;
	private CheckBox classicMusic, rebornMusic;
	private ControlRow settingsRows;
	private ScrollPane settingsPane;
	private float settingsX, settingsY, settingsWidth, settingsTarget, settingsGap;
	
	public WndSettings(boolean inGame) {
		super();
		if (Presentation.reborn()) createReborn(inGame);
		else createClassic(inGame);
	}

	private void createClassic(final boolean inGame) {
		int width = boundedWidth(WIDTH);
		settingsWidth = textWidth(width);
		settingsTarget = BTN_HEIGHT;
		if (!isDesktopRuntime()) settingsTarget = Math.max(settingsTarget, 48 * Game.density / camera.zoom);
		settingsGap = GAP;
		settingsRows = new ControlRow();

		if (inGame) {
			addDungeonZoomSlider();
		} else if (!PixelDungeon.supportsWindowedMode()) {
			CheckBox immersive = settingToggle(TXT_IMMERSIVE, PixelDungeon.immersed(), value -> PixelDungeon.immerse(value));
			immersive.enable(android.os.Build.VERSION.SDK_INT >= 19);
		}
		addUiScaleSlider(inGame);
		addQuickSlotSlider();
		addAudioSliders();
		btnLanguage = settingAction(languageText(), () -> showLanguagePicker(inGame));

		if (!inGame) {
			settingToggle(Messages.get(WndSettings.class, "title_animation"), PixelDungeon.titleAnimation(),
					value -> PixelDungeon.titleAnimation(value));
			if (onTitleScreen()) {
				settingAction(Messages.get(WndWelcome.class, "try_reborn"), () -> {
					hide();
					Presentation.switchTo(true);
				});
			}
		}
		if (ControlSettings.backend != null)
			settingAction(Messages.get(WndControls.class, "title"), () -> WndSettings.this.add(new WndControls()));

		if (inGame && !(Dungeon.hero instanceof Legend) && !(Dungeon.hero instanceof OnlineHero)
				&& !(Dungeon.hero instanceof GauntletHero)) {
			settingToggle(TXT_NO_DEGRADATION, PixelDungeon.itemDeg(), value -> PixelDungeon.itemDeg(value));
			addBrightnessControls();
		} else if (!inGame) {
			if (PixelDungeon.supportsWindowedMode()) addWindowModeControls();
			else settingAction(orientationText(), () -> PixelDungeon.landscape(!PixelDungeon.landscape()));
		}
		layoutSettings(width, null, settingsY - settingsGap);
	}

	private void createReborn(final boolean inGame) {
		boolean columns = Game.width > Game.height;
		int width = boundedWidth(columns ? 276 : 190);
		settingsWidth = columns ? (textWidth(width) - 8) / 2f : textWidth(width);
		settingsTarget = columns ? 18 : 20;
		if (!isDesktopRuntime()) settingsTarget = Math.max(settingsTarget, 48 * Game.density / camera.zoom);
		settingsGap = columns ? 2 : 3;
		settingsRows = new ControlRow();
		IconTitle heading = new IconTitle(Icons.PREFS.get(), Messages.maybeTranslate("Settings"));
		heading.setRect(0, 0, width, 0);

		section("audio");
		addAudioSliders();

		float columnWidth = settingsWidth;
		if (columns) {
			float audioHeight = settingsY;
			settingsX = columnWidth + 8;
			settingsY = 0;
			addRebornControls(inGame);
			settingsX = 0;
			settingsY = Math.max(audioHeight, settingsY);
			settingsWidth = textWidth(width);
		}
		section("display");
		settingsWidth = columnWidth;
		float displayTop = settingsY;
		addUiScaleSlider(inGame);
		addQuickSlotSlider();
		if (inGame) {
			addDungeonZoomSlider();
			addBrightnessControls();
		} else if (!PixelDungeon.supportsWindowedMode()) {
			if (columns) {
				settingAction(orientationText(), () -> PixelDungeon.landscape(!PixelDungeon.landscape()));
			}
		}
		float leftHeight = settingsY;
		if (columns) {
			settingsX = columnWidth + 8;
			settingsY = displayTop;
		}
		if (inGame) {
			if (columns) addRebornDungeonSettings();
		} else {
			settingToggle(Messages.get(WndSettings.class, "title_animation"), PixelDungeon.titleAnimation(),
					value -> PixelDungeon.titleAnimation(value));
			if (PixelDungeon.supportsWindowedMode()) {
				addWindowModeControls();
			} else {
				CheckBox immersive = settingToggle(TXT_IMMERSIVE, PixelDungeon.immersed(), value -> PixelDungeon.immerse(value));
				immersive.enable(android.os.Build.VERSION.SDK_INT >= 19);
				if (!columns) {
					settingAction(orientationText(), () -> PixelDungeon.landscape(!PixelDungeon.landscape()));
				}
			}
		}

		settingsY = Math.max(leftHeight, settingsY);
		if (!columns) {
			addRebornControls(inGame);
			if (inGame) addRebornDungeonSettings();
		}
		layoutSettings(width, heading, settingsY - settingsGap);
	}

	private void addRebornControls(boolean inGame) {
		section("controls_language");
		btnLanguage = settingAction(languageText(), () -> showLanguagePicker(inGame));
		if (ControlSettings.backend != null)
			settingAction(Messages.get(WndControls.class, "title"), () -> WndSettings.this.add(new WndControls()));
		if (!inGame && onTitleScreen()) {
			settingAction(Messages.get(WndWelcome.class, "switch_classic"), () -> {
				hide();
				Presentation.switchTo(false);
			});
		}
	}

	private void addRebornDungeonSettings() {
		if (!(Dungeon.hero instanceof Legend) && !(Dungeon.hero instanceof OnlineHero)
				&& !(Dungeon.hero instanceof GauntletHero)) {
			section("dungeon");
			settingToggle(TXT_NO_DEGRADATION, PixelDungeon.itemDeg(), value -> PixelDungeon.itemDeg(value));
		}
	}

	private void addAudioSliders() {
		Presentation.applyAudio();
		addSoundtrackChoice();
		settingSlider(new Slider(Messages.maybeTranslate(TXT_MUSIC), 0, 100, Presentation.musicVolume()) {
			@Override
			protected void onChange(int value) {
				Music.INSTANCE.masterVolume(value / 100f);
			}

			@Override
			protected void onRelease(int value) {
				Presentation.musicVolume(value);
			}
		});
		settingSlider(new Slider(Messages.maybeTranslate(TXT_SOUND), 0, 100, Presentation.soundVolume()) {
			@Override
			protected void onChange(int value) {
				Sample.INSTANCE.volume(value / 100f);
			}

			@Override
			protected void onRelease(int value) {
				Presentation.soundVolume(value);
				Sample.INSTANCE.play(Assets.SND_CLICK);
			}
		});
	}

	private void addSoundtrackChoice() {
		BitmapTextMultiline label = PixelScene.createMultiline(Messages.get(WndSettings.class, "soundtrack"), 8);
		label.maxWidth = (int)settingsWidth - 4;
		label.measure();
		label.hardlight(Presentation.reborn() ? 0xECE2D4 : 0xFFFFFF);
		label.x = settingsX + 2;
		label.y = settingsY + 1;
		settingsRows.add(label);
		settingsY += label.height() + 4;

		classicMusic = new SettingsToggle(Messages.get("custom.gameplay.classic"), value -> chooseSoundtrack(false));
		rebornMusic = new SettingsToggle(Messages.get("custom.gameplay.reborn"), value -> chooseSoundtrack(true));
		float width = (settingsWidth - 2) / 2;
		classicMusic.setRect(settingsX, settingsY, width, settingsTarget);
		rebornMusic.setRect(settingsX + width + 2, settingsY, width, settingsTarget);
		float height = Math.max(settingsTarget, Math.max(classicMusic.reqHeight(), rebornMusic.reqHeight()));
		classicMusic.setSize(width, height);
		rebornMusic.setSize(width, height);
		classicMusic.checked(!Presentation.rebornMusic());
		rebornMusic.checked(Presentation.rebornMusic());
		settingsRows.add(classicMusic);
		settingsRows.add(rebornMusic);
		settingsY += height + settingsGap;
	}

	private void chooseSoundtrack(boolean reborn) {
		classicMusic.checked(!reborn);
		rebornMusic.checked(reborn);
		Presentation.rebornMusic(reborn);
	}

	private void addUiScaleSlider(final boolean inGame) {
		final int minimum = PixelScene.minDefaultZoom;
		int maximum = PixelScene.maxDefaultZoom;
		int saved = PixelDungeon.uiScale();
		int step = saved < minimum || saved > maximum ? 0 : saved - minimum + 1;
		settingSlider(new Slider(Messages.get(WndSettings.class, "ui_scale_title"), 0, maximum - minimum + 1, step) {
			@Override
			protected String valueText(int value) {
				return value == 0 ? Messages.get(WndSettings.class, "ui_scale_auto") : (minimum + value - 1) + "x";
			}

			@Override
			protected void onRelease(int value) {
				int scale = value == 0 ? 0 : minimum + value - 1;
				if (scale == PixelDungeon.uiScale()) return;
				float scroll = settingsRows.camera.scroll.y;
				PixelDungeon.uiScale(scale);
				PixelScene.noFade = true;
				Game.resetScene(() -> {
					WndSettings settings = new WndSettings(inGame);
					Game.scene().add(settings);
					settings.settingsPane.scrollTo(0, scroll);
				});
			}
		});
	}

	private void addQuickSlotSlider() {
		int maximum = Toolbar.maxQuickSlots();
		settingSlider(new Slider(Messages.get(WndSettings.class, "quick_slots"),
				1, maximum, Math.min(Presentation.quickSlots(), maximum)) {
			@Override
			protected String valueText(int value) {
				return Integer.toString(value);
			}

			@Override
			protected void onRelease(int value) {
				Presentation.quickSlots(value);
			}
		});
	}

	private void addDungeonZoomSlider() {
		settingSlider(new Slider(Messages.get(WndSettings.class, "zoom"),
				(int)PixelScene.minZoom, (int)PixelScene.maxZoom, Math.round(Camera.main.zoom)) {
			@Override
			protected String valueText(int value) {
				return value + "x";
			}

			@Override
			protected void onChange(int value) {
				Camera.main.zoom(value);
			}

			@Override
			protected void onRelease(int value) {
				int offset = value - (int)PixelScene.defaultZoom;
				if (GauntletRun.active()) GauntletRun.zoomOffset(offset);
				else PixelDungeon.zoom(offset);
			}
		});
	}

	private void addBrightnessControls() {
		float small = settingsTarget, middle = settingsWidth - 2 * small - 4;
		btnBrightnessMinus = new SettingsButton("-", () -> modifyBrightness(-1));
		btnBrightnessPlus = new SettingsButton("+", () -> modifyBrightness(1));
		btnBrightness = new SettingsButton(getBrightnessText(PixelDungeon.brightnessNew()), () -> {});
		settingsRows.add(btnBrightnessMinus.setRect(settingsX, settingsY, small, settingsTarget));
		settingsRows.add(btnBrightness.setRect(settingsX + small + 2, settingsY, middle, settingsTarget));
		settingsRows.add(btnBrightnessPlus.setRect(settingsX + settingsWidth - small, settingsY, small, settingsTarget));
		settingsY += settingsTarget + settingsGap;
	}

	private void addWindowModeControls() {
		final CheckBox[] borderless = new CheckBox[1];
		CheckBox windowed = settingToggle(TXT_WINDOWED, PixelDungeon.windowedMode(), value -> {
			PixelDungeon.windowedMode(value);
			if (value && borderless[0] != null) borderless[0].checked(false);
		});
		if (PixelDungeon.supportsBorderlessWindowedMode())
			borderless[0] = settingToggle(TXT_BORDERLESS_WINDOWED, PixelDungeon.borderlessWindowedMode(), value -> {
				PixelDungeon.borderlessWindowedMode(value);
				if (value) windowed.checked(false);
			});
	}

	private void layoutSettings(int width, Component heading, float contentHeight) {
		settingsRows.setSize(textWidth(width), contentHeight);
		RedButton close = new ActionButton(Messages.get(WndControls.class, "close")) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setSize(width, settingsTarget);
		layoutContent(width, heading, settingsRows, 5, close);
	}

	private static boolean onTitleScreen() {
		return Game.scene() instanceof ClassicTitleScene
				|| (Game.scene() instanceof TitleScene && ((TitleScene)Game.scene()).page() == TitleScene.Page.HOME);
	}

	private interface ToggleAction {
		void set(boolean value);
	}

	private CheckBox settingToggle(String label, boolean value, final ToggleAction action) {
		CheckBox toggle = new SettingsToggle(label, action);
		toggle.checked(value);
		addSetting(toggle);
		return toggle;
	}

	private RedButton settingAction(String label, Runnable action) {
		RedButton button = new SettingsButton(label, action);
		addSetting(button);
		return button;
	}

	private void settingSlider(Slider slider) {
		slider.setRect(settingsX, settingsY, settingsWidth, settingsTarget + 12);
		slider.setSize(settingsWidth, slider.requiredHeight(isDesktopRuntime() ? 16 : settingsTarget));
		settingsRows.addSlider(slider);
		settingsY = slider.bottom() + settingsGap;
	}

	private void addSetting(RedButton button) {
		button.setRect(settingsX, settingsY, settingsWidth, settingsTarget);
		button.setSize(settingsWidth, Math.max(settingsTarget, button.reqHeight()));
		settingsRows.add(button);
		settingsY = button.bottom() + settingsGap;
	}

	private void section(String key) {
		if (settingsY > 0) settingsY += settingsGap + 1;
		BitmapText label = PixelScene.createText(Messages.get(WndSettings.class, key), 8);
		label.hardlight(titleColor());
		label.measure();
		label.x = settingsX + 2;
		label.y = settingsY + 2;
		settingsRows.add(label);
		ColorBlock rule = new ColorBlock(Math.max(1, settingsWidth - label.width() - 10), 1, 0xFF514253);
		rule.x = settingsX + label.width() + 8;
		rule.y = settingsY + 6;
		settingsRows.add(rule);
		settingsY += label.height() + settingsGap + 3;
	}

	private final class SettingsButton extends ActionButton {
		private final Runnable action;
		SettingsButton(String label, Runnable action) {
			super(label);
			this.action = action;
			settingsRows.addControl(hotArea);
		}

		@Override
		protected void onClick() {
			action.run();
		}
	}

	private final class SettingsToggle extends CheckBox {
		private final ToggleAction action;
		SettingsToggle(String label, ToggleAction action) {
			super(label);
			this.action = action;
			settingsRows.addControl(hotArea);
		}

		@Override
		protected void createChildren() {
			super.createChildren();
			remove(text);
			text.destroy();
			text = PixelScene.createMultiline(9);
			text.hardlight(Presentation.reborn() ? 0xECE2D4 : 0xFFFFFF);
			add(text);
		}

		@Override
		protected void layout() {
			((BitmapTextMultiline)text).maxWidth = Math.max(1, (int)width - 28);
			text.measure();
			super.layout();
			text.x = x + 5;
			text.y = PixelScene.align(y + (height - text.height()) / 2);
			if (icon != null) icon.x = right() - icon.width() - 5;
		}

		@Override
		public float reqHeight() {
			return text.height() + 8;
		}

		@Override
		protected void onClick() {
			super.onClick();
			action.set(checked());
		}
	}

	@Override
	protected ScrollPane createScrollPane(Component content) {
		settingsPane = settingsRows != null ? settingsRows.scrollPane(content) : super.createScrollPane(content);
		return settingsPane;
	}

	private String orientationText() {
		return PixelDungeon.landscape() ? TXT_SWITCH_PORT : TXT_SWITCH_LAND;
	}

	private void modifyBrightness(int change){
		int existing = PixelDungeon.brightnessNew();
		existing += change;
		if(existing < -2){
			existing = -2;
		}

		if(existing > 2){
			existing = 2;
		}

		PixelDungeon.brightnessNew(existing);
		btnBrightness.text(getBrightnessText(existing));
	}

	private String getBrightnessText(int existing){
		switch (existing){
			case -2: return "Lowest Brightness";
			case -1: return "Low Brightness";
			case 0: return "Normal Brightness";
			case 1: return "High Brightness";
			case 2: return "Highest Brightness";
		}

		return "??? Brightness";
	}

	private void showLanguagePicker(final boolean inGame) {
		final Languages[] languages = Languages.values();
		String[] options = new String[languages.length];
		for (int i = 0; i < languages.length; i++) {
			options[i] = languages[i].nativeName();
		}

		parent.add(new WndOptions(
			Messages.get(WndSettings.class, "language_title"),
			null,
			landscapeLayout() ? Math.max(1, Math.min(4, (int)(Game.width / PixelScene.defaultZoom - 24) / 90)) : 1,
			options
		) {
			@Override
			protected boolean shouldTranslateOptionLabels() {
				return false;
			}

			@Override
			protected void onSelect(int index) {
				applyLanguage(languages[index], inGame);
			}
		});
	}

	private void applyLanguage(Languages selectedLanguage, boolean inGame) {
		PixelDungeon.language(selectedLanguage);
		hide();
		if (inGame) {
			GameScene.show(new WndSettings(true));
		} else {
			PixelDungeon.switchNoFade(TitleScene.class);
		}
	}

	private String languageText() {
		return Messages.get( WndSettings.class, "language", PixelDungeon.language().nativeName() );
	}
}
