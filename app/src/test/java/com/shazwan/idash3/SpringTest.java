package com.shazwan.idash3;

import static org.junit.Assert.*;
import org.junit.Test;

public class SpringTest {
    @Test public void settlesOnTarget() {
        Spring s = Spring.snappy(0f); s.target = 1f;
        int frames = 0; while (s.step(1f / 60f) && frames < 600) frames++;
        assertTrue("must settle", frames < 600);
        assertEquals(1f, s.value, 1e-6f);
    }
    @Test public void underdampedOvershootsCriticalDoesNot() {
        Spring bounce = new Spring(400f, .5f, 0f); bounce.target = 1f;
        Spring crit = new Spring(400f, 1f, 0f); crit.target = 1f;
        float maxB = 0, maxC = 0;
        for (int i = 0; i < 300; i++) { bounce.step(1f / 60f); crit.step(1f / 60f); maxB = Math.max(maxB, bounce.value); maxC = Math.max(maxC, crit.value); }
        assertTrue(maxB > 1.05f);
        assertTrue(maxC <= 1.0005f);
    }
    @Test public void stableWithHugeFrameTime() {
        Spring s = Spring.soft(0f); s.target = 5f;
        for (int i = 0; i < 100; i++) s.step(2f); // clamped to 100 ms
        assertFalse(Float.isNaN(s.value)); assertEquals(5f, s.value, 1e-3f);
    }
    @Test public void zeroDtDoesNothing() {
        Spring s = Spring.snappy(0f); s.target = 1f;
        assertTrue(s.step(0f)); assertEquals(0f, s.value, 0f);
    }
}
