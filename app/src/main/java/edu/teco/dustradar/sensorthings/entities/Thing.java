package edu.teco.dustradar.sensorthings.entities;

import java.util.ArrayList;
import java.util.Collection;

public class Thing extends Entity {

    // private members

    private String name = null;
    private String description = null;
    private Object properties = null;

    private Collection<Location> Locations = null;
    private Collection<HistoricalLocation> HistoricalLocations = null;
    private Collection<Datastream> Datastreams = null;


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


    public Collection<Location> getLocations() {
        return Locations;
    }

    public void setLocations(Collection<Location> arg) {
        Locations = arg;
    }

    public void linkLocation(String id) {
        if (Locations == null) {
            Locations = new ArrayList<>();
        }

        Locations.add(new Location(id));
    }


    public Collection<HistoricalLocation> getHistoricalLocations() {
        return HistoricalLocations;
    }

    public void setHistoricalLocations(Collection<HistoricalLocation> arg) {
        HistoricalLocations = arg;
    }

    public void linkHistoricalLocation(String id) {
        if (HistoricalLocations == null) {
            HistoricalLocations = new ArrayList<>();
        }

        HistoricalLocations.add(new HistoricalLocation(id));
    }


    public Collection<Datastream> getDatastreams() {
        return Datastreams;
    }

    public void setDatastreams(Collection<Datastream> arg) {
        Datastreams = arg;
    }

    public void linkDatastream(String id) {
        if (Datastreams == null) {
            Datastreams = new ArrayList<>();
        }

        Datastreams.add(new Datastream(id));
    }
}
