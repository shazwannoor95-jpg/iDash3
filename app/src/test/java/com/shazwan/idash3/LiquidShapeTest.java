package com.shazwan.idash3;

import static org.junit.Assert.*;
import org.junit.Test;

public class LiquidShapeTest {
    @Test public void gap() {
        assertEquals(0f, LiquidShape.gap(0, 0, 10, 10, 5, 5, 15, 15), 0f);
        assertEquals(5f, LiquidShape.gap(0, 0, 10, 10, 15, 0, 25, 10), 1e-6f);
        assertEquals(5f, LiquidShape.gap(0, 0, 3, 4, 6, 8, 9, 12), 1e-6f); // 3-4-5 diagonal
    }
    @Test public void mergeIsMonotonicAndBounded() {
        float prev = 2f;
        for (float g = 0; g <= 30; g += 1.5f) { float m = LiquidShape.merge(g, 24f); assertTrue(m >= 0 && m <= 1); assertTrue(m <= prev); prev = m; }
        assertEquals(1f, LiquidShape.merge(0f, 24f), 0f);
        assertEquals(0f, LiquidShape.merge(24f, 24f), 0f);
        assertEquals(0f, LiquidShape.merge(5f, 0f), 0f);
    }
    @Test public void bridgeSideBySide() {
        float[] q = new float[8];
        assertTrue(LiquidShape.bridge(0, 0, 10, 10, 14, 2, 30, 8, .5f, q));
        assertEquals(10f, q[0], 0f); assertEquals(14f, q[2], 0f); // spans the gap
        assertTrue(q[1] >= 2f && q[5] <= 8f); // inside the shared vertical overlap
    }
    @Test public void bridgeStacked() {
        float[] q = new float[8];
        assertTrue(LiquidShape.bridge(0, 0, 10, 10, 0, 14, 10, 30, 1f, q));
        assertEquals(10f, q[1], 0f); assertEquals(14f, q[5], 0f);
    }
    @Test public void noBridgeWhenOverlappingDiagonalOrBadBuffer() {
        float[] q = new float[8];
        assertFalse(LiquidShape.bridge(0, 0, 10, 10, 5, 5, 15, 15, 1f, q));
        assertFalse(LiquidShape.bridge(0, 0, 10, 10, 20, 20, 30, 30, 1f, q));
        assertFalse(LiquidShape.bridge(0, 0, 10, 10, 14, 0, 30, 10, 1f, new float[4]));
    }
}
