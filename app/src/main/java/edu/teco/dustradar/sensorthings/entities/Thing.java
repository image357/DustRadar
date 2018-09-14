package edu.teco.dustradar.sensorthings.entities;

import java.util.ArrayList;
import java.util.List;

public class Thing extends Entity {

    // private members

    private String name = null;
    private String description = null;
    private Object properties = null;

    private List<Location> Locations = null;
    private List<HistoricalLocation> HistoricalLocations = null;
    private List<Datastream> Datastreams = null;


    // constructors

    public  Thing() {
    }

    public Thing(String id) {
        super(id);
    }


    // public methods

    public String getName() {
        return name;
    }

    public void setName(String arg) {
        name = arg;
    }


    public String getDescription () {
        return description;
    }

    public void setDescription(String arg) {
        description = arg;
    }


    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object arg) {
        properties = arg;
    }


    public List<Location> getLocations() {
        return Locations;
    }

    public void setLocations(List<Location> arg) {
        Locations = arg;
    }

    public void insertLocation(Location arg) {
        if (Locations == null) {
            Locations = new ArrayList<>();
        }

        Locations.add(arg);
    }

    public void linkLocation(String id) {
        insertLocation(new Location(id));
    }


    public List<HistoricalLocation> getHistoricalLocations() {
        return HistoricalLocations;
    }

    public void setHistoricalLocations(List<HistoricalLocation> arg) {
        HistoricalLocations = arg;
    }

    public void insertHistrocialLocation(HistoricalLocation arg) {
        if (HistoricalLocations == null) {
            HistoricalLocations = new ArrayList<>();
        }

        HistoricalLocations.add(arg);
    }

    public void linkHistoricalLocation(String id) {
        insertHistrocialLocation(new HistoricalLocation(id));
    }


    public List<Datastream> getDatastreams() {
        return Datastreams;
    }

    public void setDatastreams(List<Datastream> arg) {
        Datastreams = arg;
    }

    public void insertDatastream(Datastream arg) {
        if (Datastreams == null) {
            Datastreams = new ArrayList<>();
        }

        Datastreams.add(arg);
    }

    public void linkDatastream(String id) {
        insertDatastream(new Datastream(id));
    }
}
