package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.LibraryCreatureStats;
import com.bilboldev.pixeldungeonskills.LibraryHeroBuild;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import java.util.ArrayList;
import java.util.Locale;

public final class LibraryStats extends Component {
    public enum Stat {
        HEALTH, MANA, DAMAGE, ARMOR, ACCURACY, EVASION, ATTACK_TIME, MOVE_TIME, STEALTH;

        Image icon() {
            switch (this) {
                case HEALTH: return buff(BuffIndicator.HEART);
                case MANA: return new ItemSprite(ItemSpriteSheet.WAND_MAGIC_MISSILE, null);
                case DAMAGE: return new ItemSprite(ItemSpriteSheet.SWORD, null);
                case ARMOR: return new ItemSprite(ItemSpriteSheet.ARMOR_PLATE, null);
                case ACCURACY: return Icons.TARGET.get();
                case EVASION: return buff(BuffIndicator.LEVITATION);
                case ATTACK_TIME: return Icons.BUSY.get();
                case MOVE_TIME: return buff(BuffIndicator.SLOW);
                case STEALTH: return buff(BuffIndicator.SHADOWS);
                default: throw new IllegalArgumentException("Unknown stat");
            }
        }
    }

    private final boolean compact = Window.landscapeLayout();
    private final int gap = compact ? 1 : 4;
    private final ArrayList<StatCell> cells = new ArrayList<>();
    private final HighlightedText title;

    private LibraryStats(float width, boolean creature) {
        this.width = width;
        if (creature) {
            title = new HighlightedText(8);
            title.setColor(0xECD095, 0xECD095);
            title.text(LibraryCatalog.text("creature_stats"), (int)width - 8, false);
        } else {
            title = null;
        }
        if (title != null) add(title);
    }

    public void stat(Stat stat, String value) {
        StatCell cell = new StatCell(stat, value, (width - gap - 8) / 2, compact);
        cells.add(cell);
        add(cell);
        layout();
    }

    @Override
    protected void layout() {
        float top = y + 4;
        if (title != null) {
            title.setPos(x + 4, y + 3);
            top = title.bottom() + 7;
        }
        for (int i = 0; i < cells.size(); i += 2) {
            float rowHeight = cells.get(i).height();
            if (i + 1 < cells.size()) rowHeight = Math.max(rowHeight, cells.get(i + 1).height());
            for (int column = 0; column < 2 && i + column < cells.size(); column++) {
                StatCell cell = cells.get(i + column);
                cell.setPos(x + 4 + column * (cell.width() + gap), top);
            }
            top += rowHeight + gap;
        }
        height = Math.max(0, top - y - gap + 4);
    }

    public static LibraryStats hero(float width, LibraryHeroBuild.Stats stats) {
        LibraryStats result = new LibraryStats(width, false);
        result.stat(Stat.HEALTH, String.valueOf(stats.maxHealth));
        result.stat(Stat.MANA, String.valueOf(stats.maxMana));
        result.stat(Stat.DAMAGE, stats.damageMin + "-" + stats.damageMax);
        result.stat(Stat.ARMOR, stats.armorMax == 0 ? "0" : "0-" + stats.armorMax);
        result.stat(Stat.ACCURACY, String.valueOf(stats.accuracy));
        result.stat(Stat.EVASION, String.valueOf(stats.evasion));
        result.stat(Stat.ATTACK_TIME, turns(stats.attackTurns));
        result.stat(Stat.MOVE_TIME, turns(stats.moveTurns));
        result.stat(Stat.STEALTH, String.valueOf(stats.stealth));
        return result;
    }

    public static LibraryStats creature(float width, LibraryCreatureStats stats) {
        LibraryStats result = new LibraryStats(width, true);
        result.stat(Stat.HEALTH, stats.health);
        result.stat(Stat.DAMAGE, stats.damage);
        result.stat(Stat.ARMOR, stats.armor);
        result.stat(Stat.ACCURACY, stats.accuracy);
        result.stat(Stat.EVASION, stats.evasion);
        return result;
    }

    private static String turns(float value) {
        return value < .01f ? "<" + Messages.format("%.2f", .01f) : Messages.format("%.2f", value);
    }

    private static final class StatCell extends Component {
        final Image icon;
        final HighlightedText label, value;
        final int padding;

        StatCell(Stat stat, String amount, float width, boolean compact) {
            padding = compact ? 1 : 2;
            icon = stat.icon();
            icon.scale.set(12 / Math.max(icon.width, icon.height));
            add(icon);
            label = new HighlightedText(7);
            label.setColor(0xBCA98B, 0xECD095);
            label.text(LibraryCatalog.text("stat_" + stat.name().toLowerCase(Locale.ROOT)), (int)width - 18, false);
            add(label);
            value = new HighlightedText(9);
            value.setColor(0xDDD4C5, 0xECD095);
            value.text(amount, (int)width - 18, false);
            add(value);
            setSize(width, Math.max(compact ? 20 : 26, label.height() + value.height() + padding * 3));
        }

        @Override
        protected void layout() {
            icon.x = x;
            icon.y = y + (height - icon.height()) / 2;
            label.setPos(x + 18, y + padding);
            value.setPos(label.left(), label.bottom() + padding);
        }
    }

    private static Image buff(int index) {
        Image image = new Image(Assets.BUFFS_LARGE);
        image.frame(new TextureFilm(image.texture, 16, 16).get(index));
        return image;
    }
}
