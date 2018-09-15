package edu.teco.dustradar.sensorthings.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sensor extends Entity implements Serializable {

    // private members

    private String name = null;
    private String description = null;
    private String encodingType = null;
    private Object metadata = null;

    private List<Datastream> Datastreams = null;


    // constructors

    public Sensor() {
    }

    public Sensor(Sensor old) {
        super(old);

        this.name = old.getName();
        this.description = old.getDescription();
        this.encodingType = old.getEncodingType();
        this.metadata = deepCopy(old.getMetadata());

        this.Datastreams = (List<Datastream>) deepCopy(old.getDatastreams());
    }

    public Sensor(String id) {
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


    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
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
