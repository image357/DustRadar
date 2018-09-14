package edu.teco.dustradar.sensorthings.entities.geojson;

import java.util.ArrayList;
import java.util.List;

abstract public class GeoEntity {

    // private members

    private String type = null;
    private List coordinates = null;


    // constructors

    protected GeoEntity(String type) {
        this.type = type;
    }


    // public methods

    public String getType() {
        return type;
    }


    public List getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List coordinates) {
        this.coordinates = coordinates;
    }


    // protected methods

    protected void insertCoordinate(Object element) {
        if (coordinates == null) {
            coordinates = new ArrayList();
        }

        coordinates.add(element);
    }
}
