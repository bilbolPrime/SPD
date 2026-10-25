-include ../proguard-game.pro

-dontskipnonpubliclibraryclassmembers
-keepnames class com.jcraft.jorbis.PcmVorbisFile

-dontwarn android.content.Intent

-keep class com.bilboldev.pixeldungeonskills.desktop.DesktopLauncher {
    public static void main(java.lang.String[]);
}
