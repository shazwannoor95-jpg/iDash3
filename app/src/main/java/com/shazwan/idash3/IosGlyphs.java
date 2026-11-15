package com.shazwan.idash3;

import android.graphics.*;
import java.util.Calendar;
import java.util.Locale;

/**
 * iOS-style icon theme drawn from scratch (no Apple artwork): a gradient squircle tile plus a white
 * glyph, chosen from the app's package name / label. Covers the apps found on the Proton Saga head
 * unit (Autochips/XY/KyHero car apps, MediaTek tools) and common Android apps.
 */
final class IosGlyphs {
    static final int NONE = -1, BLUETOOTH = 0, PHONE = 1, MUSIC = 2, RADIO = 3, VIDEO = 4, PHOTOS = 5, FILES = 6, DOWNLOAD = 7,
        SETTINGS = 8, CAR = 9, CAMERA = 10, CALENDAR = 11, CLOCK = 12, BROWSER = 13, MAPS = 14, KEYBOARD = 15, CALCULATOR = 16,
        TOOLS = 17, MESSAGES = 18, CONTACTS = 19, WEATHER = 20, SOUND = 21, YOUTUBE = 22, STORE = 23, MAIL = 24, REVERSE = 25, CANBUS = 26, LINK = 27, MIC = 28;
    // top, bottom gradient per glyph (iOS-like system colours)
    private static final int[][] TILE = {
        {0xff3d9bff, 0xff0a5fe0}, {0xff6ff07a, 0xff22b83a}, {0xffff6a86, 0xfff4213f}, {0xffffa14a, 0xfff2611d}, {0xff5b6676, 0xff222831},
        {0xffffffff, 0xffeef1f6}, {0xff4fb2ff, 0xff1a73e8}, {0xff5fd0ff, 0xff1f8fe6}, {0xffa9b0bb, 0xff6d7480}, {0xff5a6b85, 0xff27334a},
        {0xffb9bfc8, 0xff7d848f}, {0xffffffff, 0xfff4f4f6}, {0xff2b2f36, 0xff0d0f12}, {0xff59c8ff, 0xff1572e8}, {0xff7fe08a, 0xff2fae60},
        {0xff8d97a8, 0xff515b6b}, {0xffffa53a, 0xffef6c0c}, {0xff8d97a8, 0xff4c5566}, {0xff6ff07a, 0xff22b83a}, {0xffc7ccd4, 0xff8e95a1},
        {0xff59b8ff, 0xff2d6ff0}, {0xffff6d5e, 0xffe8304a}, {0xffff4a4a, 0xffd40f1e}, {0xff4fa8ff, 0xff1468e6}, {0xff59b8ff, 0xff1a73e8},
        {0xff5a6b85, 0xff1f2a3d}, {0xff38d9c8, 0xff0f9b8e}, {0xff8f7dff, 0xff5b3fe8}, {0xffff6d5e, 0xffe8304a}};

    private IosGlyphs() {}

    static int match(String pkg, String label) {
        String p = pkg == null ? "" : pkg.toLowerCase(Locale.ROOT), l = label == null ? "" : label.toLowerCase(Locale.ROOT), k = p + " " + l;
        if (k.contains("backcar") || k.contains("avin") || k.contains("reverse") || k.contains("rearview")) return REVERSE;
        if (k.contains("canbus") || k.contains("myhost") || k.contains("canset")) return CANBUS;
        if (k.contains("recorder") || k.contains("voice")) return MIC;
        if (k.contains("termux") || k.contains("terminal")) return TOOLS;
        if (k.contains(".theme") || k.contains("wallpaper")) return PHOTOS;
        if (k.contains("bluetooth") && !k.contains("tool")) return BLUETOOTH;
        if (k.contains("dialer") || k.contains("phone") || k.contains("btphone") || l.equals("call")) return PHONE;
        if (k.contains("youtube") || k.contains("smarttube") || k.contains("tizentube") || k.contains("tubemate")) return YOUTUBE;
        if (k.contains("music") || k.contains("audio") || k.contains("spotify") || k.contains("joox") || k.contains("player.music")) return MUSIC;
        if (k.contains("radio") || k.contains(".fm") || k.contains("fmradio") || k.contains("tuner")) return RADIO;
        if (k.contains("video") || k.contains("movie") || k.contains("mxtech")) return VIDEO;
        if (k.contains("gallery") || k.contains("photo") || k.contains("album") || k.contains("logoselector")) return PHOTOS;
        if (k.contains("download")) return DOWNLOAD;
        if (k.contains("file") || k.contains("documentsui") || k.contains("explorer")) return FILES;
        if (k.contains("calendar")) return CALENDAR;
        if (k.contains("deskclock") || l.equals("clock") || k.contains("alarm")) return CLOCK;
        if (k.contains("calculator")) return CALCULATOR;
        if (k.contains("camera")) return CAMERA;
        if (k.contains("chrome") || k.contains("browser") || k.contains("firefox") || k.contains("webview")) return BROWSER;
        if (k.contains("maps") || k.contains("navi") || k.contains("waze") || k.contains("gps") || k.contains("sygic")) return MAPS;
        if (k.contains("inputmethod") || k.contains("keyboard") || k.contains("gboard")) return KEYBOARD;
        if (k.contains("mms") || k.contains("messag") || k.contains("sms") || k.contains("whatsapp") || k.contains("telegram")) return MESSAGES;
        if (k.contains("contact")) return CONTACTS;
        if (k.contains("weather")) return WEATHER;
        if (k.contains("equalizer") || k.contains("sound") || k.contains("tda77") || k.contains("dsp")) return SOUND;
        if (k.contains("vending") || k.contains("store") || k.contains("market")) return STORE;
        if (k.contains("mail") || p.equals("com.google.android.gm")) return MAIL;
        if (k.contains("carlink") || k.contains("carplay") || k.contains("android.auto") || k.contains("zlink") || k.contains("easyconnect") || k.contains("mirror")) return LINK;
        if (k.contains("car") && (k.contains("setting") || k.contains("set"))) return CAR;
        if (k.contains("setting") || k.contains(".gms")) return SETTINGS;
        if (k.contains("extractor") || k.contains("autotest") || k.contains("tool") || k.contains("engineer")) return TOOLS;
        return NONE;
    }

    static int top(int g) { return TILE[g][0]; }
    static int bottom(int g) { return TILE[g][1]; }
    /** White tiles (Photos, Calendar) use dark/colour ink instead of white. */
    static boolean lightTile(int g) { return g == PHOTOS || g == CALENDAR; }

    /** Draws the glyph for g inside box (64-unit design grid). ink is used for monochrome glyphs. */
    static void draw(Canvas c, RectF box, int g, int ink, boolean monochrome) {
        c.save(); c.translate(box.left, box.top); c.scale(box.width() / 64f, box.height() / 64f);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(ink); Path path = new Path();
        switch (g) {
            case BLUETOOTH: stroke(p, 4.2f); path.moveTo(22, 23); path.lineTo(42, 41); path.lineTo(32, 50); path.lineTo(32, 14); path.lineTo(42, 23); path.lineTo(22, 41); c.drawPath(path, p); break;
            case PHONE: fill(p); path.moveTo(23, 14); path.cubicTo(26, 13, 28, 15, 30, 21); path.cubicTo(31, 25, 28, 26, 27, 28); path.cubicTo(29, 33, 32, 36, 36, 38); path.cubicTo(38, 36, 40, 34, 44, 35); path.cubicTo(49, 37, 51, 39, 50, 42); path.cubicTo(49, 47, 45, 50, 40, 49); path.cubicTo(28, 47, 17, 36, 15, 24); path.cubicTo(14, 19, 18, 15, 23, 14); path.close(); c.drawPath(path, p); break;
            case MUSIC: fill(p); c.drawCircle(22, 44, 7, p); c.drawCircle(42, 40, 7, p); c.drawRect(26, 16, 29.5f, 44, p); c.drawRect(46, 12, 49.5f, 40, p); path.moveTo(26, 16); path.lineTo(49.5f, 11); path.lineTo(49.5f, 18); path.lineTo(26, 23); path.close(); c.drawPath(path, p); break;
            case RADIO: fill(p); c.drawRoundRect(12, 24, 52, 50, 6, 6, p); stroke(p, 2.6f); c.drawLine(18, 24, 42, 13, p); fill(p); p.setColor(monochrome ? 0x00000000 : 0xfff2611d); if (!monochrome) { c.drawCircle(40, 37, 7, p); p.setColor(0xfff2611d); c.drawRect(17, 31, 30, 33.5f, p); c.drawRect(17, 37, 30, 39.5f, p); c.drawRect(17, 43, 26, 45.5f, p); } break;
            case VIDEO: fill(p); c.drawRoundRect(11, 18, 41, 46, 6, 6, p); path.moveTo(43, 27); path.lineTo(53, 21); path.lineTo(53, 43); path.lineTo(43, 37); path.close(); c.drawPath(path, p); break;
            case PHOTOS: fill(p); if (!monochrome) p.setColor(0xffffb020); c.drawCircle(24, 22, 6, p); if (!monochrome) p.setColor(0xff34c759); path.moveTo(8, 50); path.lineTo(26, 30); path.lineTo(36, 41); path.lineTo(42, 34); path.lineTo(56, 50); path.close(); c.drawPath(path, p); if (!monochrome) { p.setColor(0xff0a84ff); Path w = new Path(); w.moveTo(8, 50); w.lineTo(56, 50); w.lineTo(56, 54); w.lineTo(8, 54); w.close(); c.drawPath(w, p); } break;
            case FILES: fill(p); p.setAlpha(170); c.drawRoundRect(12, 18, 32, 30, 3, 3, p); p.setAlpha(255); c.drawRoundRect(12, 24, 52, 47, 4, 4, p); break;
            case DOWNLOAD: stroke(p, 4.4f); c.drawLine(32, 14, 32, 38, p); path.moveTo(22, 29); path.lineTo(32, 39); path.lineTo(42, 29); c.drawPath(path, p); c.drawLine(18, 48, 46, 48, p); break;
            case SETTINGS: fill(p); for (int i = 0; i < 8; i++) { c.save(); c.rotate(i * 45f, 32, 32); c.drawRoundRect(29, 12, 35, 20, 1.5f, 1.5f, p); c.restore(); } stroke(p, 5.5f); c.drawCircle(32, 32, 11.5f, p); fill(p); c.drawCircle(32, 32, 3.2f, p); break;
            case CAR: case CANBUS: fill(p); path.moveTo(14, 38); path.lineTo(18, 26); path.cubicTo(19, 23, 21, 22, 24, 22); path.lineTo(40, 22); path.cubicTo(43, 22, 45, 23, 46, 26); path.lineTo(50, 38); path.lineTo(50, 46); path.lineTo(14, 46); path.close(); c.drawPath(path, p); c.drawRoundRect(16, 44, 23, 50, 2, 2, p); c.drawRoundRect(41, 44, 48, 50, 2, 2, p);
                p.setColor(monochrome ? 0x55000000 : (g == CANBUS ? 0xff0f9b8e : 0xff27334a)); c.drawRoundRect(21, 26, 43, 33, 2, 2, p); c.drawCircle(20, 40, 2.6f, p); c.drawCircle(44, 40, 2.6f, p);
                if (g == CANBUS) { p.setColor(ink); stroke(p, 2.4f); c.drawLine(32, 10, 32, 17, p); c.drawCircle(32, 9, 2, p); } break;
            case REVERSE: fill(p); c.drawRoundRect(12, 22, 44, 46, 6, 6, p); path.moveTo(46, 30); path.lineTo(54, 25); path.lineTo(54, 43); path.lineTo(46, 38); path.close(); c.drawPath(path, p); p.setColor(monochrome ? 0x55000000 : 0xff1f2a3d); stroke(p, 3f); c.drawArc(19, 27, 37, 45, 200, 140, false, p); break;
            case CAMERA: fill(p); c.drawRoundRect(10, 22, 54, 48, 7, 7, p); c.drawRoundRect(24, 17, 40, 24, 3, 3, p); p.setColor(monochrome ? 0x55000000 : 0xff6d7480); c.drawCircle(32, 35, 9, p); p.setColor(ink); c.drawCircle(32, 35, 6, p); break;
            case CALENDAR: { Calendar now = Calendar.getInstance(); Paint t = new Paint(Paint.ANTI_ALIAS_FLAG); t.setTextAlign(Paint.Align.CENTER); t.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                t.setColor(monochrome ? ink : 0xffff3b30); t.setTextSize(10.5f); c.drawText(now.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()).toUpperCase(Locale.ROOT), 32, 19, t);
                t.setColor(monochrome ? ink : 0xff1c1c1e); t.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL)); t.setTextSize(31); c.drawText(String.valueOf(now.get(Calendar.DAY_OF_MONTH)), 32, 49, t); break; }
            case CLOCK: fill(p); p.setColor(monochrome ? ink : 0xffffffff); c.drawCircle(32, 32, 21, p); p.setColor(monochrome ? 0x66000000 : 0xff1c1c1e); stroke(p, 2.8f); p.setStrokeCap(Paint.Cap.ROUND); c.drawLine(32, 32, 32, 18, p); c.drawLine(32, 32, 42, 37, p); p.setColor(0xffff9500); stroke(p, 1.3f); c.drawLine(32, 32, 22, 43, p); break;
            case BROWSER: stroke(p, 3f); c.drawCircle(32, 32, 19, p); c.drawOval(23, 13, 41, 51, p); c.drawLine(13, 32, 51, 32, p); c.drawLine(32, 13, 32, 51, p); c.drawLine(16, 23, 48, 23, p); c.drawLine(16, 41, 48, 41, p); break;
            case MAPS: fill(p); path.moveTo(32, 52); path.cubicTo(24, 42, 18, 35, 18, 27); path.cubicTo(18, 19, 24, 13, 32, 13); path.cubicTo(40, 13, 46, 19, 46, 27); path.cubicTo(46, 35, 40, 42, 32, 52); path.close(); c.drawPath(path, p); p.setColor(monochrome ? 0x55000000 : 0xff2fae60); c.drawCircle(32, 27, 6, p); break;
            case KEYBOARD: fill(p); c.drawRoundRect(9, 20, 55, 46, 5, 5, p); p.setColor(monochrome ? 0x55000000 : 0xff515b6b); for (int r = 0; r < 2; r++) for (int col = 0; col < 6; col++) c.drawRoundRect(13 + col * 7, 24 + r * 7, 18 + col * 7, 29 + r * 7, 1, 1, p); c.drawRoundRect(19, 38, 45, 42, 1, 1, p); break;
            case CALCULATOR: fill(p); for (int r = 0; r < 3; r++) for (int col = 0; col < 3; col++) c.drawCircle(19 + col * 13, 20 + r * 12, 4.6f, p); c.drawRoundRect(14, 51, 37, 56, 2.5f, 2.5f, p); c.drawCircle(45, 53.5f, 4.6f, p); break;
            case TOOLS: stroke(p, 5f); p.setStrokeCap(Paint.Cap.ROUND); c.drawLine(20, 44, 38, 26, p); fill(p); c.drawCircle(42, 22, 9, p); p.setColor(monochrome ? 0x00000000 : 0xff4c5566); if (!monochrome) c.drawCircle(46, 18, 4.5f, p); break;
            case MESSAGES: fill(p); c.drawOval(11, 14, 53, 46, p); path.moveTo(18, 40); path.lineTo(14, 52); path.lineTo(28, 44); path.close(); c.drawPath(path, p); break;
            case CONTACTS: fill(p); c.drawCircle(32, 25, 9, p); path.addArc(16, 36, 48, 64, 180, 180); c.drawPath(path, p); break;
            case WEATHER: fill(p); if (!monochrome) p.setColor(0xffffd60a); c.drawCircle(25, 24, 9, p); p.setColor(ink); c.drawCircle(30, 40, 8, p); c.drawCircle(40, 35, 10, p); c.drawRoundRect(22, 38, 52, 48, 5, 5, p); break;
            case SOUND: stroke(p, 3.6f); p.setStrokeCap(Paint.Cap.ROUND); for (int i = 0; i < 4; i++) { float x = 18 + i * 9.5f; c.drawLine(x, 16, x, 48, p); } fill(p); float[] ys = {38, 24, 34, 22}; for (int i = 0; i < 4; i++) c.drawCircle(18 + i * 9.5f, ys[i], 4.6f, p); break;
            case YOUTUBE: fill(p); c.drawRoundRect(10, 18, 54, 46, 10, 10, p); p.setColor(monochrome ? 0x55000000 : 0xffd40f1e); path.moveTo(28, 25); path.lineTo(28, 39); path.lineTo(40, 32); path.close(); c.drawPath(path, p); break;
            case STORE: stroke(p, 4f); p.setStrokeCap(Paint.Cap.ROUND); c.drawLine(24, 46, 38, 18, p); c.drawLine(40, 46, 26, 18, p); c.drawLine(18, 38, 46, 38, p); break;
            case MAIL: fill(p); c.drawRoundRect(10, 18, 54, 46, 5, 5, p); p.setColor(monochrome ? 0x55000000 : 0xff1a73e8); stroke(p, 2.6f); path.moveTo(12, 21); path.lineTo(32, 35); path.lineTo(52, 21); c.drawPath(path, p); break;
            case LINK: stroke(p, 4.4f); p.setStrokeCap(Paint.Cap.ROUND); c.drawRoundRect(12, 24, 32, 40, 8, 8, p); c.drawRoundRect(32, 24, 52, 40, 8, 8, p); break;
            case MIC: fill(p); c.drawRoundRect(25, 12, 39, 38, 7, 7, p); stroke(p, 3.4f); c.drawArc(19, 22, 45, 44, 0, 180, false, p); c.drawLine(32, 44, 32, 51, p); c.drawLine(25, 51, 39, 51, p); break;
            default: break;
        }
        c.restore();
    }

    private static void fill(Paint p) { p.setStyle(Paint.Style.FILL); }
    private static void stroke(Paint p, float w) { p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(w); p.setStrokeJoin(Paint.Join.ROUND); p.setStrokeCap(Paint.Cap.ROUND); }
}
