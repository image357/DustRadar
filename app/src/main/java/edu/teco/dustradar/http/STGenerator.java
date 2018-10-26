package edu.teco.dustradar.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.preference.PreferenceManager;

import java.net.URISyntaxException;

import edu.teco.dustradar.R;
import edu.teco.dustradar.data.DataObject;
import edu.teco.dustradar.sensorthings.Datastream;
import edu.teco.dustradar.sensorthings.FeatureOfInterest;
import edu.teco.dustradar.sensorthings.Observation;
import edu.teco.dustradar.sensorthings.ObservedProperty;
import edu.teco.dustradar.sensorthings.Sensor;
import edu.teco.dustradar.sensorthings.Thing;
import edu.teco.dustradar.sensorthings.geojson.GeoPoint;
import edu.teco.dustradar.sensorthings.helper.UnitOfMeasurement;

public class STGenerator {

    private final static String TAG = STGenerator.class.getSimpleName();

    // private members

    private DataObject data = null;
    Resources resources = null;
    SharedPreferences sharedPref = null;

    // constants
    private final String sensor_SDS011_id = "saqn:s:teco.edu:SDS011";
    private final String observedProperty_PM10_id = "saqn:o:PM10";
    private final String observedProperty_PM25_id = "saqn:o:PM25";

    // SensorThings
    private Thing thing = null;
    private Datastream datastream_PM10 = null;
    private Datastream datastream_PM25 = null;
    private Sensor sensor_SDS011 = null;
    private ObservedProperty observedProperty_PM10 = null;
    private ObservedProperty observedProperty_PM25 = null;
    private FeatureOfInterest featureOfInterest = null;
    private Observation observation_PM10 = null;
    private Observation observation_PM25 = null;


    // constructors

    public STGenerator(Context context, DataObject data) {
        this.data = data;
        resources = context.getResources();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        createThing();
        createDatastream_PM10();
        createDatastream_PM25();
        createSensor_SDS011();
        createObservedProperty_PM10();
        createObservedProperty_PM25();
        createEvent_PM10();
        createEvent_PM25();
    }


    // public methods

    public String getThing() {
        return thing.toJson();
    }

    public String getThing_id() {
        return thing.getId();
    }

    public String getDatastream_PM10() {
        return datastream_PM10.toJson();
    }

    public String getDatastream_PM10_id() {
        return datastream_PM10.getId();
    }

    public String getDatastream_PM25() {
        return datastream_PM25.toJson();
    }

    public String getDatastream_PM25_id() {
        return datastream_PM25.getId();
    }

    public String getSensor_SDS011() {
        return sensor_SDS011.toJson();
    }

    public String getSensor_SDS011_id() {
        return sensor_SDS011.getId();
    }

    public String getObservedProperty_PM10() {
        return observedProperty_PM10.toJson();
    }

    public String getObservedProperty_PM10_id() {
        return observedProperty_PM10.getId();
    }

    public String getObservedProperty_PM25() {
        return observedProperty_PM25.toJson();
    }

    public String getObservedProperty_PM25_id() {
        return observedProperty_PM25.getId();
    }

    public String getEvent_PM10() {
        return observation_PM10.toJson();
    }

    public String getEvent_PM25() {
        return observation_PM25.toJson();
    }


    // private methods

    private void createThing() {
        String key = null;
        String value = null;
        thing = new Thing();

        key = resources.getString(R.string.blebridge_pref_thing_id_key);
        value = sharedPref.getString(key, null);
        thing.setId(value);

        thing.setName("HMGU-Messwagen");
        thing.setDescription("Mobiler HMGU-Messwagen fuer Aerosolmessungen, ausgestattet mit SDS011 Sensoren");
    }

    private void createDatastream_PM10() {
        String key = null;
        String value = null;
        datastream_PM10 = new Datastream();

        key = resources.getString(R.string.blebridge_pref_pm_datastream_id_key);
        value = sharedPref.getString(key, null);
        datastream_PM10.setId(value + ":PM10");

        datastream_PM10.setName("PM 10");
        datastream_PM10.setDescription("n/a");

        UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement();
        unitOfMeasurement.setName("microgram per cubic meter");
        unitOfMeasurement.setSymbol("ug/m^3");
        unitOfMeasurement.setDefinition("none");
        datastream_PM10.setUnitOfMeasurement(unitOfMeasurement);

        datastream_PM10.setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");

        datastream_PM10.linkSensor(sensor_SDS011_id);
        datastream_PM10.linkObservedProperty(observedProperty_PM10_id);

        key = resources.getString(R.string.blebridge_pref_thing_id_key);
        value = sharedPref.getString(key, null);
        datastream_PM10.linkThing(value);
    }

    private void createDatastream_PM25() {
        String key = null;
        String value = null;
        datastream_PM25 = new Datastream();

        key = resources.getString(R.string.blebridge_pref_pm_datastream_id_key);
        value = sharedPref.getString(key, null);
        datastream_PM25.setId(value + ":PM25");

        datastream_PM25.setName("PM 2.5");
        datastream_PM25.setDescription("n/a");

        UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement();
        unitOfMeasurement.setName("microgram per cubic meter");
        unitOfMeasurement.setSymbol("ug/m^3");
        unitOfMeasurement.setDefinition("none");
        datastream_PM25.setUnitOfMeasurement(unitOfMeasurement);

        datastream_PM25.setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");

        datastream_PM25.linkSensor(sensor_SDS011_id);
        datastream_PM25.linkObservedProperty(observedProperty_PM25_id);

        key = resources.getString(R.string.blebridge_pref_thing_id_key);
        value = sharedPref.getString(key, null);
        datastream_PM25.linkThing(value);
    }

    private void createSensor_SDS011() {
        sensor_SDS011 = new Sensor();

        sensor_SDS011.setId(sensor_SDS011_id);
        sensor_SDS011.setName("Nova SDS011");
        sensor_SDS011.setDescription("Particulate matter sensor for PM 10 and PM 2.5");
        sensor_SDS011.setEncodingType("application/pdf");
        sensor_SDS011.setMetadata("http://www.teco.edu/~koepke/SDS011.pdf");
    }

    private void createObservedProperty_PM10() {
        observedProperty_PM10 = new ObservedProperty();

        observedProperty_PM10.setId(observedProperty_PM10_id);
        observedProperty_PM10.setName("PM 10");
        observedProperty_PM10.setDescription("Particulate matter with an approximate diameter of less than 10 micrometers");
        try {
            observedProperty_PM10.setDefinition("https://www.eea.europa.eu/themes/air/air-quality/resources/glossary/pm10");
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Cannot create ObservedProperty");
        }
    }

    private void createObservedProperty_PM25() {
        observedProperty_PM25 = new ObservedProperty();

        observedProperty_PM25.setId(observedProperty_PM25_id);
        observedProperty_PM25.setName("PM 2.5");
        observedProperty_PM25.setDescription("Particulate matter with an approximate diameter of less than 2.5 micrometers");
        try {
            observedProperty_PM25.setDefinition("https://www.eea.europa.eu/themes/air/air-quality/resources/glossary/pm10");
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Cannot create ObservedProperty");
        }
    }

    private void createFeatureOfInterest() {
        featureOfInterest = new FeatureOfInterest();

        featureOfInterest.setName("");
        featureOfInterest.setDescription("");
        featureOfInterest.setEncodingTypeGeoJson();

        GeoPoint point = new GeoPoint();
        point.setPoint(data.getLongitude(), data.getLatitude(), data.getHeight());
        featureOfInterest.setFeature(point);
    }

    private void createEvent_PM10() {
        if (featureOfInterest == null) {
            createFeatureOfInterest();
        }

        String key = null;
        String value = null;
        observation_PM10 = new Observation();

        observation_PM10.setPhenomenonTime(data.getTime());
        observation_PM10.setResult(data.getPM10());
        observation_PM10.setFeatureOfInterest(featureOfInterest);

        key = resources.getString(R.string.blebridge_pref_pm_datastream_id_key);
        value = sharedPref.getString(key, null);
        observation_PM10.linkDatastream(value + ":PM10");
    }

    private void createEvent_PM25() {
        if (featureOfInterest == null) {
            createFeatureOfInterest();
        }

        String key = null;
        String value = null;
        observation_PM25 = new Observation();

        observation_PM25.setPhenomenonTime(data.getTime());
        observation_PM25.setResult(data.getPM25());
        observation_PM25.setFeatureOfInterest(featureOfInterest);

        key = resources.getString(R.string.blebridge_pref_pm_datastream_id_key);
        value = sharedPref.getString(key, null);
        observation_PM25.linkDatastream(value + ":PM25");
    }

}
