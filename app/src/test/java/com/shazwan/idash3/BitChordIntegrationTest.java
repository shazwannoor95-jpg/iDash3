package com.shazwan.idash3;
import org.junit.Test;import static org.junit.Assert.*;
public class BitChordIntegrationTest {
 @Test public void onlyOfficialAndDevSessionsMatch(){assertTrue(BitChordIntegration.accepts("com.music.bitchord"));assertTrue(BitChordIntegration.accepts("com.dev.bitchord"));assertFalse(BitChordIntegration.accepts("com.spotify.music"));assertFalse(BitChordIntegration.accepts("com.music.bitchord.fake"));assertFalse(BitChordIntegration.accepts(null));}
}
