package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.Window;

public class InspectionTitle extends Component {

	private static final int FRAME_SIZE = 44;

	private final RebornChrome frame;
	private final Image image;
	private final BitmapTextMultiline name;
	private final ColorBlock rule;
	private ColorBlock rim, empty, fill, shine;
	private final float durability;

	public static int windowWidth(int classicWidth) {
		if (!Presentation.reborn()) {
			return classicWidth;
		}
		return Window.landscapeLayout() ? 184 : 144;
	}

	public static Component create(Image image, String name) {
		return create(image, name, Window.titleColor(), Float.NaN);
	}

	static Component create(Image image, String name, int color, float durability) {
		if (Presentation.reborn()) {
			return new InspectionTitle(image, name, color, durability);
		}
		IconTitle title = new IconTitle(image, name);
		title.color(color);
		if (!Float.isNaN(durability)) {
			title.health(durability);
		}
		return title;
	}

	private InspectionTitle(Image image, String name, int color, float durability) {
		frame = new RebornChrome(Chrome.Type.BUTTON).material(RebornChrome.Material.STONE);
		add(frame);
		this.image = image;
		image.origin.set(0);
		float scale = Math.min(2, Math.min((FRAME_SIZE - 8) / image.width,
				(FRAME_SIZE - 8) / image.height));
		image.scale.set(scale >= 1 ? (float)Math.floor(scale) : scale);
		add(image);
		this.name = PixelScene.createMultiline(Messages.maybeTranslate(name), 9);
		this.name.hardlight(color);
		add(this.name);

		this.durability = Math.max(0, Math.min(1, durability));
		if (!Float.isNaN(durability)) {
			add(rim = new ColorBlock(1, 1, 0xFF71647C));
			add(empty = new ColorBlock(1, 1, 0xFF37252C));
			add(fill = new ColorBlock(1, 1, 0xFF527F65));
			add(shine = new ColorBlock(1, 1, 0xFF91B88A));
		}
		add(rule = new ColorBlock(1, 1, 0xFF514458));
	}

	@Override
	protected void layout() {
		frame.x = x;
		frame.y = y;
		frame.size(FRAME_SIZE, FRAME_SIZE);
		image.x = PixelScene.align(x + (FRAME_SIZE - image.width()) / 2);
		image.y = PixelScene.align(y + (FRAME_SIZE - image.height()) / 2);

		name.x = x + FRAME_SIZE + 8;
		name.maxWidth = Math.max(1, (int)(right() - name.x));
		name.measure();
		float textHeight = name.height() + (rim == null ? 0 : 11);
		name.y = y + Math.max(2, (float)Math.floor((FRAME_SIZE - textHeight) / 2));
		if (rim != null) {
			rim.x = name.x;
			rim.y = (float)Math.ceil(name.y + name.height()) + 5;
			rim.size(name.maxWidth, 6);
			empty.x = fill.x = shine.x = rim.x + 1;
			empty.y = fill.y = shine.y = rim.y + 1;
			float barWidth = Math.max(0, name.maxWidth - 2);
			empty.size(barWidth, 4);
			fill.size(barWidth * durability, 4);
			shine.size(barWidth * durability, 1);
		}

		height = Math.max(FRAME_SIZE, name.y - y + textHeight);
		rule.x = x;
		rule.y = y + height + 5;
		rule.size(width, 1);
		height += 10;
	}
}
