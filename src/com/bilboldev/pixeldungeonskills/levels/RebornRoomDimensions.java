package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.utils.Random;

enum RebornRoomDimensions {
    SEWERS(11, 13, 1.3f, 1.5f),
    PRISON(13, 14, 1.5f, 1.5f),
    CAVES(10, 12, 1.4f, 0.5f),
    CITY(9, 11, 1.1f, 3f),
    HALLS(10, 12, 2f, 2f);

    final int minRooms, maxRooms;
    private final float aspect, shapePreference;

    RebornRoomDimensions(int minRooms, int maxRooms, float aspect, float shapePreference) {
        this.minRooms = minRooms;
        this.maxRooms = maxRooms;
        this.aspect = aspect;
        this.shapePreference = shapePreference;
    }

    static RebornRoomDimensions forLevel(RegularLevel level) {
        if (level instanceof SewerLevel) return SEWERS;
        if (level instanceof PrisonLevel) return PRISON;
        if (level instanceof CavesLevel) return CAVES;
        if (level instanceof CityLevel) return CITY;
        return HALLS;
    }

    float cutWeight(int length, int other, int at) {
        float balance = Math.min(at, length-at) / (float)Math.max(at, length-at);

        float scale = this == CAVES ? 2f-balance : 0.5f+balance;
        return 0.05f + scale * shapeWeight(at, other) * shapeWeight(length-at, other);
    }

    private float shapeWeight(int width, int height) {
        float difference = Math.max(width, height) / (float)Math.min(width, height) - aspect;
        return 1f / (1f + shapePreference * difference * difference);
    }

    void fit(Room room) {

        room.left++;
        room.right--;
        room.top++;
        room.bottom--;
        int width = room.width()-1, height = room.height()-1;
        boolean horizontal = width > height || (width == height && Random.Int(2) == 0);
        int longer = Math.max(width, height), shorter = Math.min(width, height);
        switch (this) {
            case SEWERS:
                if (Random.Int(4) == 0) {
                    longer = Math.min(longer, 11);
                    shorter = Math.min(shorter, 8);
                } else {
                    longer = Math.min(longer, 8);
                    shorter = Math.min(shorter, Random.IntRange(5, 6));
                }
                break;
            case PRISON:
                shorter = Math.min(shorter, Random.IntRange(4, 5));
                longer = Math.min(longer, Random.IntRange(6, 7));
                break;
            case CAVES:
                if (Random.Int(3) == 0) {
                    shorter = Math.min(shorter, Random.IntRange(4, 6));
                    longer = Math.min(longer, Random.IntRange(6, 8));
                } else {
                    shorter = Math.min(shorter, 10);
                    longer = Math.min(longer, 13);
                }
                break;
            case CITY:
                shorter = Math.min(shorter, 11);
                longer = Math.min(longer, shorter+2);
                break;
            case HALLS:
                if (Random.Int(4) == 0) {
                    shorter = Math.min(shorter, 10);
                    longer = Math.min(longer, shorter+2);
                } else {
                    shorter = Math.min(shorter, Random.IntRange(4, 5));
                    longer = Math.min(longer, 14);
                }
                break;
        }
        int wantedWidth = horizontal ? longer : shorter;
        int wantedHeight = horizontal ? shorter : longer;
        room.left += Random.IntRange(0, width-wantedWidth);
        room.top += Random.IntRange(0, height-wantedHeight);
        room.right = room.left+wantedWidth+1;
        room.bottom = room.top+wantedHeight+1;
    }
}
