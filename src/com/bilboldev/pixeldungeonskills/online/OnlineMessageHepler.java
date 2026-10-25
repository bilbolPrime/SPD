package com.bilboldev.pixeldungeonskills.online;

import android.util.Log;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Online.OnlineFoe;
import com.bilboldev.pixeldungeonskills.actors.Online.OnlineHero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.LeafParticle;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Online.OnlineDagger;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfBlink;
import com.bilboldev.pixeldungeonskills.plants.Online.OnlineSungrass;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.OnlineScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.LegendSprite;
import com.bilboldev.pixeldungeonskills.sprites.OnlineSprite;
import com.bilboldev.pixeldungeonskills.ui.AttackIndicator;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class OnlineMessageHepler {

    public static String RoomIdentifier;
    public static HashMap<Integer, Plant> Plants = null;
    public static void DecodeMessage(String Message)
    {
        try {
            if(Dungeon.hero == null || !(Dungeon.hero instanceof OnlineHero))
                return;

            JSONObject jsonObject = new JSONObject(Message);
            if(jsonObject.getInt("MessageType") == 1)
            {
                ((Game)Game.mContext).mService.mHubConnection.stop();
                GLog.localN("Disconnected");
            }
            else if(jsonObject.getInt("MessageType") == 0)
            {
                Plants = new HashMap<>();
                GLog.localP(jsonObject.getString("Data"));
            }
            else  if(jsonObject.getInt("MessageType") == 9)
                GLog.localN(jsonObject.getString("Data"));
            else if(jsonObject.getInt("MessageType") == 5)
            {
                JSONObject jsonObject2 = new JSONObject(jsonObject.getString("Data"));
                ((OnlineHero)Dungeon.hero).ApprovedMove(jsonObject2.getInt("pos"));
                if(jsonObject2.getInt("pos") != jsonObject2.getInt("finalStep"))
                    ((OnlineHero)Dungeon.hero).ResumeMove(jsonObject2.getInt("finalStep"));
            }
            else if(jsonObject.getInt("MessageType") == 8)
            {
                JSONObject jsonObject2 = new JSONObject(jsonObject.getString("Data"));
                WandOfBlink.appear(Dungeon.hero, jsonObject2.getInt("Position"));

                ((OnlineHero)Dungeon.hero).Identifier = jsonObject2.getString("Identifier");
            }
            else if(jsonObject.getInt("MessageType") == 10)
            {
                JSONObject jsonObject2 = new JSONObject(jsonObject.getString("Data"));
               String toFind = jsonObject2.getString("Identifier");

                if(toFind.equals(((OnlineHero)Dungeon.hero).Identifier) == false) {
                    boolean found = false;
                    for (Mob mob : Dungeon.level.mobs) {
                        if (mob instanceof OnlineFoe) {
                            OnlineFoe onlineFoe = (OnlineFoe) mob;
                            if (onlineFoe.Identifier.equals(toFind) == true) {
                                onlineFoe.ApprovedMove(Integer.parseInt(jsonObject2.getString("Position")));
                                found = true;
                                break;
                            }
                        }
                    }

                    if (found == false)
                        GLog.localN("Could not find player, out of sync");
                }
            }
            else if(jsonObject.getInt("MessageType") == 11)
            {
                JSONObject jsonObject2 = new JSONObject(jsonObject.getString("Data"));
                String toFind = jsonObject2.getString("Identifier");

                if(toFind.equals(((OnlineHero)Dungeon.hero).Identifier) == false) {
                    boolean found = false;
                    for (Mob mob : Dungeon.level.mobs) {
                        if (mob instanceof OnlineFoe) {
                            OnlineFoe onlineFoe = (OnlineFoe) mob;
                            if (onlineFoe.Identifier.equals(toFind) == true) {
                                onlineFoe.DieWithReason(jsonObject2.getString("Reason"));
                                found = true;
                                break;
                            }
                        }
                    }

                    if (found == false)
                        GLog.localN("Could not find player, out of sync");
                }
            }
            else if(jsonObject.getInt("MessageType") == 13)
            {
                String toFind = jsonObject.getString("Data");

                if(toFind.equals(((OnlineHero)Dungeon.hero).Identifier) == false) {
                    boolean found = false;
                    for (Mob mob : Dungeon.level.mobs) {
                        if (mob instanceof OnlineFoe) {
                            OnlineFoe onlineFoe = (OnlineFoe) mob;
                            if (onlineFoe.Identifier.equals(toFind) == true) {
                                ((OnlineHero)Dungeon.hero).AttackApproved(onlineFoe);
                                found = true;
                                break;
                            }
                        }
                    }

                    if (found == false)
                        GLog.localN("Could not find player, out of sync");
                }
            }
            else if(jsonObject.getInt("MessageType") == 7)
            {
                JSONObject jsonObject2 = new JSONObject(jsonObject.getString("Data"));
                String toFind = jsonObject2.getString("Identifier");
                String source = jsonObject2.getString("Source");

                CharSprite sourceSprite = null;
                int targetPos = -1;

                if(toFind.equals(((OnlineHero)Dungeon.hero).Identifier) == true)
                {
                    targetPos =  Dungeon.hero.pos;
                }

                if(toFind.equals(((OnlineHero)Dungeon.hero).Identifier) == false) {
                    boolean found = false;
                    for (Mob mob : Dungeon.level.mobs) {
                        if (mob instanceof OnlineFoe) {
                            OnlineFoe onlineFoe = (OnlineFoe) mob;
                            if (onlineFoe.Identifier.equals(toFind) == true) {
                                (onlineFoe.sprite).bloodBurstA( (onlineFoe.sprite).center(), Integer.parseInt((jsonObject2.getString("Damage"))) );
                                onlineFoe.HP = Integer.parseInt((jsonObject2.getString("newHP")));
                                onlineFoe.sprite.showStatus(
                                                CharSprite.NEGATIVE,
                                        jsonObject2.getString("Damage"));
                                targetPos = onlineFoe.pos;
                                (onlineFoe.sprite).flash();
                                Sample.INSTANCE.play(Assets.SND_HIT);
                                if(jsonObject2.getBoolean("Kill") == true)
                                {
                                    onlineFoe.DieWithReason(jsonObject2.getString("Extra"));
                                    GLog.localI( "%s was %s",
                                            com.bilboldev.pixeldungeonskills.messages.Messages.maybeTranslate( onlineFoe.Tag ),
                                            com.bilboldev.pixeldungeonskills.messages.Messages.maybeTranslate( jsonObject2.getString("Extra") ) );
                                }
                                else
                                {
                                    AttackIndicator.target(onlineFoe);
                                }

                                found = true;
                            }

                            if (onlineFoe.Identifier.equals(source) == true) {
                                sourceSprite = onlineFoe.sprite;
                            }

                            if(sourceSprite != null && targetPos != -1)
                                break;
                        }
                    }

                    if (found == false)
                        GLog.localN("Could not find player, out of sync");
                }
                else
                {
                    Dungeon.hero.sprite.bloodBurstA( (((OnlineHero) Dungeon.hero).sprite).center(), Integer.parseInt((jsonObject2.getString("Damage")) ));
                    Dungeon.hero.sprite.flash();
                    Dungeon.hero.sprite.showStatus(
                        CharSprite.NEGATIVE,
                        jsonObject2.getString("Damage"));

                    Dungeon.hero.HP =  Integer.parseInt((jsonObject2.getString("newHP")));
                    Sample.INSTANCE.play(Assets.SND_HIT);
                    if(jsonObject2.getBoolean("Kill") == true)
                    {
                        GLog.localN(jsonObject2.getString("Extra"));
                        Dungeon.hero.sprite.showStatus(
                                CharSprite.NEGATIVE,
                                jsonObject2.getString("Extra"));
                        Dungeon.hero.die(null);
                        ((Game)Game.mContext).mService.mHubConnection.stop();
                    }

                    for (Mob mob : Dungeon.level.mobs) {
                        if (mob instanceof OnlineFoe) {
                            OnlineFoe onlineFoe = (OnlineFoe) mob;
                            if (onlineFoe.Identifier.equals(source) == true) {

                                sourceSprite = onlineFoe.sprite;
                                break;
                            }
                        }
                    }
                }

                if(sourceSprite != null && targetPos != -1)
                {
                    sourceSprite.attack(targetPos);
                }
            }

            else if(jsonObject.getInt("MessageType") == 14)
            {
                JSONObject jsonObject2 = new JSONObject(jsonObject.getString("Data"));
                String toFind = jsonObject2.getString("Identifier");
                String message = jsonObject2.getString("Message");
                int HPChange = jsonObject2.getInt("HPChange");
                boolean Heal = jsonObject2.getBoolean("Heal");
                String effect = jsonObject2.getString("Effect");

                CharSprite sourceSprite = null;
                int targetPos = -1;

                if(toFind.equals(((OnlineHero)Dungeon.hero).Identifier) == true)
                {

                    sourceSprite =  ((OnlineHero) Dungeon.hero).sprite;
                    if(HPChange > 0)
                        Dungeon.hero.HT =  Dungeon.hero.HT + HPChange;

                    if(Heal == true)
                    {
                        Dungeon.hero.HP = Dungeon.hero.HT;
                    }
                }

                if(toFind.equals(((OnlineHero)Dungeon.hero).Identifier) == false) {

                    for (Mob mob : Dungeon.level.mobs) {
                        if (mob instanceof OnlineFoe) {
                            OnlineFoe onlineFoe = (OnlineFoe) mob;
                            if (onlineFoe.Identifier.equals(toFind) == true) {
                                if(HPChange > 0)
                                    onlineFoe.HT = onlineFoe.HT +  HPChange;
                                if(Heal == true)
                                {
                                    onlineFoe.HP = onlineFoe.HT;
                                }
                                sourceSprite = onlineFoe.sprite;
                                break;
                            }

                        }
                    }

                    if (sourceSprite == null)
                        GLog.localN("Could not find player, out of sync");
                }

                if(sourceSprite != null)
                {
                    sourceSprite.showStatus(CharSprite.POSITIVE, message);
                    if(effect.equals("Heal"))
                    {
                        sourceSprite.emitter().burst( Speck.factory(Speck.HEALING), 1 );
                    }
                }
            }
            else if(jsonObject.getInt("MessageType") == 2)
            {
                ArrayList<PlayerInfo> MissingPlayers = new ArrayList<>();
                JSONObject roomData = new JSONObject(jsonObject.getString("Data"));
                RoomIdentifier = roomData.getString("Identifier");
                JSONArray Players = roomData.getJSONArray("Players");

                JSONArray PlantsA = roomData.getJSONArray("Plants");
                if(Plants == null)
                    Plants = new HashMap<>();

                for (int i = 0; i < PlantsA.length(); i++) {

                    JSONObject plantData = PlantsA.getJSONObject(i);
                    String plantType = plantData.getString("Tag");
                    int pos = plantData.getInt("Position");

                    if(Plants.containsKey(pos) == false) {
                        Plant toAdd = new OnlineSungrass();
                        toAdd.pos = pos;
                        GameScene.add(toAdd);
                        Plants.put(pos, toAdd);
                    }
                }

                for (int i = 0; i < Players.length(); i++) {

                    JSONObject obj = Players.getJSONObject(i);

                    PlayerInfo player = new PlayerInfo(obj.getString("Identifier"), obj.getInt("Position"), obj.getInt("HP"), obj.getInt("MaxHP"), obj.getString("Tag"), obj.getBoolean("isAI"));

                    if(player.Identifier.equals(((OnlineHero)Dungeon.hero).Identifier) == true)
                        continue;

                    boolean found = false;
                    for (Mob mob : Dungeon.level.mobs) {
                        if (mob instanceof OnlineFoe) {
                            OnlineFoe onlineFoe = (OnlineFoe)mob;
                            if(onlineFoe.Identifier.equals(player.Identifier) == true)
                            {
                                onlineFoe.HP = player.HP;
                                onlineFoe.isAI = obj.getBoolean("isAI");
                                ((OnlineSprite)onlineFoe.sprite).text = obj.getString("Tag");

                                if(onlineFoe.pos != player.Pos)
                                    WandOfBlink.appear(onlineFoe, player.Pos);

                                found = true;
                                break;
                            }
                        }
                    }
                    if(found == false)
                        MissingPlayers.add(player);
                }

                for (PlayerInfo missingPlayer : MissingPlayers) {
                     OnlineFoe onlineFoe = new OnlineFoe();
                     onlineFoe.pos = missingPlayer.Pos;
                     onlineFoe.HP = missingPlayer.HP;
                     onlineFoe.HT = missingPlayer.MaxHP;
                     onlineFoe.Identifier = missingPlayer.Identifier;
                    onlineFoe.isAI = missingPlayer.isAI;
                    onlineFoe.Tag = missingPlayer.Tag;
                    OnlineScene.add(onlineFoe);
                    ((OnlineSprite)onlineFoe.sprite).text = missingPlayer.Tag;
                    if(onlineFoe.isAI == false)
                        ((OnlineSprite)onlineFoe.sprite).color = CharSprite.WARNING;
                    else
                    {
                        switch(missingPlayer.Tag)
                        {
                            case "Rat": ((OnlineSprite)onlineFoe.sprite).LoadRat();
                                break;
                            case "Crab": ((OnlineSprite)onlineFoe.sprite).LoadCrab();
                                break;
                            case "Gnoll": ((OnlineSprite)onlineFoe.sprite).LoadGnoll();
                                break;
                            case "Goo": ((OnlineSprite)onlineFoe.sprite).LoadGoo();
                                break;

                            default:((OnlineSprite)onlineFoe.sprite).LoadRat();
                        }

                    }
                   ((OnlineSprite)onlineFoe.sprite).resetData();
                     WandOfBlink.appear( onlineFoe, onlineFoe.pos );
                }
            }
            else if(jsonObject.getInt("MessageType") == 15)
            {
                if(Plants == null)
                    Plants = new HashMap<>();

                JSONObject plantData = new JSONObject(jsonObject.getString("Data"));
                String plantType = plantData.getString("PlantType");
                int pos = plantData.getInt("Pos");
                boolean activate = plantData.getBoolean("Activate");
                if(activate == false) {
                    Plant toAdd = new OnlineSungrass();
                    toAdd.pos = pos;
                    GameScene.add(toAdd);
                    Plants.put(pos, toAdd);
                }
                else
                {
                    if(Plants.containsKey(pos))
                        Plants.get(pos).sprite.kill();
                    CellEmitter.get(pos).burst( LeafParticle.GENERAL, 6 );
                }
            }
            else if(jsonObject.getInt("MessageType") == 16)
            {
                String toFind = jsonObject.getString("Data");

                if(toFind.equals(((OnlineHero)Dungeon.hero).Identifier) == true)
                {
                    ((LegendSprite)((OnlineHero) Dungeon.hero).sprite).showOnlineTurn();
                }
                else
                {
                    ((LegendSprite)((OnlineHero) Dungeon.hero).sprite).hideOnlineTurn();
                }

                    for (Mob mob : Dungeon.level.mobs) {
                        if (mob instanceof OnlineFoe) {
                            OnlineFoe onlineFoe = (OnlineFoe) mob;
                            if(onlineFoe.isAI == false ) {
                                if (onlineFoe.Identifier.equals(toFind) == true){
                                    ((OnlineSprite)onlineFoe.sprite).showOnlineTurn();
                                }
                                else
                                    ((OnlineSprite)onlineFoe.sprite).hideOnlineTurn();
                            }

                        }
                    }
            }
            else if(jsonObject.getInt("MessageType") == 17)
            {
                JSONObject itemData = new JSONObject(jsonObject.getString("Data"));
                Heap heap = Dungeon.level.heaps.get( itemData.getInt("Pos") );
                if(heap == null) {
                    heap = new Heap();

                    heap.pos = itemData.getInt("Pos");
                    heap.drop(new OnlineDagger());
                    Dungeon.level.heaps.put( itemData.getInt("Pos"), heap );
                    GameScene.add( heap );
                }
                else
                {
                    heap.drop(new OnlineDagger());
                }
                heap.sprite.drop(itemData.getInt("Pos"));
            }
        }
        catch (Exception e)
        {
            Log.e("","ERROR" +  e.getMessage());
        }
    }

    public static void RequestMove(int pos, int finalStep)
    {
        try {
            JSONObject json = new JSONObject();
            json.put("MessageType", 4);
            JSONObject data = new JSONObject();
            data.put("pos", pos);
            data.put("finalStep", finalStep);
            json.put("data", data.toString());
            OnlineSync.GetSingleton().SendMessage(json.toString());
        }
        catch (Exception ex)
        {

        }
    }

    public static void RequestAttack(String Target) {
        try {
            JSONObject json = new JSONObject();
            json.put("MessageType", 12);
            json.put("Data", Target);
            OnlineSync.GetSingleton().SendMessage(json.toString());
        } catch (Exception ex) {

        }
    }

    public static class PlayerInfo
    {
        public String Identifier;
        public int Pos;
        public int HP;
        public int MaxHP;
        public String Tag;
        public boolean isAI;
        public PlayerInfo(String id, int pos, int hp, int maxHP, String Tagg, boolean isAII)
        {
            Identifier = id;
            Pos = pos;
            HP = hp;
            MaxHP = maxHP;
            Tag = Tagg;
            isAI = isAII;
        }
    }
}
