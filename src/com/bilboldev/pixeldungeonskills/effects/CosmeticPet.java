package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.pixeldungeonskills.CosmeticPets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import java.util.Arrays;

public final class CosmeticPet extends MovieClip {
    private static final int SEARCH_WIDTH = 11;
    private final HeroSprite owner;
    private final int[] queue = new int[SEARCH_WIDTH * SEARCH_WIDTH];
    private final int[] firstStep = new int[queue.length];
    private Level floor;
    private int floorKey;
    private Animation idle, run;
    private int style = -1;
    private int step = -1;
    private float footX, footY, lift, size;
    private float age, thinkIn, decisionIn, opacity, riding;
    private float goalX, goalY;
    private Mob watching;
    private boolean frightened;

    public CosmeticPet(HeroSprite owner) {
        this.owner = owner;
        visible = false;
    }

    public CosmeticPet image() { return visible && style > 0 ? this : null; }
    public float floorX() { return footX / 16; }
    public float floorY() { return footY / 16; }
    public float elevation() { return lift / 16; }
    public float groundY() { return footY + 8; }

    @Override public void update() {
        if (owner.parent == null || !owner.exists || !(owner.ch instanceof Hero)) {
            killAndErase();
            return;
        }
        Hero hero = (Hero)owner.ch;
        int selected = hero.tutorial ? 0 : PartySession.pet(hero);
        if (style != selected) {
            style = selected;
            floor = null;
            if (style > 0) {
                CharSprite source = CosmeticPets.sprite(style);
                texture(source.texture);
                idle = source.idleAnimation();
                run = style == 3 ? idle : source.runAnimation();
                if (run == null) run = idle;
                play(idle, true);
                source.destroy();
                size = Math.min(1, 9f / Math.max(width, height));
                age = style * 1.9f + hero.heroClass.ordinal();
            }
        }
        visible = style > 0 && hero.isAlive() && owner.visible && hero.invisible <= 0
                && Dungeon.level != null && Dungeon.level.mobs != null;
        if (!visible) return;

        float heroX = owner.x + owner.width() / 2;
        float heroY = owner.groundY() - 8;
        boolean changedFloor = floor == null || floorKey != Dungeon.levelKey();
        if (floor != Dungeon.level) {

            floor = Dungeon.level;
            watching = null;
            thinkIn = 0;
        }
        floorKey = Dungeon.levelKey();
        if (changedFloor || !walkable(cellAt(footX, footY))
                || distanceSquared(footX, footY, heroX, heroY) > 112 * 112) {
            footX = (hero.pos % floor.width() + .5f) * 16;
            footY = (hero.pos / floor.width() + .5f) * 16;
            goalX = footX;
            goalY = footY;
            opacity = riding = thinkIn = decisionIn = 0;
            step = -1;
            if (!walkable(hero.pos)) {
                visible = false;
                return;
            }
        }
        float elapsed = Math.min(.1f, Math.max(0, Game.elapsed));
        age += elapsed;
        thinkIn -= elapsed;
        decisionIn -= elapsed;
        if (thinkIn <= 0) {
            lookAround(hero);
            thinkIn = .3f;
        }
        boolean perched = ride(elapsed, heroX, heroY);
        float travelled = 0;
        if (!perched) {
            if (decisionIn <= 0) {
                chooseGoal(heroX, heroY);
                if (step < 0) step = nextStep(goalX, goalY);
                decisionIn = .18f;
            }
            if (step >= 0) travelled = walk(elapsed);
        }
        play(travelled > 0 ? run : idle);
        pose(travelled > 0, perched);
        if (!Difficulties.is3d) {
            int cell = cellAt(footX, footY);
            if (cell >= floor.width() && Level.solid[cell - floor.width()]) {
                lift = Math.min(lift, Math.max(0, groundY() - height() - cell / floor.width() * 16));
            }
        }
        if (watching != null && travelled == 0 && watching.sprite != null) {
            flipHorizontal = watching.sprite.x < footX;
        }
        float targetOpacity = frightened && travelled == 0 ? .5f : 1;
        opacity += Math.max(-elapsed * 3, Math.min(elapsed * 3, targetOpacity - opacity));
        alpha(opacity);
        x = footX - width() / 2;
        y = groundY() - (Difficulties.is3d ? 6 : 0) - height() - lift;
        super.update();
    }

    private void lookAround(Hero hero) {
        watching = null;
        int nearby = 0;
        float closest = 80 * 80;
        for (Mob mob : Dungeon.level.mobs) {
            if (!mob.hostile || !mob.isAlive() || mob.invisible > 0 || mob.sprite == null || !mob.sprite.visible
                    || mob.pos < 0 || mob.pos >= Dungeon.visible.length || !Dungeon.visible[mob.pos]) continue;
            float dx = (mob.pos % floor.width() - hero.pos % floor.width()) * 16;
            float dy = (mob.pos / floor.width() - hero.pos / floor.width()) * 16;
            float distance = dx * dx + dy * dy;
            if (distance <= 48 * 48) nearby++;
            if (distance < closest) {
                closest = distance;
                watching = mob;
            }
        }
        frightened = watching != null && (nearby >= 3 || hero.HP < hero.HT / 3 || style == 1);
    }

    private void chooseGoal(float heroX, float heroY) {
        float radius = 17;
        float angle = age * .55f;
        float enemyX = watching == null ? heroX : (watching.pos % floor.width() + .5f) * 16;
        float enemyY = watching == null ? heroY : (watching.pos / floor.width() + .5f) * 16;
        if (frightened) {
            angle = (float)Math.atan2(heroY - enemyY, heroX - enemyX);
            radius = style == 1 ? 30 : 23;
        } else if (style == 4 && watching != null && distanceSquared(heroX, heroY, enemyX, enemyY) <= 48 * 48) {
            heroX = enemyX;
            heroY = enemyY;
            radius = 23;
            angle = age * 1.2f;
        } else if (style == 6) {
            radius = 15;
            angle = watching == null ? .7f + (float)Math.sin(age * .3f) * .4f
                    : (float)Math.atan2(heroY - enemyY, heroX - enemyX);
        } else if (style == 3) {
            angle = (float)Math.floor(age / 2) * 2.4f;
            radius = 22;
        } else if (style == 5) {
            angle = 2.4f + (float)Math.sin(age * .3f) * .6f;
            radius = 16;
        } else if (style == 2 && watching != null) {
            angle = (float)Math.atan2(enemyY - heroY, enemyX - heroX);
            radius = 13;
        }
        if (style == 1 && !frightened && age % 18 >= 13 && age % 18 < 16) radius = 0;
        goalX = heroX + (float)Math.cos(angle) * radius;
        goalY = heroY + (float)Math.sin(angle) * radius;
    }

    private boolean ride(float elapsed, float heroX, float heroY) {
        if (style != 1 || frightened) {
            riding = 0;
            return false;
        }
        float cycle = age % 18;
        if (riding == 0 && cycle >= 13 && cycle < 16
                && distanceSquared(footX, footY, heroX, heroY) < 16) riding = .001f;
        if (riding == 0) return false;
        riding += elapsed;
        if (riding >= 2.8f) {
            riding = 0;
            step = -1;
            decisionIn = 0;
            return false;
        }
        footX = heroX;
        footY = heroY;
        lift = 11 * Math.min(1, Math.min(riding / .35f, (2.8f - riding) / .35f));
        step = -1;
        return true;
    }

    private void pose(boolean moving, boolean perched) {
        float pulse = (float)Math.sin(age * (watching == null ? 5 : 11));
        scale.set(size);
        if (!perched) lift = 0;
        if (style == 2) {
            float swell = watching == null ? 1 : 1.2f;
            scale.set(size * swell * (1 + .13f * pulse), size * swell * (1 - .13f * pulse));
            lift = moving ? Math.max(0, pulse) * 3 : 0;
        } else if (style == 3 && moving) {
            lift = Math.abs((float)Math.sin(age * 9)) * 4;
        } else if (style == 6) {
            lift = 4 + (float)Math.sin(age * 2) * 1.5f;
        } else if (style == 5 && moving) {
            lift = Math.abs((float)Math.sin(age * 6)) * 1.2f;
        }
    }

    private float walk(float elapsed) {
        int from = cellAt(footX, footY);
        if (!canStep(from, step)) {
            step = -1;
            return 0;
        }
        float targetX = (step % floor.width() + .5f) * 16;
        float targetY = (step / floor.width() + .5f) * 16;
        float dx = targetX - footX, dy = targetY - footY;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        float speed;
        switch (style) {
            case 1: speed = 42; break;
            case 3: speed = 38; break;
            case 4: speed = 28; break;
            case 5: speed = 19; break;
            default: speed = 24;
        }
        if (frightened) speed *= 1.35f;
        float move = Math.min(distance, speed * elapsed);
        if (distance > .01f) {
            footX += dx / distance * move;
            footY += dy / distance * move;
            if (Math.abs(dx) > .1f) flipHorizontal = dx < 0;
        }
        if (distance <= move + .01f) {
            step = -1;
            decisionIn = 0;
        }
        return move;
    }

    private int nextStep(float targetX, float targetY) {
        int start = cellAt(footX, footY);
        if (!walkable(start)) return -1;
        int left = start % floor.width() - SEARCH_WIDTH / 2;
        int top = start / floor.width() - SEARCH_WIDTH / 2;
        int center = queue.length / 2;
        Arrays.fill(firstStep, -2);
        firstStep[center] = -1;
        queue[0] = center;
        int count = 1, best = -1;
        float bestDistance = distanceSquared(footX, footY, targetX, targetY);
        for (int head = 0; head < count; head++) {
            int local = queue[head];
            int x = left + local % SEARCH_WIDTH, y = top + local / SEARCH_WIDTH;
            int cell = y * floor.width() + x;
            float distance = distanceSquared((x + .5f) * 16, (y + .5f) * 16, targetX, targetY);
            if (distance < bestDistance - 1) {
                bestDistance = distance;
                best = firstStep[local];
            }
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int nx = local % SEARCH_WIDTH + dx, ny = local / SEARCH_WIDTH + dy;
                    if (nx < 0 || nx >= SEARCH_WIDTH || ny < 0 || ny >= SEARCH_WIDTH) continue;
                    int next = ny * SEARCH_WIDTH + nx;
                    if (firstStep[next] != -2) continue;
                    int destX = x + dx, destY = y + dy;
                    if (destX < 0 || destX >= floor.width() || destY < 0 || destY >= floor.height()) continue;
                    int dest = destY * floor.width() + destX;
                    if (!canStep(cell, dest)) continue;
                    firstStep[next] = local == center ? dest : firstStep[local];
                    queue[count++] = next;
                }
            }
        }
        return best;
    }

    private boolean canStep(int from, int to) {
        if (!walkable(to)) return false;
        int dx = to % floor.width() - from % floor.width();
        int dy = to / floor.width() - from / floor.width();
        if (Math.abs(dx) > 1 || Math.abs(dy) > 1) return false;
        return dx == 0 || dy == 0 || walkable(from + dx) && walkable(from + dy * floor.width());
    }

    private boolean walkable(int cell) {
        if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()
                || !Level.passable[cell] || Level.solid[cell] || Level.pit[cell]
                || !Dungeon.visible[cell]) return false;
        Char occupant = Actor.findChar(cell);
        return occupant == null || occupant == owner.ch;
    }

    private int cellAt(float x, float y) {
        if (Dungeon.level == null || x < 0 || y < 0 || x >= Dungeon.level.width() * 16 || y >= Dungeon.level.height() * 16) return -1;
        return (int)(y / 16) * Dungeon.level.width() + (int)(x / 16);
    }

    private static float distanceSquared(float x, float y, float otherX, float otherY) {
        float dx = x - otherX, dy = y - otherY;
        return dx * dx + dy * dy;
    }
}
