package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Thing extends Entity implements Serializable {

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

    public Thing(Thing old) {
        super(old);

        this.name = old.getName();
        this.description = old.getDescription();

        this.properties = deepCopy(old.getProperties());
        this.Locations = (List<Location>) deepCopy(old.getLocations());
        this.HistoricalLocations = (List<HistoricalLocation>) deepCopy(old.getHistoricalLocations());
        this.Datastreams = (List<Datastream>) deepCopy(old.getDatastreams());
    }

    public Thing(String id) {
        super(id);
    }


    // public methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription () {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }


    public List<Location> getLocations() {
        return Locations;
    }

    public void setLocations(List<Location> locations) {
        this.Locations = locations;
    }

    public void insertLocation(Location location) {
        if (Locations == null) {
            Locations = new ArrayList<>();
        }

        Locations.add(location);
    }

    public void linkLocation(String id) {
        insertLocation(new Location(id));
    }


    public List<HistoricalLocation> getHistoricalLocations() {
        return HistoricalLocations;
    }

    public void setHistoricalLocations(List<HistoricalLocation> historicalLocations) {
        this.HistoricalLocations = historicalLocations;
    }

    public void insertHistrocialLocation(HistoricalLocation historicalLocation) {
        if (HistoricalLocations == null) {
            HistoricalLocations = new ArrayList<>();
        }

        HistoricalLocations.add(historicalLocation);
    }

    public void linkHistoricalLocation(String id) {
        insertHistrocialLocation(new HistoricalLocation(id));
    }


    public List<Datastream> getDatastreams() {
        return Datastreams;
    }

    public void setDatastreams(List<Datastream> datastreams) {
        this.Datastreams = datastreams;
    }

    public void insertDatastream(Datastream datastream) {
        if (Datastreams == null) {
            Datastreams = new ArrayList<>();
        }

        Datastreams.add(datastream);
    }

    public void linkDatastream(String id) {
        insertDatastream(new Datastream(id));
    }
}
