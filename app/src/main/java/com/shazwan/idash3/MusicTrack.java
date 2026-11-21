/* SPDX-License-Identifier: GPL-3.0-only. BitChord provider adaptation; see third_party/bitchord. */
package com.shazwan.idash3;
final class MusicTrack {
 final String id,title,artist,album,location;final boolean local;
 MusicTrack(String id,String title,String artist,String album,String location,boolean local){this.id=id;this.title=title;this.artist=artist;this.album=album;this.location=location;this.local=local;}
 public String toString(){return title+"\n"+artist+(album.isEmpty()?"":" · "+album);}
}
