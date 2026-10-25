-keepnames class com.bilboldev.pixeldungeonskills.**

-keep,allowoptimization class * implements com.bilboldev.utils.Bundlable {
    public <init>();
}

-keep,allowoptimization,allowobfuscation class * extends com.bilboldev.noosa.Gizmo {
    public <init>();
}
-keep,allowoptimization,allowobfuscation class * extends com.bilboldev.glscripts.Script {
    public <init>();
}

-keepclassmembers enum com.bilboldev.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static final ** *;
}

-keepclassmembers class com.bilboldev.pixeldungeonskills.levels.painters.** {
    public static void paint(com.bilboldev.pixeldungeonskills.levels.Level,com.bilboldev.pixeldungeonskills.levels.Room);
}

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod,Exceptions,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-repackageclasses com.bilboldev.internal
