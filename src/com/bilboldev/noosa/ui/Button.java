/*
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

package com.bilboldev.noosa.ui;

import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.input.GameAction;
import com.bilboldev.input.GameActions;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.pixeldungeonskills.ui.Window;

public class Button extends Component {

	public static float longClick = 1f;

	protected TouchArea hotArea;

	protected boolean pressed;
	protected float pressTime;

	protected boolean processed;

	public void bindAction(GameAction action) {
		GameActions.bind(this, action, false);
	}

	public void bindAlternateAction(GameAction action) {
		GameActions.bind(this, action, true);
	}

	public boolean acceptsAction() {
		if (!GameActions.enabled || hotArea == null || pressed) return false;
		Window owner = null;
		boolean attached = false;
		for (Gizmo ancestor = this; ancestor != null; ancestor = ancestor.parent) {
			if (!ancestor.exists || !ancestor.active || !ancestor.visible) return false;
			if (ancestor instanceof Window && owner == null) owner = (Window) ancestor;
			if (ancestor == Game.scene()) {
				attached = true;
				break;
			}
		}
		return attached && owner == Window.topWindow();
	}

	public boolean triggerAction(boolean alternate) {
		if (!acceptsAction()) return false;
		if (alternate) return onLongClick();
		onClick();
		return true;
	}

	public boolean triggerPartyAction() {
		for (Gizmo ancestor = this; ancestor != null; ancestor = ancestor.parent) {
			if (!ancestor.exists || !ancestor.active || !ancestor.visible) return false;
			if (ancestor instanceof Window) {
				if (!((Window)ancestor).isPartyWindow()) return false;
				onClick();
				return true;
			}
		}
		return false;
	}

	@Override
	public void destroy() {
		GameActions.unbind(this);
		super.destroy();
	}

	@Override
	protected void createChildren() {
		hotArea = new TouchArea( 0, 0, 0, 0 ) {
			@Override
			protected void onTouchDown(Touch touch) {
				pressed = true;
				pressTime = 0;
				processed = false;
				Button.this.onTouchDown();
				if (touch.longClick) {
					triggerForcedLongClick();
				}
			};
			@Override
			protected void onTouchUp(Touch touch) {
				pressed = false;
				Button.this.onTouchUp();
			};
			@Override
			protected void onClick( Touch touch ) {
				if (!processed) {
					Button.this.onClick();
				}
			};
		};
		add( hotArea );
	}

	@Override
	public void update() {
		super.update();

		hotArea.active = visible;

		if (pressed) {
			if ((pressTime += Game.elapsed) >= longClick) {
				pressed = false;
				if (onLongClick()) {

					hotArea.reset();
					processed = true;
					onTouchUp();

					Game.vibrate( 50 );
				}
			}
		}
	}

	private void triggerForcedLongClick() {
		pressed = false;
		processed = true;
		if (onLongClick()) {
			hotArea.reset();
			Game.vibrate( 50 );
		}
	}

	protected void onTouchDown() {};
	protected void onTouchUp() {};
	protected void onClick() {};

	protected boolean onLongClick() {
		return false;
	};

	@Override
	protected void layout() {
		hotArea.x = x;
		hotArea.y = y;
		hotArea.width = width;
		hotArea.height = height;
	}
}
