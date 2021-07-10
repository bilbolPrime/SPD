package com.bilboldev.pixeldungeonskills.actors.skills.skilltree;

import com.bilboldev.pixeldungeonskills.actors.skills.Skill;

public class SkillContainer {
    public Skill skill;
    public int x;
    public int y;

    public SkillContainer(Skill skill, int x, int y){
        this.skill = skill;
        this.x = x;
        this.y = y;
    }
}
