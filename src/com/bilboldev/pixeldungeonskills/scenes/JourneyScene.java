package com.bilboldev.pixeldungeonskills.scenes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.Tilemap;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.JourneyForces;
import com.bilboldev.pixeldungeonskills.JourneyRoute;
import com.bilboldev.pixeldungeonskills.JourneyRun;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.effects.CombatTextIcon;
import com.bilboldev.pixeldungeonskills.effects.BannerSprites;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.WindParticle;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTileSheet;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.GooSprite;
import com.bilboldev.pixeldungeonskills.sprites.TenguSprite;
import com.bilboldev.pixeldungeonskills.sprites.DM300Sprite;
import com.bilboldev.pixeldungeonskills.sprites.KingSprite;
import com.bilboldev.pixeldungeonskills.sprites.YogSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.Banner;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndJourneyChest;
import com.bilboldev.pixeldungeonskills.windows.WndJourneyShop;
import com.bilboldev.pixeldungeonskills.windows.WndInfoItem;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.pixeldungeonskills.windows.WndSkillsNew;
import com.bilboldev.pixeldungeonskills.windows.WndScrollable;
import com.bilboldev.pixeldungeonskills.windows.WndTitledMessage;
import com.bilboldev.pixeldungeonskills.windows.IconTitle;

public class JourneyScene extends PixelScene {
    private CampWindow camp;
    private AreaWindow areaWindow;
    private CampButton roster;
    private Image rosterSleep;
    private RouteMap map;
    private ScrollPane routes;
    private int selectedNode;
    private boolean saveFailed, refreshCamp;

    @Override public void create() {
        super.create();
        if (!JourneyRun.active() || !JourneyRun.inCamp()) {
            Game.switchScene(TitleScene.class);
            return;
        }
        Music.INSTANCE.play(Presentation.titleMusic(), true);
        int w = Camera.main.width, h = Camera.main.height;
        add(new ColorBlock(w, h, 0xFF000000));
        Emitter wind = new Emitter();
        wind.pos(0, 0, w, h);
        wind.pour(WindParticle.FACTORY, .04f);
        add(wind);

        selectedNode = JourneyRun.currentNode().id;
        map = new RouteMap(w, h - 36, w > h);
        routes = new ScrollPane(map) {
            @Override public void onClick(float x, float y) { map.select(x, y); }

            @Override protected boolean scrollByMouseWheel(float amount) {
                if (!map.horizontal) return super.scrollByMouseWheel(amount);
                float previousX = map.camera.scroll.x;
                scrollTo(previousX + amount * Math.max(12, width() * .25f), map.camera.scroll.y);
                return previousX != map.camera.scroll.x;
            }
        };
        add(routes);
        routes.setRect(0, 36, w, h - 36);
        if (map.horizontal) {
            routes.scrollTo(map.nodeX(JourneyRun.node(selectedNode)) - routes.width() * .5f,
                    (map.height() - routes.height()) / 2);
        } else {
            routes.scrollTo(map.nodeX(JourneyRun.node(selectedNode)) - routes.width() * .5f,
                    map.nodeY(JourneyRun.node(selectedNode)) - routes.height() * .5f);
        }

        CampButton back = new CampButton(Messages.get("scenes.titlescene.back"), null, this::onBackPressed);
        back.setRect(8, 8, 48, 22);
        add(back);
        BitmapText title = createText(Messages.get("journey.title"), 12);
        title.measure();
        if (title.width() > w - 136) title.scale.set(title.scale.x * (w - 136) / title.width());
        title.x = (w - title.width()) / 2;
        title.y = 13;
        title.hardlight(0xE6C891);
        add(title);
        roster = new CampButton("", selectedHeroImage(), this::showCamp);
        roster.setRect(w - 32, 6, 26, 26);
        add(roster);
        rosterSleep = Icons.SLEEP.get();
        rosterSleep.scale.set(.75f);
        rosterSleep.x = roster.right() - rosterSleep.width() - 2;
        rosterSleep.y = roster.top() + 1;
        rosterSleep.visible = JourneyRun.resting(JourneyRun.selectedClass());
        add(rosterSleep);
        ColorBlock rule = new ColorBlock(w - 16, 1, 0xFF302D2C);
        rule.x = 8;
        rule.y = 35;
        add(rule);
        fadeIn();
        if (JourneyRun.finished() && !JourneyRun.victory()) {
            Banner gameOver = new Banner(BannerSprites.get(BannerSprites.Type.GAME_OVER));
            gameOver.scale.set(Math.min(1, (w - 24f) / gameOver.width()));
            gameOver.x = align((w - gameOver.width()) / 2);
            gameOver.y = align((h - gameOver.height()) / 2);
            gameOver.show(0x000000, .8f);
            add(gameOver);
        }
        Badges.showJourneyComplete();
    }

    public void refreshCamp() {
        refreshCamp = true;
    }

    private void showCamp() {
        if (camp != null) camp.hide();
        map.refreshCampHeroes();
        add(camp = new CampWindow());
        roster.icon(selectedHeroImage());
        rosterSleep.visible = JourneyRun.resting(JourneyRun.selectedClass());
    }

    private static Image selectedHeroImage() {
        Hero hero = JourneyRun.selectedHero();
        return JourneyRun.alive(hero.heroClass) ? LibraryCatalog.heroImage(hero.spritesheet(), hero.tier())
                : new ItemSprite(ItemSpriteSheet.BONES, null);
    }

    private void showArea() {
        JourneyRoute.Node node = JourneyRun.node(selectedNode);
        if (JourneyRun.finished() || JourneyRun.victory() && !JourneyRun.hasRemains(node.id)) {
            add(new WndOptions(areaImage(node), Messages.get("journey.title"), areaDescription(node),
                    Messages.get("journey.new"), Messages.get("scenes.titlescene.back")) {
                @Override protected void onSelect(int index) { if (index == 0) newJourney(); }
            });
        } else if (node.id == 0) {
            showCamp();
        } else {
            add(areaWindow = new AreaWindow(node));
        }
    }

    private void deploy() {
        try {
            JourneyRun.deploy(selectedNode);
        } catch (IOException error) {
            showError("journey.start_failed");
        }
    }

    private void newJourney() {
        onBackPressed();
    }

    private String areaDescription(JourneyRoute.Node node) {
        if (JourneyRun.finished()) return Messages.get(JourneyRun.victory() ? "journey.reached" : "journey.lost");
        if (JourneyRun.victory()) return Messages.get("journey.reached")
                + (JourneyRun.hasRemains(node.id) ? "\n\n" + Messages.get("journey.recovery_objective") : "");
        if (node.id == 0) return Messages.get("journey.choose");
        String description = Messages.get("journey.stage", node.stage) + " · "
                + Messages.get("journey." + node.biome.name().toLowerCase(Locale.ROOT)) + "\n"
                + Messages.get("journey.floors", node.floors);
        Item loot = JourneyRun.reward(node.id);
        if (loot != null) description += "\n\n" + Messages.get("journey.loot", (Object)loot.toString());
        if (!node.completed || JourneyRun.recovery(node.id)) {
            ArrayList<JourneyForces.Troop> troops = new ArrayList<>();
            for (JourneyForces.Group group : JourneyRun.forces()) {
                if (group.node != node.id) continue;
                for (JourneyForces.Troop troop : group.troops) if (!troops.contains(troop)) troops.add(troop);
            }
            if (!troops.isEmpty()) description += "\n\n" + reinforcementText(troops) + "\n"
                    + Messages.get("journey.leader_blocked");
        }
        description += "\n\n" + Messages.get(JourneyRun.recovery(node.id) ? "journey.recovery_objective" : node.completed ? "journey.cleared"
                : node.finish && !JourneyRun.bossDefeated(node.id) ? "journey.final_objective"
                : node.gate && !JourneyRun.bossDefeated(node.id) ? "journey.boss_objective"
                : objectiveKey(node));
        if (!node.completed && !JourneyRun.available(node.id))
            description += "\n\n" + Messages.get("journey.blocked");
        if ((node.gate || node.finish) && !JourneyRun.bossDefeated(node.id)) {
            description += "\n\n_" + Messages.get("journey." + bossName(node) + "_mechanic") + "_";
            description += "\n\n" + Messages.get("journey.boss_dispatch");
            if (JourneyRun.outpostCleared(node.biome)) description += "\n\n" + Messages.get("journey.boss_unsupported");
            else {
                description += "\n\n" + Messages.get("journey.boss_garrison");
                for (JourneyRoute.Node other : JourneyRun.route().nodes) {
                    if (other.biome == node.biome && other.objective == JourneyRoute.Objective.OUTPOST) {
                        description += " " + Messages.get("journey.outpost_hint");
                        break;
                    }
                }
            }
        }
        if (node.gate && node.biome == JourneyRoute.Biome.PRISON)
            description += "\n\n" + Messages.get("journey.subclass_reward");
        if (JourneyRun.hasRemains(node.id) && !JourneyRun.recovery(node.id))
            description += "\n\n_" + Messages.get("journey.remains") + "_";
        return description;
    }

    private static String reinforcementText(ArrayList<JourneyForces.Troop> troops) {
        String names = "";
        for (int i = 0; i < troops.size(); i++) {
            String name = "_" + Messages.get(troops.get(i).mobClass, "name") + "_";
            names = i == 0 ? name : Messages.get(i == troops.size() - 1
                    ? "journey.troops_and" : "journey.troops_comma", (Object)names, name);
        }
        return Messages.get("journey.reinforced", (Object)names);
    }

    public static String areaName(JourneyRoute.Node node) {
        if (node.id == 0) return Messages.get("journey.camp");
        if (node.finish) return Messages.get("journey.destination");
        if (node.gate) return Messages.get("actors.mobs." + bossName(node) + ".name");
        if (node.objective == JourneyRoute.Objective.NONE)
            return Messages.get("journey." + node.biome.name().toLowerCase(Locale.ROOT));
        return Messages.get("journey." + node.objective.name().toLowerCase(Locale.ROOT));
    }

    public static String objectiveKey(JourneyRoute.Node node) {
        switch (node.objective) {
            case CHECKPOINT: return "journey.checkpoint_objective";
            case SUPPLIES: return "journey.supply_objective";
            case RESCUE: return "journey.rescue_objective";
            case OUTPOST: return "journey.outpost_objective";
            default: return "journey.objective";
        }
    }

    private static String bossName(JourneyRoute.Node node) {
        switch (node.biome) {
            case SEWERS: return "goo";
            case PRISON: return "tengu";
            case CAVES: return "dm300";
            case CITY: return "king";
            default: return "yog";
        }
    }

    static Image areaImage(JourneyRoute.Node node) {
        int sprite = 0;
        if (node.finish) sprite = 17;
        else if (node.gate) sprite = 13 + node.biome.ordinal();
        else if (node.id != 0) {
            int variant = node.id % 2;
            switch (node.biome) {
                case SEWERS: sprite = 1 + variant; break;
                case PRISON: sprite = 3 + variant; break;
                case CAVES: sprite = 5 + variant; break;
                case CITY: sprite = 7 + variant; break;
                default: sprite = 9; break;
            }
        }
        Image image = new Image(Assets.JOURNEY_NODES);
        image.frame(sprite * 32, 0, 32, 32);
        return image;
    }

    private static Image bossImage(JourneyRoute.Node node) {
        CharSprite sprite;
        switch (node.biome) {
            case SEWERS: sprite = new GooSprite(); break;
            case PRISON: sprite = new TenguSprite(); break;
            case CAVES: sprite = new DM300Sprite(); break;
            case CITY: sprite = new KingSprite(); break;
            default: sprite = new YogSprite(); break;
        }
        Image image;
        if (JourneyRun.bossDefeated(node.id)) {
            image = new Image(sprite.texture);
            MovieClip.Animation death = sprite.deathAnimation();
            image.frame(death.frames[death.frames.length - 1]);
            image.active = false;
        } else {
            MovieClip idle = new MovieClip(sprite.texture);
            idle.play(sprite.idleAnimation());
            image = idle;
        }
        image.scale.set(.75f);
        sprite.destroy();
        return image;
    }

    private void showError(String key) { add(new WndMessage(Messages.get(key))); }

    @Override public void pause() {
        super.pause();
        try {
            if (JourneyRun.active()) JourneyRun.save();
        } catch (IOException error) {
            saveFailed = true;
        }
    }

    @Override public void update() {
        super.update();
        if (refreshCamp && (Window.topWindow() == camp || Window.topWindow() == null)) {
            refreshCamp = false;
            if (camp != null) showCamp();
        }
        if (saveFailed && Window.topWindow() == null) {
            saveFailed = false;
            showError("journey.save_failed");
        }
    }

    @Override protected void onBackPressed() {
        try {
            JourneyRun.leave();
            Game.switchScene(TitleScene.class);
        } catch (IOException error) {
            showError("journey.save_failed");
        }
    }

    private void chooseSubclass() {
        Hero hero = JourneyRun.selectedHero();
        if (!JourneyRun.canChooseSubclass(hero)) return;
        HeroSubClass[] choices = JourneyRun.subclassChoices(hero.heroClass);
        String first = Messages.capitalize(Message.subclass(choices[0], false).toString());
        String second = Messages.capitalize(Message.subclass(choices[1], false).toString());
        String description = "_" + first + "_\n" + Messages.get("skills4.subclass." + choices[0].name().toLowerCase(Locale.ROOT))
                + "\n\n_" + second + "_\n" + Messages.get("skills4.subclass." + choices[1].name().toLowerCase(Locale.ROOT));
        add(new WndOptions(new ItemSprite(ItemSpriteSheet.MASTERY, null), Messages.get("journey.choose_subclass"),
                description, 2, false, first, second, Messages.get("windows.wndchooseability.cancel")) {
            @Override protected boolean fullWidthOption(int index) { return index == 2; }

            @Override protected void onSelect(int index) {
                if (index == 2) return;
                try {
                    if (JourneyRun.chooseSubclass(hero, choices[index])) {
                        Sample.INSTANCE.play(Assets.SND_MASTERY);
                        showCamp();
                    }
                } catch (IOException error) {
                    showError("journey.save_failed");
                }
            }
        });
    }

    private class CampWindow extends Window {
        private final HeroSummary summary;

        CampWindow() {
            int w = Math.min(224, maxFrameWidth() - chrome.marginHor());
            IconTitle heading = new IconTitle(areaImage(JourneyRun.node(0)), Messages.get("journey.camp"));
            heading.setRect(0, 0, w, 0);
            add(heading);

            float y = heading.bottom() + 6;
            int columns = JourneyRun.HEROES.length;
            float size = Math.min(34, (w - (columns - 1) * 3) / columns);
            float rosterWidth = columns * size + (columns - 1) * 3;
            for (int i = 0; i < columns; i++) {
                HeroButton button = new HeroButton(JourneyRun.HEROES[i]);
                button.setRect((w - rosterWidth) / 2 + i * (size + 3), y, size, size);
                add(button);
            }
            y += size + 8;

            summary = new HeroSummary(w);
            summary.setPos(0, y);
            add(summary);
            y = summary.bottom() + 8;
            if (JourneyRun.canChooseSubclass(JourneyRun.selectedHero())) {
                CampButton subclass = new CampButton(Messages.get("journey.choose_subclass"),
                        new ItemSprite(ItemSpriteSheet.MASTERY, null), JourneyScene.this::chooseSubclass);
                subclass.setRect(0, y, w, 37);
                add(subclass);
                y = subclass.bottom() + 8;
            }
            ColorBlock divider = new ColorBlock(w, 1, 0xFF51484A);
            divider.y = y;
            add(divider);
            Image coins = new ItemSprite(ItemSpriteSheet.GOLD, null);
            coins.scale.set(.65f);
            BitmapText gold = createText(Integer.toString(JourneyRun.gold()), 7);
            gold.measure();
            gold.hardlight(0xD6BE98);
            gold.x = w - gold.width();
            gold.y = align((heading.height() - gold.height()) / 2 - 2);
            coins.x = gold.x - coins.width() - 3;
            coins.y = align(gold.y + (gold.height() - coins.height()) / 2);
            add(coins);
            add(gold);

            CampButton inventory = new CampButton(Messages.get("journey.inventory"),
                    Icons.BACKPACK.get(), () -> JourneyScene.this.add(new WndJourneyChest(false)));
            CampButton skills = new CampButton(Messages.get("library.skill_tree"), Icons.SKILLS.get(),
                    () -> JourneyScene.this.add(new WndSkillsNew(JourneyRun.selectedHero(), Skill.availableSkill) {
                        @Override public void hide() {
                            super.hide();
                            summary.refresh();
                        }
                    }));
            CampButton chest = new CampButton(Messages.get("journey.chest"),
                    new ItemSprite(ItemSpriteSheet.CHEST, null), () -> JourneyScene.this.add(new WndJourneyChest(true)));
            CampButton shop = new CampButton(Messages.get("journey.shop"),
                    WndJourneyShop.shopkeeperImage(), () -> JourneyScene.this.add(new WndJourneyShop()));
            float buttonWidth = (w - 4) / 2f;
            inventory.setRect(0, y + 6, buttonWidth, 37);
            skills.setRect(inventory.right() + 4, inventory.top(), buttonWidth, 37);
            chest.setRect(0, inventory.bottom() + 4, buttonWidth, 37);
            shop.setRect(chest.right() + 4, chest.top(), buttonWidth, 37);
            inventory.enable(!JourneyRun.finished());
            chest.enable(!JourneyRun.finished());
            shop.enable(!JourneyRun.finished());
            add(inventory);
            add(skills);
            add(chest);
            add(shop);
            resize(w, (int)shop.bottom());
        }

        @Override public void hide() {
            super.hide();
            camp = null;
            if (areaWindow != null) areaWindow.refreshHero();
        }
    }

    private class AreaWindow extends WndScrollable {
        final HeroSummary summary;
        private final CampButton heroes, deploy;
        private final int nodeId;

        AreaWindow(JourneyRoute.Node node) {
            nodeId = node.id;
            int w = boundedWidth(200);
            IconTitle heading = new IconTitle(areaImage(node), areaName(node));
            heading.setRect(0, 0, w, 0);
            HighlightedText description = new HighlightedText(7);
            description.setColor(0xD2CBC4, titleColor());
            description.text(areaDescription(node), textWidth(w), false);
            summary = new HeroSummary(w);
            deploy = new CampButton(Messages.get(JourneyRun.recovery(node.id) ? "journey.recover" : "journey.deploy"),
                    Icons.EXIT.get(), () -> {
                        hide();
                        JourneyScene.this.deploy();
                    });
            deploy.enable(JourneyRun.available(node.id) && JourneyRun.canDeploySelected());
            heroes = new CampButton(Messages.get("journey.heroes"), selectedHeroImage(), JourneyScene.this::showCamp);
            float buttonWidth = (w - 5) / 2f;
            deploy.setSize(buttonWidth, 36);
            heroes.setSize(buttonWidth, 36);
            layoutContent(w, heading, description, summary, 5, deploy, heroes);
        }

        void refreshHero() {
            summary.refresh();
            heroes.icon(selectedHeroImage());
            deploy.enable(JourneyRun.available(nodeId) && JourneyRun.canDeploySelected());
        }

        @Override public void hide() {
            super.hide();
            areaWindow = null;
        }
    }

    private class HeroSummary extends Component {
        private final Image portrait = new Image();
        private final Image sleep = Icons.SLEEP.get();
        private final HighlightedText identity = new HighlightedText(8);
        private final Image health = new Image(Assets.DAMAGE_ICONS);
        private final Image mana = new Image(Assets.DAMAGE_ICONS);
        private final BitmapText hp = createText("", 7);
        private final BitmapText mp = createText("", 7);
        private final TouchArea restInfo = new TouchArea(0, 0, 36, 42) {
            @Override protected void onClick(Touch touch) {
                Sample.INSTANCE.play(Assets.SND_CLICK);
                JourneyScene.this.add(new WndTitledMessage(Icons.SLEEP.get(),
                        Messages.get("journey.resting"), Messages.get("journey.rest_desc")));
            }
        };

        HeroSummary(int width) {
            add(portrait);
            sleep.scale.set(.75f);
            add(sleep);
            add(restInfo);
            identity.setColor(0xE6D9C7, 0xE6C891);
            add(identity);
            health.frame(CombatTextIcon.HEALING.id * CombatTextIcon.WIDTH, 0, CombatTextIcon.WIDTH, CombatTextIcon.HEIGHT);
            health.hardlight(0xC77F77);
            add(health);
            mana.frame(CombatTextIcon.MANA.id * CombatTextIcon.WIDTH, 0, CombatTextIcon.WIDTH, CombatTextIcon.HEIGHT);
            mana.hardlight(0x899FD1);
            add(mana);
            hp.hardlight(0xB9B9C4);
            mp.hardlight(0xB9B9C4);
            add(hp);
            add(mp);
            setSize(width, 42);
            refresh();
        }

        void refresh() {
            Hero hero = JourneyRun.selectedHero();
            portrait.copy(selectedHeroImage());
            portrait.scale.set(2);
            String role = hero.subClass == HeroSubClass.NONE ? Message.heroClass(hero.heroClass).toString()
                    : Message.subclass(hero.subClass, false).toString();
            String name = Messages.get("daily.hero", hero.lvl, Messages.capitalize(role));
            sleep.visible = restInfo.active = JourneyRun.resting(hero.heroClass);
            if (sleep.visible) name += "\n_" + Messages.get("journey.resting") + "_";
            identity.text(name, (int)width - 42, false);
            hp.text(hero.HP + "/" + hero.HT);
            mp.text(hero.MP + "/" + hero.MMP);
            hp.measure();
            mp.measure();
            layout();
        }

        @Override protected void layout() {
            super.layout();
            if (portrait == null) return;
            portrait.x = align(x + (36 - portrait.width()) / 2);
            portrait.y = align(y + (height - portrait.height()) / 2);
            sleep.x = align(portrait.x + portrait.width() - 5);
            sleep.y = align(portrait.y - 3);
            restInfo.x = x;
            restInfo.y = y;
            float lineHeight = identity.height() + 5 + hp.height();
            identity.setPos(x + 42, align(y + (height - lineHeight) / 2));
            health.x = x + 42;
            hp.y = mp.y = align(identity.bottom() + 5);
            health.y = align(hp.y + (hp.height() - health.height()) / 2);
            hp.x = health.x + health.width() + 3;
            mana.x = hp.x + hp.width() + 10;
            mana.y = align(mp.y + (mp.height() - mana.height()) / 2);
            mp.x = mana.x + mana.width() + 3;
        }
    }

    private class HeroButton extends Button {
        final HeroClass type;
        final RebornChrome frame;
        final Image portrait;
        final Image sleep;
        final ColorBlock health;

        HeroButton(HeroClass type) {
            this.type = type;
            Hero hero = JourneyRun.hero(type);
            frame = new RebornChrome(Chrome.Type.BUTTON).material(RebornChrome.Material.IRON);
            frame.highlight(type == JourneyRun.selectedClass());
            add(frame);
            portrait = JourneyRun.alive(type) ? LibraryCatalog.heroImage(hero.spritesheet(), hero.tier())
                    : new ItemSprite(ItemSpriteSheet.BONES, null);
            add(portrait);
            sleep = Icons.SLEEP.get();
            sleep.scale.set(.75f);
            sleep.visible = JourneyRun.resting(type);
            add(sleep);
            health = new ColorBlock(1, 2, 0xFF947B51);
            add(health);
        }

        @Override protected void layout() {
            super.layout();
            if (frame == null) return;
            frame.x = x;
            frame.y = y;
            frame.size(width, height);
            portrait.x = align(x + (width - portrait.width()) / 2);
            portrait.y = align(y + (height - portrait.height()) / 2 - 1);
            sleep.x = align(portrait.x + portrait.width() - 4);
            sleep.y = align(Math.max(y + 2, portrait.y - sleep.height()));
            Hero hero = JourneyRun.hero(type);
            health.size(Math.max(0, width - 6) * Math.max(0, hero.HP) / Math.max(1, hero.HT), 2);
            health.x = x + 3;
            health.y = bottom() - 4;
        }

        @Override protected void onClick() {
            Sample.INSTANCE.play(Assets.SND_CLICK);
            if (JourneyRun.finished()) return;
            if (!JourneyRun.alive(type)) {
                JourneyScene.this.add(new WndTitledMessage(new ItemSprite(ItemSpriteSheet.BONES, null),
                        Message.heroClass(type).toString(), Messages.get("journey.fallen")));
                return;
            }
            JourneyRun.select(type);
            showCamp();
            try {
                JourneyRun.save();
            } catch (IOException error) {
                showError("journey.save_failed");
            }
        }
    }

    private class RouteMap extends Component {
        private static final int TILE = 8;
        private static final int LAYER_TILES = 8;
        private final ArrayList<RouteMarker> markers = new ArrayList<>();
        private final ArrayList<ForceMarker> forces = new ArrayList<>();
        private final ArrayList<MovieClip> campHeroes = new ArrayList<>();
        private final ArrayList<Image> campSleep = new ArrayList<>();
        private final int layers, columns, rows;
        private final boolean[] ground;
        private final boolean horizontal;

        RouteMap(float w, float h, boolean horizontal) {
            this.horizontal = horizontal;
            int last = 0;
            for (JourneyRoute.Node node : JourneyRun.route().nodes) last = Math.max(last, node.layer);
            layers = last;
            columns = horizontal ? (layers + 1) * LAYER_TILES + 4 : (int)w / TILE;
            rows = horizontal ? (int)h / TILE : (layers + 1) * LAYER_TILES + 4;
            ground = new boolean[columns * rows];
            setSize(horizontal ? columns * TILE : w, horizontal ? h : rows * TILE);
            for (JourneyRoute.Node node : JourneyRun.route().nodes) {
                for (int next : node.next) {
                    JourneyRoute.Node target = JourneyRun.node(next);
                    for (int cell : routeCells(node, target)) ground[cell] = true;
                }
            }
            java.util.Random random = new java.util.Random(JourneyRun.route().seed ^ 0x6c6564676573L);
            for (JourneyRoute.Node node : JourneyRun.route().nodes) {
                int cx = (int)nodeX(node) / TILE, cy = (int)(nodeY(node) + 12) / TILE;
                for (int dy = -2; dy <= 2; dy++) {
                    int radius = Math.abs(dy) == 2 ? 1 : 2;
                    for (int dx = -radius; dx <= radius; dx++) {
                        if (Math.abs(dx) == 2 && Math.abs(dy) == 1 && random.nextBoolean()) continue;
                        ground[cell(cx + dx, cy + dy)] = true;
                    }
                }
            }
            for (JourneyRoute.Node node : JourneyRun.route().nodes) {
                RouteMarker marker = new RouteMarker(node);
                marker.setRect(nodeX(node) - 18, nodeY(node) - 18, 36, 36);
                marker.selection.visible = node.id == selectedNode;
                add(marker);
                markers.add(marker);
            }
            if (!JourneyRun.victory()) for (JourneyForces.Group group : JourneyRun.forces()) {
                JourneyRoute.Node from = JourneyRun.node(group.node);
                JourneyRoute.Node destination = JourneyRun.node(group.previous < 0 ? group.target : group.previous);
                boolean facingLeft = nodeX(destination) <= nodeX(from);
                ForceMarker marker = null;
                for (ForceMarker other : forces) {
                    if (other.node == group.node && other.previous == (group.traveling ? group.previous : -1)) {
                        marker = other;
                        break;
                    }
                }
                if (marker == null) {
                    marker = new ForceMarker(group);
                    forces.add(marker);
                    add(marker);
                }
                marker.include(group, facingLeft);
            }
            for (ForceMarker marker : forces) marker.finish(this);
            refreshCampHeroes();
            int[] tiles = new int[ground.length];
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    int cell = cell(x, y);
                    if (ground[cell]) {
                        int variant = random.nextInt(12);
                        tiles[cell] = variant == 0 ? DungeonTileSheet.FLOOR_DECO
                                : variant < 4 ? DungeonTileSheet.FLOOR_ALT_1
                                : variant < 6 ? DungeonTileSheet.FLOOR_ALT_2 : DungeonTileSheet.FLOOR;
                    } else {
                        tiles[cell] = DungeonTileSheet.stitchChasmTile(solid(x, y - 1) ? Terrain.EMPTY : Terrain.CHASM);
                    }
                }
            }
            terrain(tiles);
        }

        void refreshCampHeroes() {
            for (MovieClip image : campHeroes) {
                remove(image);
                image.destroy();
            }
            campHeroes.clear();
            for (Image sleep : campSleep) {
                remove(sleep);
                sleep.destroy();
            }
            campSleep.clear();
            Hero[] living = JourneyRun.livingHeroes();
            JourneyRoute.Node camp = JourneyRun.node(0);
            for (int i = 0; i < living.length; i++) {
                Hero hero = living[i];
                MovieClip image = LibraryCatalog.heroImage(hero.spritesheet(), hero.tier());
                image.scale.set(.5f);
                int count = Math.min(3, living.length - i / 3 * 3);
                int cx = (int)(nodeX(camp) / TILE) + i % 3 - count / 2;
                int cy = (int)((nodeY(camp) + 24) / TILE) + i / 3 * 2;
                for (int floorY = (int)((nodeY(camp) + 24) / TILE); floorY <= cy; floorY++)
                    ground[cell(cx, floorY)] = true;
                image.x = align((cx + .5f) * TILE - image.width() / 2);
                image.y = align((cy + .5f) * TILE - image.height());
                add(image);
                campHeroes.add(image);
                Image sleep = Icons.SLEEP.get();
                sleep.scale.set(.5f);
                sleep.x = align(image.x + (image.width() - sleep.width()) / 2);
                sleep.y = align(image.y - sleep.height() - 1);
                sleep.visible = JourneyRun.resting(hero.heroClass);
                sleep.active = false;
                add(sleep);
                campSleep.add(sleep);
            }
        }

        @Override public void update() {
            Camera view = camera();
            for (MovieClip image : campHeroes)
                image.active = image.x + image.width() >= view.scroll.x && image.x <= view.scroll.x + view.width
                        && image.y + image.height() >= view.scroll.y && image.y <= view.scroll.y + view.height;
            super.update();
        }

        private int cell(int x, int y) { return y * columns + x; }

        private boolean solid(int x, int y) {
            return x >= 0 && x < columns && y >= 0 && y < rows && ground[cell(x, y)];
        }

        private float nodeX(JourneyRoute.Node node) {
            if (horizontal) return 32 + node.layer * LAYER_TILES * TILE;
            float spread = Math.min(280, columns * TILE - 56);
            return ((int)((columns * TILE - spread) / 2 + node.column * spread / 6) / TILE) * TILE + 4;
        }

        float nodeY(JourneyRoute.Node node) {
            if (!horizontal) return 32 + (layers - node.layer) * LAYER_TILES * TILE;
            float spread = Math.min(240, rows * TILE - 80);

            return ((int)((rows * TILE - spread) / 2 + node.column * spread / 6) / TILE) * TILE - 12;
        }

        private ArrayList<Integer> routeCells(JourneyRoute.Node from, JourneyRoute.Node to) {
            ArrayList<Integer> path = new ArrayList<>();
            int x = (int)nodeX(from) / TILE, y = (int)(nodeY(from) + 12) / TILE;
            int tx = (int)nodeX(to) / TILE, ty = (int)(nodeY(to) + 12) / TILE;
            int dx = Math.abs(tx - x), dy = Math.abs(ty - y);
            int sx = x < tx ? 1 : -1, sy = y < ty ? 1 : -1;
            int error = dx - dy;
            while (true) {
                path.add(cell(x, y));
                if (x == tx && y == ty) break;
                int twice = error * 2;
                if (twice > -dy) {
                    error -= dy;
                    x += sx;
                    if (twice < dx) path.add(cell(x, y));
                }
                if (twice < dx) {
                    error += dx;
                    y += sy;
                }
            }
            return path;
        }

        private void terrain(int[] tiles) {
            Tilemap terrain = new Tilemap(Assets.TILES_CAVES_3D, new TextureFilm(Assets.TILES_CAVES_3D, 16, 16)) {
                @Override protected boolean needsRender(int pos) { return data[pos] != DungeonTileSheet.CHASM; }
                @Override public void draw() {
                    camera = RouteMap.this.camera;
                    super.draw();
                }
            };
            terrain.map(tiles, columns);
            terrain.scale.set(TILE / 16f);
            addToBack(terrain);
        }

        void select(float x, float y) {
            for (MovieClip hero : campHeroes) {
                if (!hero.overlapsPoint(x, y)) continue;
                Sample.INSTANCE.play(Assets.SND_CLICK);
                showCamp();
                return;
            }
            for (ForceMarker marker : forces) {
                if (!marker.inside(x, y)) continue;
                Sample.INSTANCE.play(Assets.SND_CLICK);
                JourneyScene.this.add(new WndTitledMessage(ForceMarker.troopImage(marker.troops.get(0), marker.facings.get(0)),
                        Messages.get("journey.forces"), Messages.get(marker.settled
                        ? "journey.forces_settled" : "journey.forces_desc") + "\n\n"
                        + reinforcementText(marker.troops)));
                return;
            }
            for (RouteMarker marker : markers) {
                if (!marker.inside(x, y)) continue;
                if (marker.loot != null && marker.loot.visible && marker.loot.overlapsPoint(x, y)) {
                    Sample.INSTANCE.play(Assets.SND_CLICK);
                    JourneyScene.this.add(new WndInfoItem(JourneyRun.reward(marker.node.id)));
                    return;
                }
                selectedNode = marker.node.id;
                for (RouteMarker other : markers) other.selection.visible = other.node.id == selectedNode;
                Sample.INSTANCE.play(Assets.SND_CLICK);
                showArea();
                break;
            }
        }
    }

    private static class ForceMarker extends Component {
        final int node, previous;
        final ArrayList<JourneyForces.Troop> troops = new ArrayList<>();
        final ArrayList<MovieClip> images = new ArrayList<>();
        final ArrayList<Boolean> facings = new ArrayList<>();
        private int groups;
        private boolean settled;

        ForceMarker(JourneyForces.Group group) {
            node = group.node;
            previous = group.traveling ? group.previous : -1;
        }

        void include(JourneyForces.Group group, boolean facingLeft) {
            groups++;
            settled |= group.settled;
            for (JourneyForces.Troop troop : group.troops) if (!troops.contains(troop)) {
                troops.add(troop);
                facings.add(facingLeft);
            }
        }

        private static MovieClip troopImage(JourneyForces.Troop troop, boolean facingLeft) {
            try {
                CharSprite sprite = troop.spriteClass.newInstance();
                MovieClip image = new MovieClip(sprite.texture);
                image.flipHorizontal = facingLeft;
                image.play(sprite.idleAnimation());
                sprite.destroy();
                return image;
            } catch (ReflectiveOperationException error) {
                throw new IllegalStateException(error);
            }
        }

        void finish(RouteMap map) {
            JourneyRoute.Node origin = JourneyRun.node(node);
            ArrayList<Integer> path = previous < 0 ? null : map.routeCells(JourneyRun.node(previous), origin);
            float left = Float.MAX_VALUE, top = Float.MAX_VALUE, right = 0, bottom = 0;
            for (int i = 0; i < troops.size(); i++) {
                MovieClip image = troopImage(troops.get(i), facings.get(i));
                image.scale.set(.5f);
                int x, y;
                if (path != null) {
                    int cell = path.get(Math.round((path.size() - troops.size()) / 2f) + i);
                    x = cell % map.columns;
                    y = cell / map.columns;
                } else {
                    x = (int)(map.nodeX(origin) - 20) / RouteMap.TILE;
                    y = (int)(map.nodeY(origin) + 12) / RouteMap.TILE + i - troops.size() / 2;
                    for (int floorX = x; floorX <= (int)map.nodeX(origin) / RouteMap.TILE; floorX++)
                        map.ground[map.cell(floorX, y)] = true;
                }
                image.x = align((x + .5f) * RouteMap.TILE - image.width() / 2);
                image.y = align((y + .5f) * RouteMap.TILE - image.height());
                left = Math.min(left, image.x);
                top = Math.min(top, image.y);
                right = Math.max(right, image.x + image.width());
                bottom = Math.max(bottom, image.y + image.height());
                images.add(image);
                if (images.size() == 1) add(image);
                else addToBack(image);
            }
            setRect(left, top, right - left, bottom - top);
            if (groups > 1) {
                BitmapText count = createText(Integer.toString(groups), 6);
                count.hardlight(0xECD095);
                count.measure();
                count.x = right() - count.width();
                count.y = y;
                add(count);
            }
        }

        @Override public void update() {
            Camera view = camera();
            boolean onScreen = right() >= view.scroll.x && x <= view.scroll.x + view.width
                    && bottom() >= view.scroll.y && y <= view.scroll.y + view.height;
            for (MovieClip image : images) image.active = onScreen;
            super.update();
        }

        @Override public boolean inside(float x, float y) {
            for (MovieClip image : images) if (image.overlapsPoint(x, y)) return true;
            return false;
        }
    }

    private static class RouteMarker extends Component {
        final JourneyRoute.Node node;
        final Image selection, image, boss, mark, reward, remains;
        final ItemSprite loot;
        private Emitter sparks;
        private float pulse;

        RouteMarker(JourneyRoute.Node node) {
            this.node = node;
            if (node.gate || node.finish) {
                image = new Image(Assets.JOURNEY_BOSS_ARENAS);
                image.frame(node.biome.ordinal() * 32, 0, 32, 32);
                boss = bossImage(node);
            } else {
                image = areaImage(node);
                boss = null;
            }
            image.alpha(JourneyRun.available(node.id) ? 1 : node.completed ? .6f : .82f);
            add(image);
            if (boss != null) add(boss);
            selection = new Image(Assets.JOURNEY_NODES);
            selection.frame(12 * 32, 0, 32, 32);
            add(selection);
            mark = new Image(Assets.DAILY_CHALLENGES);
            mark.frame(0, 24, 10, 10);
            mark.scale.set(.65f);
            mark.visible = node.completed;
            add(mark);
            boolean subclass = node.gate && node.biome == JourneyRoute.Biome.PRISON;
            if (subclass) reward = new ItemSprite(ItemSpriteSheet.MASTERY, null);
            else if (node.objective == JourneyRoute.Objective.CHECKPOINT)
                reward = new ItemSprite(ItemSpriteSheet.IRON_KEY, null);
            else if (node.objective == JourneyRoute.Objective.OUTPOST)
                reward = new ItemSprite(ItemSpriteSheet.SWORD, null);
            else if (node.objective == JourneyRoute.Objective.RESCUE) {
                reward = new Image(Assets.TROLL);
                reward.frame(0, 0, 13, 16);
            } else reward = new ItemSprite(ItemSpriteSheet.CHEST, null);
            reward.active = false;
            reward.scale.set(.75f);
            reward.visible = (node.objective != JourneyRoute.Objective.NONE || subclass) && !node.completed;
            add(reward);
            Item item = JourneyRun.reward(node.id);
            loot = item == null ? null : new ItemSprite(item);
            if (loot != null) {
                loot.scale.set(.75f);
                loot.visible = loot.active = !node.completed;
                add(loot);
                if (!node.completed) {
                    sparks = new Emitter();
                    sparks.autoKill = false;
                    sparks.pour(Speck.factory(Speck.LIGHT), .4f);
                    add(sparks);
                }
            }
            remains = new ItemSprite(ItemSpriteSheet.BONES, null);
            remains.active = false;
            remains.visible = JourneyRun.hasRemains(node.id);
            add(remains);
        }

        @Override protected void layout() {
            super.layout();
            if (image == null) return;
            image.x = x + (width - image.width()) / 2;
            image.y = y + (height - image.height()) / 2;
            if (boss != null) {
                int feet = node.biome == JourneyRoute.Biome.CAVES ? 30
                        : node.biome == JourneyRoute.Biome.SEWERS || node.biome == JourneyRoute.Biome.CITY ? 28 : 29;
                boss.x = align(image.x + (image.width() - boss.width()) / 2);
                boss.y = align(image.y + feet - boss.height());
            }
            selection.x = x + (width - selection.width()) / 2;
            selection.y = y + (height - selection.height()) / 2;
            mark.x = right() - mark.width() - 1;
            mark.y = y + 1;
            reward.x = right() - reward.width();
            reward.y = bottom() - reward.height();
            if (loot != null) {
                loot.x = right() - loot.width();
                loot.y = (reward.visible ? reward.y - 2 : bottom()) - loot.height();
                if (sparks != null) sparks.pos(loot.x - 2, loot.y - 2, loot.width() + 4, loot.height() + 4);
            }
            remains.x = x;
            remains.y = bottom() - remains.height();
        }

        @Override public void update() {
            Camera view = camera();
            boolean onScreen = right() >= view.scroll.x && x <= view.scroll.x + view.width
                    && bottom() >= view.scroll.y && y <= view.scroll.y + view.height;
            if (boss != null) boss.active = boss instanceof MovieClip && onScreen;
            if (sparks != null) sparks.on = sparks.active = sparks.visible = loot.visible && onScreen;
            super.update();
            if (selection.visible) {
                pulse += Game.elapsed;
                selection.alpha(.8f + .2f * (float)Math.sin(pulse * 3));
            }
        }
    }

    private static class CampButton extends RedButton {
        private final Runnable action;
        private float labelScale;

        CampButton(String title, Image image, Runnable action) {
            super(title);
            this.action = action;
            if (image != null) icon(image);
        }

        @Override protected void createChildren() {
            super.createChildren();
            remove(text);
            text.destroy();
            text = PixelScene.createMultiline(7);
            labelScale = text.scale.x;
            add(text);
        }

        @Override public void icon(Image image) {
            if (icon != null) icon.destroy();
            super.icon(image);
        }

        @Override protected void layout() {
            float available = Math.max(1, width - 8);
            text.scale.set(labelScale);
            ((BitmapTextMultiline)text).maxWidth = (int)available;
            text.measure();
            float textHeight = Math.max(1, height - 6 - (icon == null ? 0 : icon.height() + 3));
            float fit = Math.min(1, Math.min(available / Math.max(1, text.width()), textHeight / Math.max(1, text.height())));
            text.scale.set(labelScale * fit);
            super.layout();
            boolean labelled = text.text() != null && !text.text().isEmpty();
            float gap = icon != null && labelled ? 3 : 0;
            float contentHeight = (labelled ? text.height() : 0) + (icon == null ? 0 : icon.height() + gap);
            text.x = align(x + (width - text.width()) / 2);
            text.y = align(y + (height - contentHeight) / 2 + (icon == null ? 0 : icon.height() + gap));
            if (icon != null) {
                icon.x = align(x + (width - icon.width()) / 2);
                icon.y = align(y + (height - contentHeight) / 2);
            }
        }

        @Override protected void onClick() { action.run(); }
    }
}
