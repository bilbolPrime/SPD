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
package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.potions.*;
import com.bilboldev.pixeldungeonskills.items.rings.*;
import com.bilboldev.pixeldungeonskills.items.scrolls.*;
import com.bilboldev.pixeldungeonskills.items.wands.*;

import java.util.HashMap;
import java.util.Map;

public class ItemSpriteSheet {

	public static final int WEAPON	= 5;
	public static final int ARMOR	= 6;
	public static final int RING	= 7;
	public static final int SMTH	= 127;

	public static final int SKELETON_KEY	= 8;
	public static final int IRON_KEY		= 9;
	public static final int GOLDEN_KEY		= 10;

	public static final int SHORT_SWORD		= 2;
	public static final int KNUCKLEDUSTER	= 16;
	public static final int QUARTERSTAFF	= 17;
	public static final int MACE			= 18;
	public static final int DAGGER			= 19;
	public static final int SWORD			= 20;
	public static final int LONG_SWORD		= 21;
	public static final int BATTLE_AXE		= 22;
	public static final int WAR_HAMMER		= 23;
	public static final int SPEAR			= 29;
	public static final int GLAIVE			= 30;

	public static final int SHURIKEN		= 15;
	public static final int DART			= 31;
	public static final int BOOMERANG		= 106;
	public static final int TOMAHAWK		= 107;
	public static final int INCENDIARY_DART	= 108;
	public static final int CURARE_DART		= 109;
	public static final int JAVELIN			= 110;

	public static final int ARMOR_CLOTH		= 24;
	public static final int ARMOR_LEATHER	= 25;
	public static final int ARMOR_MAIL		= 26;
	public static final int ARMOR_SCALE		= 27;
	public static final int ARMOR_PLATE		= 28;
	public static final int ARMOR_ROGUE		= 96;
	public static final int ARMOR_WARRIOR	= 97;
	public static final int ARMOR_MAGE		= 98;
	public static final int ARMOR_HUNTRESS	= 99;

	public static final int WAND_MAGIC_MISSILE	= 3;
	public static final int WAND_HOLLY			= 48;
	public static final int WAND_YEW			= 49;
	public static final int WAND_EBONY			= 50;
	public static final int WAND_CHERRY			= 51;
	public static final int WAND_TEAK			= 52;
	public static final int WAND_ROWAN			= 53;
	public static final int WAND_WILLOW			= 54;
	public static final int WAND_MAHOGANY		= 55;
	public static final int WAND_BAMBOO			= 68;
	public static final int WAND_PURPLEHEART	= 69;
	public static final int WAND_OAK			= 70;
	public static final int WAND_BIRCH			= 71;

	public static final int RING_DIAMOND	= 32;
	public static final int RING_OPAL		= 33;
	public static final int RING_GARNET		= 34;
	public static final int RING_RUBY		= 35;
	public static final int RING_AMETHYST	= 36;
	public static final int RING_TOPAZ		= 37;
	public static final int RING_ONYX		= 38;
	public static final int RING_TOURMALINE	= 39;
	public static final int RING_EMERALD	= 72;
	public static final int RING_SAPPHIRE	= 73;
	public static final int RING_QUARTZ		= 74;
	public static final int RING_AGATE		= 75;

	public static final int POTION_TURQUOISE	= 56;
	public static final int POTION_CRIMSON		= 57;
	public static final int POTION_AZURE		= 58;
	public static final int POTION_JADE			= 59;
	public static final int POTION_GOLDEN		= 60;
	public static final int POTION_MAGENTA		= 61;
	public static final int POTION_CHARCOAL		= 62;
	public static final int POTION_IVORY		= 63;
	public static final int POTION_AMBER		= 64;
	public static final int POTION_BISTRE		= 65;
	public static final int POTION_INDIGO		= 66;
	public static final int POTION_SILVER		= 67;

	public static final int SCROLL_KAUNAN	= 40;
	public static final int SCROLL_SOWILO	= 41;
	public static final int SCROLL_LAGUZ	= 42;
	public static final int SCROLL_YNGVI	= 43;
	public static final int SCROLL_GYFU		= 44;
	public static final int SCROLL_RAIDO	= 45;
	public static final int SCROLL_ISAZ		= 46;
	public static final int SCROLL_MANNAZ	= 47;
	public static final int SCROLL_NAUDIZ	= 76;
	public static final int SCROLL_BERKANAN	= 77;
	public static final int SCROLL_ODAL		= 78;
	public static final int SCROLL_TIWAZ	= 79;
	public static final int SCROLL_WIPE_OUT	= 117;

	public static final int SEED_FIREBLOOM	= 88;
	public static final int SEED_ICECAP		= 89;
	public static final int SEED_SORROWMOSS	= 90;
	public static final int SEED_DREAMWEED	= 91;
	public static final int SEED_SUNGRASS	= 92;
	public static final int SEED_EARTHROOT	= 93;
	public static final int SEED_FADELEAF	= 94;
	public static final int SEED_ROTBERRY	= 95;

	public static final int ROSE	= 100;
	public static final int PICKAXE	= 101;
	public static final int ORE		= 102;
	public static final int SKULL	= 103;
	public static final int PHANTOM	= 118;
	public static final int DUST	= 121;
	public static final int TOKEN	= 122;

	public static final int BONES			= 0;
	public static final int CHEST			= 11;
	public static final int LOCKED_CHEST	= 12;
	public static final int TOMB			= 13;
	public static final int CRYSTAL_CHEST	= 105;
	public static final int HIDDEN			= 119;

	public static final int RATION		= 4;
	public static final int PASTY		= 112;
	public static final int MEAT		= 113;
	public static final int STEAK		= 114;
	public static final int OVERPRICED	= 115;
	public static final int CARPACCIO	= 116;

	public static final int POUCH	= 83;
	public static final int HOLDER	= 104;
	public static final int HOLSTER	= 111;
	public static final int KEYRING	= 126;

	public static final int SPARK_UPGRADE	= 117;
	public static final int SPARK_ENCHANT	= 118;

	public static final int ANKH	= 1;
	public static final int GOLD	= 14;
	public static final int STYLUS	= 80;
	public static final int DEWDROP	= 81;
	public static final int MASTERY	= 82;
	public static final int TORCH	= 84;
	public static final int BEACON	= 85;
	public static final int KIT		= 86;
	public static final int AMULET	= 87;
	public static final int VIAL	= 120;
	public static final int WEIGHT	= 123;
	public static final int BOMB	= 124;
	public static final int HONEYPOT= 125;

    public static final int SPD_OFFSET = 136;
	public static final int GUN_OFFSET = SPD_OFFSET + 8 * 8;
	public static final int POTION_HOLDER	= SPD_OFFSET + 34;
    public static final int EMPTY_BOW	= SPD_OFFSET + 0;
    public static final int Bow	= SPD_OFFSET + 1;
    public static final int ForstBow	= SPD_OFFSET + 2;
    public static final int FlameBow	= SPD_OFFSET + 3;
    public static final int Arrow	= SPD_OFFSET + 5;
    public static final int BombArrow	= SPD_OFFSET + 6;
    public static final int CupidArrow = SPD_OFFSET + 7;

    public static final int SCROLL_SKILLPOINT	= SPD_OFFSET + 8;
    public static final int SCROLL_SKILLRESETACTIVE		= SPD_OFFSET + 9;
    public static final int SCROLL_FOCUS = SCROLL_SKILLRESETACTIVE;
    public static final int SCROLL_GOHOME	= SPD_OFFSET + 10;
    public static final int SCROLL_SACRIFICE	= SPD_OFFSET + 11;
    public static final int SCROLL_BLOODY	= SPD_OFFSET + 12;
    public static final int SCROLL_FROST	= SPD_OFFSET + 13;

    public static final int NecroBlade0	= SPD_OFFSET + 16;
    public static final int NecroBlade1	= SPD_OFFSET + 17;
    public static final int NecroBlade2	= SPD_OFFSET + 18;
    public static final int NecroBlade3	= SPD_OFFSET + 19;
    public static final int NecroBlade4	= SPD_OFFSET + 20;
    public static final int NecroBlade5 = SPD_OFFSET + 21;

    public static final int Knowledge	= SPD_OFFSET + 24;
    public static final int DualSwords	= SPD_OFFSET + 26;
    public static final int RemoteBomb	= SPD_OFFSET + 27;

    public static final int POTION_MANA		= SPD_OFFSET + 28;

    public static final int GemStone	= SPD_OFFSET + 29;
    public static final int GemStoneFull = SPD_OFFSET + 30;
    public static final int GemStonePart = SPD_OFFSET + 31;

    public static final int POTION_EMPTY= SPD_OFFSET + 32;
    public static final int POTION_PLACEHOLDER = SPD_OFFSET + 33;

    public static final int CRYSTAL_EMPTY = SPD_OFFSET + 40;
    public static final int CRYSTAL_FULL = SPD_OFFSET + 41;

    public static final int MOB_PROJECTILE = SPD_OFFSET + 42;
	public static final int BULLET = GUN_OFFSET + 8;
	public static final int ARMOR_MERCENARY = BULLET + 8;
	public static final int ARMOR_NECROMANCER = BULLET + 10;
	public static final int HANDGUN = GUN_OFFSET + 0;
	public static final int PISTOL = GUN_OFFSET + 1;
	public static final int BLUNDERBUSS = GUN_OFFSET + 2;
	public static final int RIFLE = GUN_OFFSET + 3;
	public static final int MORTAR = GUN_OFFSET + 4;

	public static final int SoulBlade_0	= SPD_OFFSET + 48;
	public static final int SoulBlade_1	= SPD_OFFSET + 49;

	public static final int JOE_SPEAR	= SPD_OFFSET + 56;

	public static final int CORPSE	= ARMOR_MERCENARY + 8;
	public static final int GLACIER_CLEAVER = 232;
	public static final int HOARFROST_STAFF = 233;
	public static final int RIME_PICK = 234;
	public static final int FROST_BOW = 235;
	public static final int GRAVEFROST_SCYTHE = 236;
	public static final int FROSTBITE_RIFLE = 237;
	public static final int SCROLL_IDENTIFY = 240;
	public static final int SCROLL_MAGIC_MAPPING = 241;
	public static final int SCROLL_RECHARGING = 242;
	public static final int SCROLL_REMOVE_CURSE = 243;
	public static final int SCROLL_TELEPORTATION = 244;
	public static final int SCROLL_CHALLENGE = 245;
	public static final int SCROLL_TERROR = 246;
	public static final int SCROLL_LULLABY = 247;
	public static final int SCROLL_PSIONIC_BLAST = 248;
	public static final int SCROLL_MIRROR_IMAGE = 249;
	public static final int SCROLL_UPGRADE = 250;
	public static final int SCROLL_ENCHANTMENT = 251;
	public static final int RING_MENDING = 252;
	public static final int RING_DETECTION = 253;
	public static final int RING_SHADOWS = 254;
	public static final int RING_POWER = 255;
	public static final int RING_HERBALISM = 256;
	public static final int RING_ACCURACY = 257;
	public static final int RING_EVASION = 258;
	public static final int RING_SATIETY = 259;
	public static final int RING_HASTE = 260;
	public static final int RING_HAGGLER = 261;
	public static final int RING_ELEMENTS = 262;
	public static final int RING_THORNS = 263;
	public static final int WAND_TELEPORTATION = 264;
	public static final int WAND_SLOWNESS = 265;
	public static final int WAND_FIREBOLT = 266;
	public static final int WAND_POISON = 267;
	public static final int WAND_REGROWTH = 268;
	public static final int WAND_BLINK = 269;
	public static final int WAND_LIGHTNING = 270;
	public static final int WAND_AMOK = 271;
	public static final int WAND_REACH = 272;
	public static final int WAND_FLOCK = 273;
	public static final int WAND_DISINTEGRATION = 274;
	public static final int WAND_AVALANCHE = 275;
	public static final int POTION_HEALING = 276;
	public static final int POTION_EXPERIENCE = 277;
	public static final int POTION_TOXIC_GAS = 278;
	public static final int POTION_LIQUID_FLAME = 279;
	public static final int POTION_STRENGTH = 280;
	public static final int POTION_PARALYTIC_GAS = 281;
	public static final int POTION_LEVITATION = 282;
	public static final int POTION_MIND_VISION = 283;
	public static final int POTION_PURITY = 284;
	public static final int POTION_INVISIBILITY = 285;
	public static final int POTION_MIGHT = 286;
	public static final int POTION_FROST = 287;
	public static final int WAND_MAGIC_MISSILE_IDENTIFIED = 288;
	public static final int POTION_MANA_IDENTIFIED = 289;
	public static final int RECIPE_HEALING = 290;
	public static final int POTION_REJUVENATION = 297;
	public static final int MAX_IMAGE = POTION_REJUVENATION;

	private static final Map<Class<? extends Item>, Integer> identifiedImages = new HashMap<>();

	static {
		identifiedImages.put(ScrollOfIdentify.class, SCROLL_IDENTIFY);
		identifiedImages.put(ScrollOfMagicMapping.class, SCROLL_MAGIC_MAPPING);
		identifiedImages.put(ScrollOfRecharging.class, SCROLL_RECHARGING);
		identifiedImages.put(ScrollOfRemoveCurse.class, SCROLL_REMOVE_CURSE);
		identifiedImages.put(ScrollOfTeleportation.class, SCROLL_TELEPORTATION);
		identifiedImages.put(ScrollOfChallenge.class, SCROLL_CHALLENGE);
		identifiedImages.put(ScrollOfTerror.class, SCROLL_TERROR);
		identifiedImages.put(ScrollOfLullaby.class, SCROLL_LULLABY);
		identifiedImages.put(ScrollOfPsionicBlast.class, SCROLL_PSIONIC_BLAST);
		identifiedImages.put(ScrollOfMirrorImage.class, SCROLL_MIRROR_IMAGE);
		identifiedImages.put(ScrollOfUpgrade.class, SCROLL_UPGRADE);
		identifiedImages.put(ScrollOfEnchantment.class, SCROLL_ENCHANTMENT);
		identifiedImages.put(RingOfMending.class, RING_MENDING);
		identifiedImages.put(RingOfDetection.class, RING_DETECTION);
		identifiedImages.put(RingOfShadows.class, RING_SHADOWS);
		identifiedImages.put(RingOfPower.class, RING_POWER);
		identifiedImages.put(RingOfHerbalism.class, RING_HERBALISM);
		identifiedImages.put(RingOfAccuracy.class, RING_ACCURACY);
		identifiedImages.put(RingOfEvasion.class, RING_EVASION);
		identifiedImages.put(RingOfSatiety.class, RING_SATIETY);
		identifiedImages.put(RingOfHaste.class, RING_HASTE);
		identifiedImages.put(RingOfHaggler.class, RING_HAGGLER);
		identifiedImages.put(RingOfElements.class, RING_ELEMENTS);
		identifiedImages.put(RingOfThorns.class, RING_THORNS);
		identifiedImages.put(WandOfTeleportation.class, WAND_TELEPORTATION);
		identifiedImages.put(WandOfSlowness.class, WAND_SLOWNESS);
		identifiedImages.put(WandOfFirebolt.class, WAND_FIREBOLT);
		identifiedImages.put(WandOfPoison.class, WAND_POISON);
		identifiedImages.put(WandOfRegrowth.class, WAND_REGROWTH);
		identifiedImages.put(WandOfBlink.class, WAND_BLINK);
		identifiedImages.put(WandOfLightning.class, WAND_LIGHTNING);
		identifiedImages.put(WandOfAmok.class, WAND_AMOK);
		identifiedImages.put(WandOfReach.class, WAND_REACH);
		identifiedImages.put(WandOfFlock.class, WAND_FLOCK);
		identifiedImages.put(WandOfDisintegration.class, WAND_DISINTEGRATION);
		identifiedImages.put(WandOfAvalanche.class, WAND_AVALANCHE);
		identifiedImages.put(PotionOfHealing.class, POTION_HEALING);
		identifiedImages.put(PotionOfExperience.class, POTION_EXPERIENCE);
		identifiedImages.put(PotionOfToxicGas.class, POTION_TOXIC_GAS);
		identifiedImages.put(PotionOfLiquidFlame.class, POTION_LIQUID_FLAME);
		identifiedImages.put(PotionOfStrength.class, POTION_STRENGTH);
		identifiedImages.put(PotionOfParalyticGas.class, POTION_PARALYTIC_GAS);
		identifiedImages.put(PotionOfLevitation.class, POTION_LEVITATION);
		identifiedImages.put(PotionOfMindVision.class, POTION_MIND_VISION);
		identifiedImages.put(PotionOfPurity.class, POTION_PURITY);
		identifiedImages.put(PotionOfInvisibility.class, POTION_INVISIBILITY);
		identifiedImages.put(PotionOfMight.class, POTION_MIGHT);
		identifiedImages.put(PotionOfFrost.class, POTION_FROST);
		identifiedImages.put(WandOfMagicMissile.class, WAND_MAGIC_MISSILE_IDENTIFIED);
		identifiedImages.put(PotionOfMana.class, POTION_MANA_IDENTIFIED);
		identifiedImages.put(ScrollOfSkill.class, SCROLL_SKILLPOINT);
		identifiedImages.put(ScrollOfReadiness.class, SCROLL_FOCUS);
		identifiedImages.put(ScrollOfHome.class, SCROLL_GOHOME);
		identifiedImages.put(ScrollOfSacrifice.class, SCROLL_SACRIFICE);
		identifiedImages.put(ScrollOfBloodyRitual.class, SCROLL_BLOODY);
		identifiedImages.put(ScrollOfFrostLevel.class, SCROLL_FROST);
		identifiedImages.put(ScrollOfWipeOut.class, SCROLL_WIPE_OUT);
	}

	public static int identifiedImage(Class<? extends Item> type, int fallback) {
		Integer image = identifiedImages.get(type);
		return image == null ? fallback : image;
	}
}
