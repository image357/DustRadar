package edu.teco.dustradar.sensorthings.entities;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Thing {

    // private members

    private String name = null;
    private String description = null;
    private Object properties = null;


    // constructors

    public  Thing() {
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

}
