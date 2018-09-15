package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Location extends Entity implements Serializable {

    // private members

    private String name = null;
    private String description = null;
    private String encodingType = null;
    private Object location = null;

    private List<Thing> Things = null;
    private List<HistoricalLocation> HistoricalLocations = null;


    // constructors

    public Location() {
    }

    public Location(Location old) {
        super(old);

        this.name = old.getName();
        this.description = old.getDescription();
        this.encodingType = old.getEncodingType();
        this.location = deepCopy(old.getLocation());

        this.Things = (List<Thing>) deepCopy(old.getThings());
        this.HistoricalLocations = (List<HistoricalLocation>) deepCopy(old.getHistoricalLocations());
    }

    public Location(String id) {
        super(id);
    }


    // public methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    public void setEncodingTypeGeoJson() {
        setEncodingType("application/vnd.geo+json");
    }


    public Object getLocation() {
        return location;
    }

    public void setLocation(Object location) {
        this.location = location;
    }


    public List<Thing> getThings() {
        return Things;
    }

    public void setThings(List<Thing> things) {
        Things = things;
    }

    public void insertThing(Thing thing) {
        if (Things == null) {
            Things = new ArrayList<>();
        }

        Things.add(thing);
    }

    public void linkThing(String id) {
        insertThing(new Thing(id));
    }


    public List<HistoricalLocation> getHistoricalLocations() {
        return HistoricalLocations;
    }

    public void setHistoricalLocations(List<HistoricalLocation> historicalLocations) {
        HistoricalLocations = historicalLocations;
    }

    public void insertHistoricalLocation(HistoricalLocation historicalLocation) {
        if (HistoricalLocations == null) {
            HistoricalLocations = new ArrayList<>();
        }

        HistoricalLocations.add(historicalLocation);
    }

    public void linkHistoricalLocation(String id) {
        insertHistoricalLocation(new HistoricalLocation(id));
    }

}
