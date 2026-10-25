package android.content.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AssetManager {

    public InputStream open(String fileName) throws FileNotFoundException {
        if (Gdx.files != null) {
            FileHandle internal = Gdx.files.internal(fileName);
            if (internal.exists()) {
                return internal.read();
            }
        }

        return new FileInputStream(fileName);
    }
}
