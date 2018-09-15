package edu.teco.dustradar.sensorthings.entities.helper;

import java.io.Serializable;

public class UnitOfMeasurement implements Serializable {

    // private members

    private String name = null;
    private String symbol = null;
    private String definition = null;


    // constructors

    public UnitOfMeasurement() {
    }

    public UnitOfMeasurement(UnitOfMeasurement old) {
        this.name = old.getName();
        this.symbol = old.getSymbol();
        this.definition = old.getDefinition();
    }


    // public methods


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

}
