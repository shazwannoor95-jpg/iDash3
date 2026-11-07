package com.shazwan.idash3;

import static org.junit.Assert.*;
import org.junit.Test;

public class AdaptiveTest {
    @Test public void luminanceExtremes() {
        assertEquals(0f, Adaptive.luminance(0x000000), 1e-6f);
        assertEquals(1f, Adaptive.luminance(0xffffff), 1e-4f);
    }
    @Test public void contrastBlackWhiteIs21() { assertEquals(21f, Adaptive.contrast(0xffffff, 0x000000), .01f); }
    @Test public void averageOfHalfImage() {
        int[] px = new int[4 * 4];
        for (int y = 0; y < 4; y++) for (int x = 0; x < 4; x++) px[y * 4 + x] = x < 2 ? 0xffff0000 : 0xff0000ff;
        int left = Adaptive.averageRegion(px, 4, 4, 0, 0, 2, 4, 4, 0);
        int all = Adaptive.averageRegion(px, 4, 4, 0, 0, 4, 4, 4, 0);
        assertEquals(0xffff0000, left);
        assertEquals(0xff7f007f, all);
    }
    @Test public void regionIsClampedAndInvalidFallsBack() {
        int[] px = {0xff102030};
        assertEquals(0xff102030, Adaptive.averageRegion(px, 1, 1, -50, -50, 50, 50, 3, 0));
        assertEquals(7, Adaptive.averageRegion(px, 1, 1, 5, 5, 6, 6, 3, 7));
        assertEquals(7, Adaptive.averageRegion(null, 1, 1, 0, 0, 1, 1, 3, 7));
    }
    @Test public void inkChoice() {
        assertTrue(Adaptive.preferDarkInk(0xfff0f4fa, 0x172238, 0xffffff));
        assertFalse(Adaptive.preferDarkInk(0xff202832, 0x172238, 0xffffff));
    }
}
