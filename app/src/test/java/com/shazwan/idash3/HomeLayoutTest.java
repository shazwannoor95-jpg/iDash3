package com.shazwan.idash3;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;

public class HomeLayoutTest {
    @Test public void syncAppendsNewAndDropsRemoved() {
        HomeLayout l = HomeLayout.parse("b\na\ngone");
        assertTrue(l.sync(Arrays.asList("a", "b", "c")));
        assertEquals("b\na\nc", l.serialize());
        assertFalse(l.sync(Arrays.asList("a", "b", "c")));
    }
    @Test public void roundTripWithFolder() {
        HomeLayout l = HomeLayout.parse("x\nfolder:Media\ty\tz\nw");
        assertEquals(3, l.items.size());
        assertTrue(l.items.get(1).isFolder());
        assertEquals("x\nfolder:Media\ty\tz\nw", l.serialize());
    }
    @Test public void duplicatesAreIgnoredOnParse() {
        HomeLayout l = HomeLayout.parse("a\na\nfolder:F\ta\tb");
        assertEquals("a\nfolder:F\tb", l.serialize());
    }
    @Test public void moveBounds() {
        HomeLayout l = HomeLayout.parse("a\nb\nc");
        assertFalse(l.move(0, -1)); assertFalse(l.move(2, 1)); assertFalse(l.move(1, 0));
        assertTrue(l.move(2, -2)); assertEquals("c\na\nb", l.serialize());
    }
    @Test public void createAndJoinFolder() {
        HomeLayout l = HomeLayout.parse("a\nb\nc");
        assertEquals(0, l.addToFolder(0, "Kereta"));
        assertEquals("folder:Kereta\ta\nb\nc", l.serialize());
        assertEquals(0, l.addToFolder(2, "kereta")); // case-insensitive match, folder before item
        assertEquals("folder:Kereta\ta\tc\nb", l.serialize());
        assertEquals(-1, l.addToFolder(0, "X")); // a folder cannot go into a folder
    }
    @Test public void folderIndexShiftsWhenItemBeforeIsRemoved() {
        HomeLayout l = HomeLayout.parse("a\nb\nfolder:F\tc");
        assertEquals(1, l.addToFolder(0, "F"));
        assertEquals("b\nfolder:F\tc\ta", l.serialize());
    }
    @Test public void removeFromFolderPlacesAfterAndEmptyFolderVanishes() {
        HomeLayout l = HomeLayout.parse("folder:F\ta\tb\nc");
        assertTrue(l.removeFromFolder(0, "a"));
        assertEquals("folder:F\tb\na\nc", l.serialize());
        assertTrue(l.removeFromFolder(0, "b"));
        assertEquals("b\na\nc", l.serialize());
        assertFalse(l.removeFromFolder(0, "zz"));
    }
    @Test public void syncEmptiesFolderOfUninstalledApps() {
        HomeLayout l = HomeLayout.parse("folder:F\tgone\nk");
        l.sync(Arrays.asList("k"));
        assertEquals("k", l.serialize());
    }
    @Test public void namesAreSanitised() {
        assertEquals("Folder", HomeLayout.cleanName("  "));
        assertEquals("a b", HomeLayout.cleanName("a\tb"));
        assertEquals(HomeLayout.MAX_FOLDER_NAME, HomeLayout.cleanName("abcdefghijklmnopqrstuvwxyz0123").length());
    }
    @Test public void pages() {
        assertEquals(1, HomeLayout.pages(0, 12)); assertEquals(1, HomeLayout.pages(12, 12));
        assertEquals(2, HomeLayout.pages(13, 12)); assertEquals(1, HomeLayout.pages(5, 0));
    }
}
