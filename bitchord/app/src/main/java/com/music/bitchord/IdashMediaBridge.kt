package com.music.bitchord

/** Process-local token for the host's own player; no notification permission is needed. */
object IdashMediaBridge {
    const val CHANGED = "com.shazwan.idash3.NATIVE_MEDIA_CHANGED"
    private var token: android.media.session.MediaSession.Token? = null
    @JvmStatic fun controller(context: android.content.Context): android.media.session.MediaController? =
        token?.let { android.media.session.MediaController(context, it) }
    @JvmStatic fun owns(controller: android.media.session.MediaController?): Boolean =
        token != null && token == controller?.sessionToken
    fun publish(context: android.content.Context, value: android.media.session.MediaSession.Token?) {
        token = value
        context.sendBroadcast(android.content.Intent(CHANGED).setPackage(context.packageName))
    }
}
