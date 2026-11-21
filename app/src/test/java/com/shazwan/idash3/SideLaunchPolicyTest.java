package com.shazwan.idash3;
import org.junit.Test;import static org.junit.Assert.*;
public class SideLaunchPolicyTest {
 @Test public void pre12LUsesLegacyDocking(){for(int sdk:new int[]{26,27,28,30,31})assertFalse(SideLaunchPolicy.modern(sdk));}
 @Test public void android12LAndAboveUsesAdjacentLaunch(){for(int sdk:new int[]{32,33,34,35,36})assertTrue(SideLaunchPolicy.modern(sdk));}
}
