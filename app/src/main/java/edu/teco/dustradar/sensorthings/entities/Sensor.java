package edu.teco.dustradar.sensorthings.entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Sensor extends Entity implements Serializable {

    // private members

    private String name = null;
    private String description = null;
    private String encodingType = null;
    private Object metadata = null;

    @SerializedName("Datastream")
    private Datastream datastream;


    // constructors

    public Sensor() {
    }

    public Sensor(Sensor old) {
        super(old);

        this.name = old.getName();
        this.description = old.getDescription();
        this.encodingType = old.getEncodingType();
        this.metadata = deepCopy(old.getMetadata());

        if (old.getDatastream() != null) {
            this.datastream = new Datastream(old.getDatastream());
        }
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


    public Datastream getDatastream() {
        return datastream;
    }

    public void setDatastream(Datastream datastream) {
        this.datastream = datastream;
    }

    public void insertDatastream(Datastream datastream) {
        setDatastream(datastream);
    }

    public void linkDatastream(String id) {
        insertDatastream(new Datastream(id));
    }

}
