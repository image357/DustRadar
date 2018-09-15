package edu.teco.dustradar.sensorthings.entities.geojson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

abstract public class GeoEntity implements Serializable {

    // private members

    private String type = null;
    private List coordinates = null;


    // constructors

    protected GeoEntity(String type) {
        this.type = type;
    }

    protected GeoEntity(GeoEntity old) {
        this.type = old.getType();
        this.coordinates = (List) deepCopy(old.getCoordinates());
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


    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }


    // protected methods

    protected void insertCoordinate(Object element) {
        if (coordinates == null) {
            coordinates = new ArrayList();
        }

        coordinates.add(element);
    }


    protected Object deepCopy(Object old) {
        if (old == null) {
            return  null;
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        Gson gson = gsonBuilder.create();

        return gson.fromJson(gson.toJson(old), old.getClass());
    }
}
