package edu.teco.dustradar.sensorthings.entities.geojson;

import java.util.List;

public class GeoPolygon extends GeoEntity {

    // constructors

    public GeoPolygon() {
        super("Polygon");
    }


    // public methods

    public void insertPolygon(List<List> polygon) {
        super.insertCoordinate(polygon);
    }

}
