package mvf.mikevidev.walkandsee.models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class WalkAndSeePlace implements Comparable<WalkAndSeePlace>
{
    public String placeName;
    public String placeId;
    public LatLng placeLocation;
    public Bitmap placePhoto;
    public String placeAddress;
    public float flDistanceFromOrigin;
    public String placeDistance;
    public boolean isSelected;
    public int intPositionInRoute;
    public Double placeLat;
    public Double placeLon;
    public String placeCode;

    public WalkAndSeePlace(){}

    public WalkAndSeePlace(String placeName, String placeId, Double placeLat, Double placeLon, Bitmap placePhoto,String placeAddress, float flDistanceFromOrigin, String placeDistance, String placeCode)
    {
        this.placeId = placeId;
        this.placeName = placeName;
        this.placeLat = placeLat;
        this.placeLon = placeLon;
        this.placePhoto = placePhoto;
        this.placeAddress = placeAddress;
        this.flDistanceFromOrigin = flDistanceFromOrigin;
        this.placeDistance = placeDistance;
        this.isSelected = false;
        this.intPositionInRoute = 0;
        this.placeCode = placeCode;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public LatLng getPlaceLocation() {
        return new LatLng(this.placeLat,this.placeLon);
    }

    public void setPlaceLocation(LatLng placeLocation) {
        this.placeLocation = new LatLng(this.placeLat,this.placeLon);
    }

    public Bitmap getPlacePhoto() {
        return placePhoto;
    }

    public void setPlacePhoto(Bitmap placePhoto) {
        this.placePhoto = placePhoto;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public float getFlDistanceFromOrigin() {
        return flDistanceFromOrigin;
    }

    public void setFlDistanceFromOrigin(float flDistanceFromOrigin) {this.flDistanceFromOrigin = flDistanceFromOrigin;}

    public String getPlaceDistance() {
        return placeDistance;
    }

    public void setPlaceDistance(String placeDistance) {
        this.placeDistance = placeDistance;
    }

    public boolean isSelected() {return isSelected;}

    public void setSelected(boolean selected) {isSelected = selected;}

    public int getIntPositionInRoute() {return intPositionInRoute;}

    public void setIntPositionInRoute(int intPositionInRoute) { this.intPositionInRoute = intPositionInRoute;}
    public Double getPlaceLat() {
        return placeLat;
    }

    public void setPlaceLat(Double placeLat) {
        this.placeLat = placeLat;
    }

    public Double getPlaceLon() {
        return placeLon;
    }

    public void setPlaceLon(Double placeLon) {
        this.placeLon = placeLon;
    }

    public String getPlaceCode() {return placeCode;}

    public void setPlaceCode(String placeCode) {this.placeCode = placeCode;}

    @Override
    public String toString() {
        return "Place{" +
                "placeName='" + placeName + '\'' +
                ", placeId='" + placeId + '\'' +
                ", placeLocation=" + placeLocation +
                ", placePhoto='" +  placePhoto + '\'' +
                ", placeAddress='" +  placeAddress + '\'' +
                ", flDistanceFromOrigin='" +  flDistanceFromOrigin + '\'' +
                '}';
    }

    @Override
    public int compareTo(WalkAndSeePlace comparePlace) {

        int intComparePlace = (int) ((WalkAndSeePlace) comparePlace).getFlDistanceFromOrigin();

        return (int) this.getFlDistanceFromOrigin() - intComparePlace;
    }

    @Exclude
    public Map<String,Object> toMapInstance()
    {
        HashMap<String,Object> mapToReturn = new HashMap<>();
        mapToReturn.put("placeName",this.placeName);
        mapToReturn.put("placeId",this.placeId);
        mapToReturn.put("placeLat",this.placeLat);
        mapToReturn.put("placeLon",this.placeLon);
        mapToReturn.put("placeAddress",this.placeAddress);
        mapToReturn.put("isSelected",this.isSelected);
        mapToReturn.put("placeCode",this.placeCode);
        mapToReturn.put("intPositionInRoute",this.intPositionInRoute);
        return mapToReturn;
    }
}
