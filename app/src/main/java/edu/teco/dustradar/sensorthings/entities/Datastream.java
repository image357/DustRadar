package edu.teco.dustradar.sensorthings.entities;

import edu.teco.dustradar.sensorthings.entities.geojson.GeoPolygon;
import edu.teco.dustradar.sensorthings.entities.helper.ISODate;
import edu.teco.dustradar.sensorthings.entities.helper.ISODatePeriod;
import edu.teco.dustradar.sensorthings.entities.helper.UnitOfMeasurement;

public class Datastream extends Entity {

    // private members

    private String name = null;
    private String description = null;
    private UnitOfMeasurement unitOfMeasurement = null;
    private String observationType = null;
    private GeoPolygon observedArea = null;
    private String phenomenonTime = null;
    private String resultTime = null;


    // constructors

    public Datastream() {
    }

    public Datastream(String id) {
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


    public UnitOfMeasurement getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }


    public String getObservationType() {
        return observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }


    public GeoPolygon getObservedArea() {
        return observedArea;
    }

    public void setObservedArea(GeoPolygon observedArea) {
        this.observedArea = observedArea;
    }


    public String getPhenomenonTime() {
        return phenomenonTime;
    }

    public void setPhenomenonTime(String phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

    public void setPhenomenonTime(ISODatePeriod period) {
        this.phenomenonTime = period.getISOString();
    }

    public void setPhenomenonTime(ISODate start, ISODate end) {
        this.phenomenonTime = (new ISODatePeriod(start, end)).getISOString();
    }


    public String getResultTime() {
        return resultTime;
    }

    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    public void setResultTime(ISODatePeriod period) {
        this.resultTime = period.getISOString();
    }

    public void setResultTime(ISODate start, ISODate end) {
        this.resultTime = (new ISODatePeriod(start, end)).getISOString();
    }
}
