package edu.teco.dustradar.sensorthings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

abstract public class Entity implements Serializable {

    // private members

    @SerializedName("@iot.id")
    private String id = null;


    // constructors

    protected Entity() {
    }

    protected Entity(Entity old) {
        this.id = old.getId();
    }

    protected Entity(String id) {
        this.id = id;
    }


    // public methods

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }


    // protected methods

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
