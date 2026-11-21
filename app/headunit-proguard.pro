# Preserve the native host's public/instrumentation surface while optimizing its instructions.
-keep,allowoptimization class !**.R,!**.R$*,com.shazwan.idash3.** { *; }
-keep class com.music.bitchord.BitChordEmbedded { public *; }
-keep class com.music.bitchord.IdashMediaBridge { public *; }
# Stable media API boundary used by the native host's runtime checks.
-keep class androidx.media3.session.MediaController { public *; }
-keep class androidx.media3.session.MediaController$Builder { public *; }
-keep class androidx.media3.common.BasePlayer { public *; }
-keep interface androidx.media3.common.Player { *; }
-keep class androidx.media3.common.MediaItem$Builder { public *; }
-keep class androidx.media3.common.MediaMetadata$Builder { public *; }
