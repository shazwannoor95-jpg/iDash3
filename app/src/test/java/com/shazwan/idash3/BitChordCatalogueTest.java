/* SPDX-License-Identifier: GPL-3.0-only */
package com.shazwan.idash3;
import org.junit.Test;import static org.junit.Assert.*;import javax.crypto.*;import javax.crypto.spec.*;import java.util.*;
public class BitChordCatalogueTest {
 String encoded(String value)throws Exception{Cipher c=Cipher.getInstance("DES/ECB/PKCS5Padding");c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec("38346591".getBytes("UTF-8"),"DES"));return Base64.getEncoder().encodeToString(c.doFinal(value.getBytes("UTF-8")));}
 @Test public void adaptsProviderCodecWithoutInventingBitrate()throws Exception{String url="https://aac.saavncdn.com/music_96.mp4?token=example";assertEquals(url,BitChordCatalogue.decode(encoded(url)));}
 @Test(expected=Exception.class) public void rejectsMalformedProviderToken()throws Exception{BitChordCatalogue.decode("!invalid!");}
 @Test(expected=Exception.class) public void rejectsNonHttpsStream()throws Exception{BitChordCatalogue.decode(encoded("http://example.com/song.mp3"));}
 @Test public void encodesSearchAsOneParameter()throws Exception{String q=BitChordCatalogue.endpoint("search.getResults","q","a & b?=你好");assertTrue(q.contains("q=a+%26+b%3F%3D"));assertFalse(q.contains("q=a &"));}
 @Test public void songDisplayPreservesAlbum(){assertTrue(new MusicTrack("id","Track","Artist","Album","",false).toString().contains("Album"));}
}
