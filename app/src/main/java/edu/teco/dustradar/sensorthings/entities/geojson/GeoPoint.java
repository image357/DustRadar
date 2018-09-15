package edu.teco.dustradar.sensorthings.entities.geojson;

import java.io.Serializable;
import java.util.List;

public class GeoPoint extends GeoEntity implements Serializable {

    // constructors

    public GeoPoint() {
        super("Point");
    }

    public GeoPoint(GeoPoint old) {
        super(old);
    }

    public GeoPoint(double longitude, double latitude) {
        super("Point");
        setPoint(longitude, latitude);
    }

    public GeoPoint(double longitude, double latitude, double height) {
        super("Point");
        setPoint(longitude, latitude, height);
    }


    // public methods

    public void setPoint(List<Double> point) {
        super.setCoordinates(point);
    }

    public void setPoint(double longitude, double latitude) {
        setPoint(GeoEntity.createPoint(longitude, latitude));
    }

    public void setPoint(double longitude, double latitude, double height) {
        setPoint(GeoEntity.createPoint(longitude, latitude, height));
    }

}
