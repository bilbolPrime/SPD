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
package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;

public enum Icons {

	SKULL,
	BUSY,
	COMPASS, 
	PREFS,
	WARNING,
	TARGET,
	WATA,
	WARRIOR,
	MAGE,
	ROGUE,
	HUNTRESS,
	CLOSE,
	DEPTH,
	SLEEP,
	ALERT,
	SUPPORT,
	SUPPORTED,
	BACKPACK,
	SEED_POUCH,
	SCROLL_HOLDER,
	WAND_HOLSTER,
	KEYRING,
	CHECKED,
	UNCHECKED,
	EXIT,
	CHALLENGE_OFF,
	CHALLENGE_ON,
    RESUME,
    SKILL_NOT_AVAILABLE,
    SKILL_AVAILABLE,
    SKILLS,
    ARCHER_MAIDEN,
    BRUTE,
    THIEF,
    WIZARD,
    ARCHER,
    ARCHER_MAIDEN_SKILL,
    ARCHER_MAIDEN_SKILL_B,
    ARCHER_MAIDEN_BOW,
    ARCHER_MAIDEN_POTION,
    ALL_MERCS,
    RAT_KING,
    MOB,
    CHAMP_HALO,
    GAME,
    VIDEO;
	
	public Image get() {
		return get( this );
	}
	
	public static Image get( Icons type ) {
		Image icon = new Image( Assets.ICONS );
		switch (type) {
		case SKULL:
			icon.frame( icon.texture.uvRect( 0, 0, 8, 8 ) );
			break;
		case BUSY:
			icon.frame( icon.texture.uvRect( 8, 0, 16, 8 ) );
			break;
		case COMPASS:
			icon.frame( icon.texture.uvRect( 0, 8, 7, 13 ) );
			break;
		case PREFS:
			icon.frame( icon.texture.uvRect( 30, 0, 46, 16 ) );
			break;
		case WARNING:
			icon.frame( icon.texture.uvRect( 46, 0, 58, 12 ) );
			break;
		case TARGET:
			icon.frame( icon.texture.uvRect( 0, 13, 16, 29 ) );
			break;
		case WATA:
			icon.frame( icon.texture.uvRect( 30, 16, 45, 26 ) );
			break;
		case WARRIOR:
			icon.frame( icon.texture.uvRect( 0, 29, 16, 45 ) );
			break;
		case MAGE:
			icon.frame( icon.texture.uvRect( 16, 29, 32, 45 ) );
			break;
		case ROGUE:
			icon.frame( icon.texture.uvRect( 32, 29, 48, 45 ) );
			break;
		case HUNTRESS:
			icon.frame( icon.texture.uvRect( 48, 29, 64, 45 ) );
			break;
		case CLOSE:
			icon.frame( icon.texture.uvRect( 0, 45, 13, 58 ) );
			break;
		case DEPTH:
			icon.frame( icon.texture.uvRect( 45, 12, 54, 20 ) );
			break;
		case SLEEP:
			icon.frame( icon.texture.uvRect( 13, 45, 22, 53 ) );
			break;
		case ALERT:
			icon.frame( icon.texture.uvRect( 22, 45, 30, 53 ) );
			break;
		case SUPPORT:
			icon.frame( icon.texture.uvRect( 30, 45, 46, 61 ) );
			break;
		case SUPPORTED:
			icon.frame( icon.texture.uvRect( 46, 45, 62, 61 ) );
			break;
		case BACKPACK:
			icon.frame( icon.texture.uvRect( 58, 0, 68, 10 ) );
			break;
		case SCROLL_HOLDER:
			icon.frame( icon.texture.uvRect( 68, 0, 78, 10 ) );
			break;
		case SEED_POUCH:
			icon.frame( icon.texture.uvRect( 78, 0, 88, 10 ) );
			break;
		case WAND_HOLSTER:
			icon.frame( icon.texture.uvRect( 88, 0, 98, 10 ) );
			break;
		case KEYRING:
			icon.frame( icon.texture.uvRect( 64, 29, 74, 39 ) );
			break;
		case CHECKED:
			icon.frame( icon.texture.uvRect( 54, 12, 66, 24 ) );
			break;
		case UNCHECKED:
			icon.frame( icon.texture.uvRect( 66, 12, 78, 24 ) );
			break;
		case EXIT:
			icon.frame( icon.texture.uvRect( 98, 0, 114, 16 ) );
			break;
		case CHALLENGE_OFF:
			icon.frame( icon.texture.uvRect( 78, 16, 102, 40 ) );
			break;
		case CHALLENGE_ON:
			icon.frame( icon.texture.uvRect( 102, 16, 126, 40 ) );
			break;
		case RESUME:
			icon.frame( icon.texture.uvRect( 114, 0, 126, 11 ) );
			break;
        case SKILL_AVAILABLE:
            icon.frame( icon.texture.uvRect( 63, 48, 77, 57 ) );
            break;
        case SKILL_NOT_AVAILABLE:
            icon.frame( icon.texture.uvRect( 77, 48, 91, 57 ) );
            break;
        case SKILLS:
            icon.frame( icon.texture.uvRect( 87, 48, 101, 60 ) );
            break;
        case ARCHER_MAIDEN_SKILL:
            icon.frame( icon.texture.uvRect( 0, 62, 18, 78 ) );
            break;
        case ARCHER_MAIDEN:
        icon.frame( icon.texture.uvRect( 22, 62, 34, 78 ) );
        break;
        case ARCHER_MAIDEN_SKILL_B:
        icon.frame( icon.texture.uvRect( 38, 62, 47, 78 ) );
        break;
        case ARCHER_MAIDEN_BOW:
        icon.frame( icon.texture.uvRect( 51, 62, 67, 78 ) );
        break;
        case ARCHER_MAIDEN_POTION:
        icon.frame( icon.texture.uvRect( 72, 62, 82, 78 ) );
        break;
        case BRUTE:
        icon.frame( icon.texture.uvRect( 2, 83, 13, 97 ) );
        break;
        case ARCHER:
        icon.frame( icon.texture.uvRect( 16, 83, 27, 97 ) );
        break;
        case WIZARD:
        icon.frame( icon.texture.uvRect( 30, 83, 41, 97 ) );
        break;
       case THIEF:
        icon.frame( icon.texture.uvRect( 43, 83, 54, 97 ) );
       break;
       case ALL_MERCS:
        icon.frame( icon.texture.uvRect( 54, 83, 71, 97 ) );
        break;
       case RAT_KING:
        icon.frame( icon.texture.uvRect( 1, 102, 12, 117 ) );
          break;
       case CHAMP_HALO:
          icon.frame( icon.texture.uvRect( 17, 104, 26, 115 ) );
          break;
       case MOB:
          icon.frame( icon.texture.uvRect( 28, 101, 39, 116 ) );
           break;
        case GAME:
            icon.frame( icon.texture.uvRect( 88, 65, 103, 77 ) );
            break;
        case VIDEO:
            icon.frame( icon.texture.uvRect( 73, 83, 93, 96 ) );
            break;

		}
		return icon;
	}
	
	public static Image get( HeroClass cl ) {
		switch (cl) {
		case WARRIOR:
			return get( WARRIOR );
		case MAGE:
			return get( MAGE );
		case ROGUE:
			return get( ROGUE );
		case HUNTRESS:
			return get( HUNTRESS );
		default:
			return null;
		}
	}
}
