package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;

/**
 * Created by Moussa on 03-Mar-18.
 */
public class WeaponEffect {
    public String Prefix = "";
    public String Name = "";
    public String Description = "";

    public float Effect(Char source, Char target)
    {
        return 1f;
    }

    public void YellText(Char source)
    {
        source.sprite.showStatus(CharSprite.NEUTRAL, Name);
    }
}
