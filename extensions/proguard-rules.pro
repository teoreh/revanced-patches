-dontobfuscate
-dontoptimize
-keepattributes *
-keep class app.revanced.** {
  *;
}
-keep class com.google.** {
  *;
}
-keepclassmembers class app.revanced.extension.redditisfun.oauth.OAuthTokenManager {
    public static java.lang.String getCustomClientId();
}