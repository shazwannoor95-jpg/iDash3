package com.shazwan.idash3;

import static org.junit.Assert.*;
import org.junit.Test;

public class WebVideoScriptTest {
    @Test public void h264ScriptBlocksVp9AndAv1() {
        String js = DashWebPlayer.H264_ONLY;
        assertTrue(js.contains("vp9") && js.contains("av01"));
        assertTrue(js.contains("isTypeSupported") && js.contains("canPlayType") && js.contains("decodingInfo"));
        assertTrue("idempotent guard", js.contains("__idashH264"));
    }
    @Test public void viewportScript() {
        assertEquals("", DashWebPlayer.viewportScript(0));
        assertTrue(DashWebPlayer.viewportScript(854).contains("width=854"));
    }
}
