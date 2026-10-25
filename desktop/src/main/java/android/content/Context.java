package android.content;

import android.content.res.AssetManager;
import android.content.res.Resources;

public class Context {

    private final Resources resources = new Resources();

    public Resources getResources() {
        return resources;
    }

    public AssetManager getAssets() {
        return resources.getAssets();
    }
}
