package com.shazwan.idash3;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ResolutionStageTest {
    @Test public void fitsByTheLimitingSide() {
        assertEquals(0.5f, ResolutionStage.fit(427, 240, 854, 480), 1e-4f);    // exact 16:9
        assertEquals(290f / 480f, ResolutionStage.fit(880, 290, 854, 480), 1e-4f); // wide panel: height limits
        assertEquals(400f / 854f, ResolutionStage.fit(400, 600, 854, 480), 1e-4f); // tall panel: width limits
    }
    @Test public void invalidInputsDoNotScale() {
        assertEquals(1f, ResolutionStage.fit(0, 100, 854, 480), 0f);
        assertEquals(1f, ResolutionStage.fit(100, 100, 0, 480), 0f);
    }
    @Test public void presetsAreSixteenByNineExceptFill() {
        for (int i = 1; i < ResolutionStage.SIZES.length; i++) assertEquals(16f / 9f, ResolutionStage.SIZES[i][0] / (float) ResolutionStage.SIZES[i][1], .01f);
        assertEquals(ResolutionStage.NAMES.length, ResolutionStage.SIZES.length);
    }
}
