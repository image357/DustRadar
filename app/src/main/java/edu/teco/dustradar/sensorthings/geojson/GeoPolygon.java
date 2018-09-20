package edu.teco.dustradar.sensorthings.geojson;

import java.io.Serializable;
import java.util.List;

public class GeoPolygon extends GeoEntity implements Serializable {

    // constructors

    public GeoPolygon() {
        super("Polygon");
    }

    public GeoPolygon(GeoPolygon old) {
        super(old);
    }


    // public methods

    public void insertPolygon(List<List> polygon) {
        super.insertCoordinate(polygon);
    }

}
