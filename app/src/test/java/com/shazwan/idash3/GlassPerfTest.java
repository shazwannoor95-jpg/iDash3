package com.shazwan.idash3;

import static org.junit.Assert.*;
import org.junit.Test;

public class GlassPerfTest {
    @Test public void p90Basic() {
        long[] v = new long[100]; for (int i = 0; i < 100; i++) v[i] = (100 - i) * 1_000_000L;
        assertEquals(90_000_000L, GlassPerf.p90(v, 100));
        assertEquals(0L, GlassPerf.p90(new long[0], 0));
    }
    @Test public void degradesOnlyAfterSustainedJank() {
        GlassPerf.Policy p = new GlassPerf.Policy();
        assertFalse(p.update(40_000_000L)); assertFalse(p.update(40_000_000L));
        assertTrue(p.update(40_000_000L)); assertTrue(p.degraded());
    }
    @Test public void singleGoodWindowResetsBadStreak() {
        GlassPerf.Policy p = new GlassPerf.Policy();
        p.update(40_000_000L); p.update(40_000_000L); p.update(8_000_000L);
        p.update(40_000_000L); p.update(40_000_000L);
        assertFalse(p.degraded());
    }
    @Test public void recoversWithHysteresis() {
        GlassPerf.Policy p = new GlassPerf.Policy();
        for (int i = 0; i < 3; i++) p.update(40_000_000L);
        assertTrue(p.degraded());
        for (int i = 0; i < GlassPerf.RECOVER_AFTER - 1; i++) assertFalse(p.update(8_000_000L));
        assertTrue(p.update(8_000_000L)); assertFalse(p.degraded());
    }
    @Test public void middlingFramesNeitherDegradeNorRecover() {
        GlassPerf.Policy p = new GlassPerf.Policy();
        for (int i = 0; i < 20; i++) p.update(18_000_000L);
        assertFalse(p.degraded());
    }
    @Test public void oneGigabyteHeadUnitCountsAsLowMemory() {
        long heap96 = 96L * 1024 * 1024, total1gb = 1024L * 1024 * 1024;
        assertTrue(GlassPerf.memoryTight(false, heap96, total1gb));
        assertTrue(GlassPerf.memoryTight(false, heap96 + 1, total1gb));
        assertTrue(GlassPerf.memoryTight(false, 256L * 1024 * 1024, total1gb));
    }
    @Test public void roomyDeviceStaysFull() {
        long heap256 = 256L * 1024 * 1024, total8gb = 8L * 1024 * 1024 * 1024;
        assertFalse(GlassPerf.memoryTight(false, heap256, total8gb));
        assertFalse(GlassPerf.memoryTight(false, 128L * 1024 * 1024, total8gb));
        assertTrue(GlassPerf.memoryTight(true, heap256, total8gb));
        assertFalse(GlassPerf.memoryTight(false, 0, 0));
    }
}
