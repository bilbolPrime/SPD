package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.particles.PixelParticle;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.blobs.ConfusionGas;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.blobs.ParalyticGas;
import com.bilboldev.pixeldungeonskills.actors.blobs.SacrificialFire;
import com.bilboldev.pixeldungeonskills.actors.blobs.ToxicGas;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.DeathRay;
import com.bilboldev.pixeldungeonskills.effects.ChampRedHalo;
import com.bilboldev.pixeldungeonskills.effects.ChampYellowHalo;
import com.bilboldev.pixeldungeonskills.effects.ChampBlackHalo;
import com.bilboldev.pixeldungeonskills.effects.ChampWhiteHalo;
import com.bilboldev.pixeldungeonskills.effects.RebornAura;
import com.bilboldev.pixeldungeonskills.effects.Halo;
import com.bilboldev.pixeldungeonskills.effects.EmoIcon;
import com.bilboldev.pixeldungeonskills.effects.Lightning;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.BlobEmitter;
import com.bilboldev.pixeldungeonskills.effects.Wound;
import com.bilboldev.pixeldungeonskills.effects.Stab;
import com.bilboldev.pixeldungeonskills.effects.SpellSprite;
import com.bilboldev.pixeldungeonskills.effects.Flare;
import com.bilboldev.pixeldungeonskills.effects.ArcherMaidenHalo;
import com.bilboldev.pixeldungeonskills.effects.SoulFuryHalo;
import com.bilboldev.pixeldungeonskills.effects.WhiteHalo;
import com.bilboldev.pixeldungeonskills.effects.particles.FlameParticle;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.SewerLevel;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.sprites.ShopkeeperSprite;

final class FirstPersonEffects {
    private final GameScene scene;
    private final FirstPersonView view;
    private final Image[] flame = new Image[2];
    private final Image cloud;
    private final FirstPersonFlares flares = new FirstPersonFlares();
    private double time;
    private int immersionColor;
    private double immersionAlpha;
    private int particles;

    FirstPersonEffects(GameScene scene, FirstPersonView view) {
        this.scene = scene;
        this.view = view;
        for (int i = 0; i < flame.length; i++) {
            flame[i] = new Image(Assets.FIREBALL);
            int size = flame[i].texture.height;
            flame[i].frame((i + 2) * size, 0, size, size);
        }
        cloud = new Image(Assets.SPECKS);
        int columns = cloud.texture.width / 7;
        cloud.frame((Speck.STEAM % columns) * 7, (Speck.STEAM / columns) * 7, 7, 7);
    }

    void collect() {

        time = -scene.water.offsetY() / 5.0;
        immersionAlpha = 0;
        particles = 0;
        hazards();
        collectGroup(scene.mobs, 0);
        collectGroup(scene.effects, 0);
        collectGroup(scene.emitters, 0);
        collectGroup(scene.spells, 0);
        indicators();
        wallDecorations();
        for (Mob mob : Dungeon.level.mobs) {
            if (mob.isAlive() && mob.buff(Burning.class) != null && mob.sprite != null && mob.sprite.visible) {
                fireAt(mob.pos, 0.8, mob.sprite.movementOffsetX(), mob.sprite.movementOffsetY());
            }
        }
        if (Dungeon.hero.buff(Burning.class) != null) {
            if (view.isThirdPerson()) fireAt(Dungeon.hero.pos, 0.8,
                    Dungeon.hero.sprite.movementOffsetX(), Dungeon.hero.sprite.movementOffsetY());
            else immerse(0xEE7722, 0.12);
        }
    }

    private void indicators() {
        if (!scene.emoicons.exists || !scene.emoicons.visible) return;
        for (int i = 0; i < scene.emoicons.length; i++) {
            Gizmo g = scene.emoicons.memberAt(i);
            if (!(g instanceof EmoIcon) || !g.exists || !g.visible) continue;
            EmoIcon icon = (EmoIcon)g;
            if (icon.heightOffset > 0) continue;
            CharSprite owner = icon.owner();
            if (owner == null || owner.ch == null || !owner.exists || !owner.visible) continue;
            if (owner == Dungeon.hero.sprite && !view.isThirdPerson()) continue;
            int cell = owner.ch.pos, width = Dungeon.level.width();

            double height = Math.max(0.5, owner.height / 16.0);
            double size = Math.abs(icon.height()) / 16.0 * 0.75;
            if (icon instanceof EmoIcon.PartyTurn) {
                height += ((EmoIcon.PartyTurn)icon).bobOffset() / 16.0;
            }
            view.effectSprite(icon, cell % width + 0.5 + owner.movementOffsetX(),
                    cell / width + 0.5 + owner.movementOffsetY(),
                    height + 0.04 + size * 0.5, size, 0, 1, -1, true);
        }
    }

    private void wallDecorations() {

        int width = Dungeon.level.width();
        for (int i = 0; i < scene.length; i++) {
            Gizmo g = scene.memberAt(i);
            if (!(g instanceof Emitter) || !g.exists || !g.visible) continue;
            Emitter emitter = (Emitter)g;
            int x = (int)(emitter.x / 16), y = (int)(emitter.y / 16);
            if (x < 0 || y < 0 || x >= width || y >= Dungeon.level.height()) continue;
            int cell = x + y * width;
            if (Dungeon.level.map[cell] != Terrain.WALL_DECO) continue;
            for (int j = 0; j < emitter.length; j++) {
                Gizmo p = emitter.memberAt(j);
                if (!(p instanceof FlameParticle || p instanceof SewerLevel.WaterParticle) || !p.exists || !p.visible) continue;
                Image particle = (Image)p;

                double u = (particle.x + particle.origin.x - x * 16) / 16.0;
                double z = 1 - (particle.y + particle.origin.y - y * 16 - 4) / 12.0;
                view.wallParticle(particle, cell, u, z, Math.abs(particle.height()) / 12.0,
                        p instanceof FlameParticle);
            }
        }
    }

    private void collectGroup(Group group, int nesting) {
        if (group == null || !group.exists || !group.visible || nesting > 8) return;
        for (int i = 0; i < group.length; i++) {
            Gizmo g = group.memberAt(i);
            if (g == null || !g.exists || !g.visible) continue;
            if (g instanceof ShopkeeperSprite) {
                ShopkeeperSprite keeper = (ShopkeeperSprite)g;
                view.attachedParticle(keeper.coin(), keeper);
            } else if (g instanceof MissileSprite) {
                thrown((MissileSprite)g);
            } else if (g instanceof MagicMissile) {
                magic((MagicMissile)g);
            } else if (g instanceof DeathRay) {
                beam((Image)g, Difficulties.is3d ? 0.4 : 0, 0xFF6688);
            } else if (g instanceof Lightning) {
                Group lightning = (Group)g;
                for (int j = 0; j < lightning.length; j++) {
                    Gizmo arc = lightning.memberAt(j);
                    if (arc instanceof Image && arc.exists && arc.visible) beam((Image)arc, 0, 0xBBDFFF);
                }
            } else if (g instanceof RebornAura) {
                RebornAura aura = (RebornAura)g;
                for (int part = 0; part < aura.length; part++) {
                    view.attachedParticle((Image)aura.memberAt(part), aura.owner(), true, true);
                }
            } else if (g instanceof ChampRedHalo) {
                championHalo((Halo)g, ((ChampRedHalo)g).owner());
            } else if (g instanceof ChampYellowHalo) {
                championHalo((Halo)g, ((ChampYellowHalo)g).owner());
            } else if (g instanceof ChampBlackHalo) {
                championHalo((Halo)g, ((ChampBlackHalo)g).owner());
            } else if (g instanceof ChampWhiteHalo) {
                championHalo((Halo)g, ((ChampWhiteHalo)g).owner());
            } else if (g instanceof ArcherMaidenHalo) {
                championHalo((Halo)g, ((ArcherMaidenHalo)g).owner());
            } else if (g instanceof SoulFuryHalo) {
                championHalo((Halo)g, ((SoulFuryHalo)g).owner());
            } else if (g instanceof WhiteHalo) {
                championHalo((Halo)g, ((WhiteHalo)g).owner());
            } else if (g instanceof Flare) {
                flare((Flare)g);
            } else if (g instanceof Wound) {
                Image wound = (Image)g;
                double x = (wound.x + wound.origin.x) / 16, y = (wound.y + wound.origin.y) / 16;
                if (!view.isThirdPerson() && (int)x + (int)y * Dungeon.level.width() == Dungeon.hero.pos) {
                    view.selfEffect(wound);
                } else {
                    view.effectSprite(wound, x, y, 0.5, Math.abs(wound.height()) / 16,
                            Math.toRadians(wound.angle), 1, -1, true);
                }
            } else if (g instanceof Stab) {

                if (!view.isThirdPerson()) continue;
                Stab stab = (Stab)g;
                double x = stab.worldX(), y = stab.worldY(), z = 0.38;

                double rotation = view.flightAngle(x, y, stab.directionX(), stab.directionY(), z) + Math.PI / 4;
                view.effectSprite(stab, x, y, z, Math.abs(stab.height()) / 16, rotation, 1, -1, true);
            } else if (g instanceof SpellSprite) {
                SpellSprite spell = (SpellSprite)g;
                if (spell.target() != null && spell.target().sprite != null) {
                    if (!view.isThirdPerson() && spell.target() == Dungeon.hero) view.selfEffect(spell);
                    else view.attachedParticle(spell, spell.target().sprite, true, false);
                } else {
                    int cell = spell.cell();
                    view.effectSprite(spell, cell % Dungeon.level.width() + .5, cell / Dungeon.level.width() + .5,
                            1.2, Math.abs(spell.height()) / 16, 0, 1, -1, true);
                }
            } else if (g instanceof Emitter) {
                particles((Emitter)g);
            } else if (g instanceof Group) {
                collectGroup((Group)g, nesting + 1);
            }
        }
    }

    private void particles(Emitter emitter) {

        if (emitter instanceof BlobEmitter) {
            Blob blob = ((BlobEmitter)emitter).blob();
            if (blob instanceof Fire || blob instanceof SacrificialFire || blob instanceof ToxicGas
                    || blob instanceof ParalyticGas || blob instanceof ConfusionGas) return;
        }
        CharSprite owner = emitter.renderAnchor() instanceof CharSprite ? (CharSprite)emitter.renderAnchor() : null;
        if (owner != null && (!owner.exists || !owner.visible || owner.ch == null)) return;
        boolean additive = emitter.lightMode();
        for (int i = 0; i < emitter.length && particles < 512; i++) {
            Gizmo g = emitter.memberAt(i);
            if (!(g instanceof Speck || g instanceof PixelParticle) || !g.exists || !g.visible) continue;
            if (owner != null && g instanceof FlameParticle) continue;
            Image particle = (Image)g;
            if (particle.am <= 0 || particle.height() == 0) continue;
            particles++;
            boolean selfScream = !view.isThirdPerson() && particle instanceof Speck
                    && ((Speck)particle).type() == Speck.SCREAM
                    && (owner == Dungeon.hero.sprite || owner == null
                    && (int)(emitter.x / 16) + (int)(emitter.y / 16) * Dungeon.level.width() == Dungeon.hero.pos);
            if (selfScream) {

                view.selfEffect(particle);
            } else if (owner != null) {
                view.attachedParticle(particle, owner, true, additive);
            } else if (emitter instanceof BlobEmitter) {
                view.effectSprite(particle, (particle.x + particle.origin.x) / 16,
                        (particle.y + particle.origin.y) / 16, 0.25,
                        Math.abs(particle.height()) / 16, Math.toRadians(particle.angle), 1, -1, true, additive);
            } else {

                double x = (emitter.x + emitter.width * 0.5) / 16;
                double y = (emitter.y + emitter.height * 0.5) / 16;
                double offset = (particle.x + particle.origin.x) / 16 - x;
                double z = 0.12 + y - (particle.y + particle.origin.y) / 16;
                if (!view.isThirdPerson() && (int)x + (int)y * Dungeon.level.width() == Dungeon.hero.pos) {

                    view.selfParticle(particle, offset, z, additive);
                } else {
                    view.planeParticle(particle, x, y, offset, z, additive);
                }
            }
        }
    }

    private void flare(Flare flare) {
        CharSprite owner = flare.anchor() instanceof CharSprite ? (CharSprite)flare.anchor() : null;
        if (owner != null && (!owner.exists || !owner.visible || owner.ch == null)) return;
        int color = ((int)(flare.rm * 255) << 16) | ((int)(flare.gm * 255) << 8) | (int)(flare.bm * 255);
        if (owner == Dungeon.hero.sprite && !view.isThirdPerson()) {
            immerse(color, Math.min(0.12, flare.am * 0.12));
            return;
        }
        Image art = flares.image(flare.rays());
        if (art == null) return;
        double x = flare.x / 16, y = flare.y / 16, z = 0.12;
        if (owner != null) {
            x = owner.ch.pos % Dungeon.level.width() + 0.5 + owner.movementOffsetX();
            y = owner.ch.pos / Dungeon.level.width() + 0.5 + owner.movementOffsetY();
            z = owner.height / 32.0;
        }
        view.effectSprite(art, x, y, z, 2 * flare.radius() * Math.abs(flare.scale.y) / 16,
                Math.toRadians(flare.angle), flare.am, color, true, flare.lightMode());
    }

    private void championHalo(Halo halo, CharSprite owner) {
        if (owner == null || owner.ch == null || !owner.exists || !owner.visible || halo.am <= 0) return;
        if (owner == Dungeon.hero.sprite && !view.isThirdPerson()) return;
        int cell = owner.ch.pos, width = Dungeon.level.width();

        view.effectSprite(halo, cell % width + 0.5 + owner.movementOffsetX(),
                cell / width + 0.5 + owner.movementOffsetY(), Math.max(0.5, owner.height / 16.0) * 0.5,
                Math.abs(halo.height()) / 16.0, 0, 1, -1, true, true);
    }

    private void thrown(MissileSprite s) {

        double x = s.x / 16.0 + 0.5, y = s.y / 16.0 + 0.5;
        double z = 0.38;
        double rotation;
        if (s.angularSpeed == 0 && (s.speed.x != 0 || s.speed.y != 0)) {

            rotation = view.flightAngle(x, y, s.speed.x, s.speed.y, z) + Math.PI / 4;
        } else {

            rotation = Math.toRadians(s.angle);
        }
        view.effectSprite(s, x, y, z, 0.45, rotation, 1, -1, false);
    }

    private void magic(MagicMissile missile) {

        double raise = Difficulties.is3d ? 0.4 : 0;
        for (int i = 0; i < missile.length; i++) {
            Gizmo g = missile.memberAt(i);
            if (!(g instanceof Image) || !g.exists || !g.visible) continue;
            Image particle = (Image)g;
            double x = (particle.x + particle.origin.x) / 16.0;
            double y = (particle.y + particle.origin.y) / 16.0 + raise;
            double size = Math.max(Math.abs(particle.width()), Math.abs(particle.height())) / 16.0;
            view.effectSprite(particle, x, y, 0.38, size, Math.toRadians(particle.angle), 1, -1, true);
        }
    }

    private void beam(Image beam, double raise, int color) {
        double x = (beam.x + beam.origin.x) / 16.0;
        double y = (beam.y + beam.origin.y) / 16.0 + raise;
        double angle = Math.toRadians(beam.angle), length = beam.width * beam.scale.x / 16.0;
        if (length <= 0 || beam.am <= 0) return;
        int steps = Math.min(384, Math.max(1, (int)Math.ceil(length / 0.04)));

        for (int i = 0; i <= steps; i++) {
            double distance = length * i / steps;
            view.effectSprite(cloud, x + Math.cos(angle) * distance, y + Math.sin(angle) * distance,
                    0.38, 0.09, 0, beam.am, color, true);
        }
    }

    private void hazards() {
        for (Blob blob : Dungeon.level.blobs.values()) {
            if (blob.volume <= 0 || blob.cur == null) continue;
            boolean fire = blob instanceof Fire || blob instanceof SacrificialFire;
            int color = blob instanceof ToxicGas ? 0x50FF60 : blob instanceof ParalyticGas ? 0xFFFF66
                    : blob instanceof ConfusionGas ? 0xBB77FF : 0;
            if (!fire && color == 0) continue;
            for (int cell = 0; cell < blob.cur.length; cell++) {
                if (blob.cur[cell] <= 0 || cell >= Dungeon.visible.length || !Dungeon.visible[cell]) continue;
                if (fire) fireAt(cell, 1);
                else gasAt(cell, color, blob.cur[cell]);
                if (cell == Dungeon.hero.pos && !view.isThirdPerson()) immerse(fire ? 0xEE7722 : color, fire ? 0.12 : 0.18);
            }
        }
    }

    private void fireAt(int cell, double size) {
        fireAt(cell, size, 0, 0, true);
    }

    private void fireAt(int cell, double size, double offsetX, double offsetY) {
        fireAt(cell, size, offsetX, offsetY, false);
    }

    private void fireAt(int cell, double size, double offsetX, double offsetY, boolean terrain) {
        int width = Dungeon.level.width();
        double x = cell % width + 0.5 + offsetX, y = cell / width + 0.5 + offsetY;
        for (int i = 0; i < 3; i++) {
            double phase = time * 8 + cell * 1.73 + i * 2.1;
            double height = size * (0.48 + 0.12 * Math.sin(phase));
            Image art = flame[(int)Math.floor(time * 9 + cell + i) & 1];
            double flameX = Math.sin(i * 2.1) * 0.22, flameY = Math.cos(i * 2.1) * 0.22;
            if (terrain) view.terrainFlame(art, cell, flameX, flameY, height);
            else view.effectSprite(art, x + flameX, y + flameY, height * 0.5, height, 0, 0.9, -1, true);
        }
    }

    private void gasAt(int cell, int color, int amount) {
        int width = Dungeon.level.width();
        double x = cell % width + 0.5, y = cell / width + 0.5;
        double opacity = Math.min(0.48, 0.24 + amount * 0.008);
        for (int i = 0; i < 3; i++) {
            double phase = time * 0.8 + cell * 1.73 + i * 2.1;
            view.effectSprite(cloud, x + Math.sin(phase) * 0.22, y + Math.cos(phase) * 0.22,
                    0.25 + i * 0.22, 0.95, phase * 0.3, opacity, color, false);
        }
    }

    private void immerse(int color, double alpha) {
        if (alpha > immersionAlpha) {
            immersionColor = color;
            immersionAlpha = alpha;
        }
    }

    void drawImmersion() {
        if (immersionAlpha > 0) view.immersion(immersionColor, immersionAlpha);
    }

    void destroy() {
        flares.destroy();
        for (Image image : flame) image.destroy();
        cloud.destroy();
    }
}
