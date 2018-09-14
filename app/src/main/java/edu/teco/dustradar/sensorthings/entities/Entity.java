package edu.teco.dustradar.sensorthings.entities;

import com.google.gson.annotations.SerializedName;

abstract public class Entity {

    // private members

    @SerializedName("@iot.id")
    private String id = null;


    // constructors

    protected Entity() {
    }

    protected Entity(String id) {
        this.id = id;
    }

    protected Entity(Entity old) {
        this.id = new String(old.getId());
    }


    // public methods

    public String getId() {
        return id;
    }

    public void setId(String arg) {
        id = arg;
    }

}
