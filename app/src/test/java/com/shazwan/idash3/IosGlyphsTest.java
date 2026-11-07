package com.shazwan.idash3;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/** Launchable apps actually found on the owner's Proton Saga head unit (APK dump, 2026-11-05). */
public class IosGlyphsTest {
    private static void is(int expected, String pkg, String label) { assertEquals(pkg, expected, IosGlyphs.match(pkg, label)); }

    @Test public void headUnitApps() {
        is(IosGlyphs.MUSIC, "com.acloud.stub.localmusic", "localmusic");
        is(IosGlyphs.RADIO, "com.acloud.stub.localradio", "Radio");
        is(IosGlyphs.PHOTOS, "com.acloud.stub.logoselector", "LogoSelector");
        is(IosGlyphs.CALENDAR, "com.android.calendar", "Calendar");
        is(IosGlyphs.PHOTOS, "com.android.gallery3d", "Gallery");
        is(IosGlyphs.REVERSE, "com.autochips.android.backcar", "Backcar");
        is(IosGlyphs.BLUETOOTH, "com.autochips.bluetooth", "Bluetooth");
        is(IosGlyphs.TOOLS, "com.ext.ui", "Apk Extractor");
        is(IosGlyphs.MAPS, "com.google.android.apps.maps", "Maps");
        is(IosGlyphs.SETTINGS, "com.google.android.gms", "Google Play services");
        is(IosGlyphs.KEYBOARD, "com.google.android.inputmethod.latin", "Gboard");
        is(IosGlyphs.CANBUS, "com.kyhero.car.myhost2", "CanBus2");
        is(IosGlyphs.CANBUS, "com.kyhero.car.myhost", "CarSet");
        is(IosGlyphs.FILES, "com.mediatek.filemanager", "File Manager");
        is(IosGlyphs.DOWNLOAD, "com.pve.download", "Downloads");
        is(IosGlyphs.MAPS, "com.pve.naviguide", "Net Navi");
        is(IosGlyphs.SOUND, "com.xy.tda7729", "Sound Effects");
        is(IosGlyphs.CAR, "com.xyauto.Settings", "Car Settings");
        is(IosGlyphs.BROWSER, "com.android.chrome", "Chrome");
        is(IosGlyphs.MIC, "com.android.soundrecorder", "Sound Recorder");
        is(IosGlyphs.PHOTOS, "com.pve.theme", "Theme");
        is(IosGlyphs.TOOLS, "com.termux", "Termux");
        is(IosGlyphs.MAIL, "com.google.android.gm", "Gmail");
        is(IosGlyphs.VIDEO, "com.acloud.stub.video", "video");
        is(IosGlyphs.VIDEO, "org.videolan.vlc", "VLC");
        is(IosGlyphs.LINK, "com.xy.zlinkactivationcode", "ZlinkActivationCode");
        is(IosGlyphs.YOUTUBE, "com.shazwan.nexadash.tizentube.lab", "TizenTube Native Lab");
        is(IosGlyphs.NONE, "com.shazwan.nexadash.lab", "NexaDash Native Lab");
    }

    @Test public void bluetoothToolIsNotBluetoothApp() { assertEquals(IosGlyphs.TOOLS, IosGlyphs.match("com.mediatek.bluetooth.dtt", "BT Tool")); }
}
