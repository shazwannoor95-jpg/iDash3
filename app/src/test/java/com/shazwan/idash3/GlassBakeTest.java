package com.shazwan.idash3;

import static org.junit.Assert.*;
import org.junit.Test;

public class GlassBakeTest {
    @Test public void bilinearSampleInterpolatesAndClamps() {
        int[] px = {0xff000000, 0xffffffff};           // 2x1: black | white
        assertEquals(0xff000000, GlassBake.sample(px, 2, 1, -10f, 0f));
        assertEquals(0xffffffff, GlassBake.sample(px, 2, 1, 50f, 0f));
        int mid = GlassBake.sample(px, 2, 1, 1f, .5f) & 0xff;
        assertTrue("midpoint blends", mid > 100 && mid < 156);
    }
}
