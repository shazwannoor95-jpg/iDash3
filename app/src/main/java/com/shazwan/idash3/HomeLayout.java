package com.shazwan.idash3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ordered home-screen model: app components and simple one-level folders. Pure Java
 * (persisted as text) so ordering, sync and folder rules are unit-tested on the JVM.
 * Format: one item per line; a folder line is "folder:" + name + TAB + component + TAB + ...
 */
final class HomeLayout {
    static final String FOLDER = "folder:";
    static final int MAX_FOLDER_NAME = 24;

    static final class Item {
        final String component;          // null for folders
        String name;                     // folder name
        final ArrayList<String> children; // folder contents
        private Item(String component, String name, ArrayList<String> children) { this.component = component; this.name = name; this.children = children; }
        static Item app(String component) { return new Item(component, null, null); }
        static Item folder(String name) { return new Item(null, name, new ArrayList<>()); }
        boolean isFolder() { return children != null; }
    }

    final ArrayList<Item> items = new ArrayList<>();

    static HomeLayout parse(String saved) {
        HomeLayout out = new HomeLayout();
        if (saved == null || saved.isEmpty()) return out;
        Set<String> seen = new HashSet<>();
        for (String line : saved.split("\n")) {
            if (line.isEmpty()) continue;
            if (line.startsWith(FOLDER)) {
                String[] parts = line.substring(FOLDER.length()).split("\t");
                Item f = Item.folder(cleanName(parts[0]));
                for (int i = 1; i < parts.length; i++) if (!parts[i].isEmpty() && seen.add(parts[i])) f.children.add(parts[i]);
                if (!f.children.isEmpty()) out.items.add(f);
            } else if (seen.add(line)) out.items.add(Item.app(line));
        }
        out.normalise();
        return out;
    }

    String serialize() {
        StringBuilder b = new StringBuilder();
        for (Item it : items) {
            if (b.length() > 0) b.append('\n');
            if (it.isFolder()) { b.append(FOLDER).append(cleanName(it.name)); for (String c : it.children) b.append('\t').append(c); }
            else b.append(it.component);
        }
        return b.toString();
    }

    static String cleanName(String raw) {
        String s = raw == null ? "" : raw.replace('\t', ' ').replace('\n', ' ').trim();
        if (s.length() > MAX_FOLDER_NAME) s = s.substring(0, MAX_FOLDER_NAME).trim();
        return s.isEmpty() ? "Folder" : s;
    }

    /** Drops uninstalled apps, then appends newly installed ones in the given order. Returns true if changed. */
    boolean sync(List<String> installed) {
        String before = serialize();
        Set<String> present = new HashSet<>(installed), placed = new HashSet<>();
        for (int i = items.size() - 1; i >= 0; i--) {
            Item it = items.get(i);
            if (it.isFolder()) { it.children.retainAll(present); placed.addAll(it.children); }
            else if (!present.contains(it.component)) items.remove(i);
            else placed.add(it.component);
        }
        for (String c : installed) if (placed.add(c)) items.add(Item.app(c));
        normalise();
        return !before.equals(serialize());
    }

    /** Folders with no apps vanish (a one-app folder is kept: the user created it on purpose). */
    private void normalise() {
        for (int i = items.size() - 1; i >= 0; i--) if (items.get(i).isFolder() && items.get(i).children.isEmpty()) items.remove(i);
    }

    boolean move(int index, int delta) {
        int to = index + delta;
        if (index < 0 || index >= items.size() || to < 0 || to >= items.size() || delta == 0) return false;
        Item it = items.remove(index);
        items.add(to, it);
        return true;
    }

    /** Index of the folder with this name, or -1. */
    int folderIndex(String name) {
        String n = cleanName(name);
        for (int i = 0; i < items.size(); i++) if (items.get(i).isFolder() && items.get(i).name.equalsIgnoreCase(n)) return i;
        return -1;
    }

    List<String> folderNames() {
        ArrayList<String> out = new ArrayList<>();
        for (Item it : items) if (it.isFolder()) out.add(it.name);
        return out;
    }

    /**
     * Moves the app at index into the named folder, creating the folder in its place if needed.
     * Returns the folder's resulting index, or -1 if index is not an app.
     */
    int addToFolder(int index, String name) {
        if (index < 0 || index >= items.size() || items.get(index).isFolder()) return -1;
        String component = items.get(index).component;
        int f = folderIndex(name);
        if (f < 0) {
            Item folder = Item.folder(cleanName(name));
            folder.children.add(component);
            items.set(index, folder);
            return index;
        }
        items.get(f).children.add(component);
        items.remove(index);
        return f > index ? f - 1 : f;
    }

    /** Takes an app out of a folder and places it right after the folder. */
    boolean removeFromFolder(int folderIndex, String component) {
        if (folderIndex < 0 || folderIndex >= items.size() || !items.get(folderIndex).isFolder()) return false;
        Item f = items.get(folderIndex);
        if (!f.children.remove(component)) return false;
        items.add(folderIndex + 1, Item.app(component));
        normalise();
        return true;
    }

    boolean rename(int folderIndex, String name) {
        if (folderIndex < 0 || folderIndex >= items.size() || !items.get(folderIndex).isFolder()) return false;
        items.get(folderIndex).name = cleanName(name);
        return true;
    }

    static int pages(int count, int perPage) { return perPage <= 0 ? 1 : Math.max(1, (count + perPage - 1) / perPage); }
}
