package com.shazwan.idash3;
/** In-memory camera state, also written to Android instance state; never a GPS/location claim. */
final class DriveViewport {
 final double latitude,longitude,zoom;
 private DriveViewport(double lat,double lon,double level){latitude=lat;longitude=lon;zoom=level;}
 static DriveViewport of(double lat,double lon,double level){
  if(!Double.isFinite(lat)||!Double.isFinite(lon)||!Double.isFinite(level)||lat< -90||lat>90||lon< -180||lon>180||level<0||level>22)return null;
  return new DriveViewport(lat,lon,level);
 }
}
