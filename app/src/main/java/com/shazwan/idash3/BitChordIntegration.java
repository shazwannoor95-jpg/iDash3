package com.shazwan.idash3;
/** Public Android package contract, not copied BitChord implementation. */
final class BitChordIntegration {
 static final String RELEASE="com.music.bitchord",DEV="com.dev.bitchord";
 static final String DOWNLOAD="https://github.com/kushagrasinghx/BitChord/releases/latest";
 static boolean accepts(String name){return RELEASE.equals(name)||DEV.equals(name);}
}
