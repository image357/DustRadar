package edu.teco.dustradar.data;

import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;

import edu.teco.dustradar.gps.GPSService;
import edu.teco.dustradar.sensorthings.date.ISODateInstance;

/**
 * Holds measurements and metadata
 */
public class DataObject implements Serializable {

    private final static String TAG = DataObject.class.getSimpleName();

    // private members
    private ISODateInstance time = null;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double height = 0.0;
    private String data = null;
    private HashMap<String, Double> data_map = null;

    private boolean isvalid = true;

    private String sturl = null;
    private String thingid = null;
    private String datastreamid = null;
    private String datastreamid_suffix = null;

    // constants
    private final String key_PM10 = "PM10";
    private final String key_PM25 = "PM25";
    private final String key_TEMP = "TEMP";
    private final String key_HUM = "HUM";
    private final String key_ATM = "ATM";


    // constructors

    /**
     * @param data JSON String with format {"PM10": value, "PM25": value, "TEMP": value, "HUM": value, "ATM": value}
     * @param sturl SensorThings server url. Example: http://domain:8080/FROST-Server (no trailing slash and /v1.0)
     * @param thingid ID of the Thing entity that will store the Datastreams
     * @param datastreamid Prefix of the ID of the Datastreams that will store the Observations
     * @param dsidSuffix Additional suffix. Format: datastreamid:dsidSuffix
     */
    public DataObject(String data, String sturl, String thingid, String datastreamid, String dsidSuffix) {
        isvalid = true;
        setTime(new ISODateInstance());
        setLocation(GPSService.getLocation());

        setData(data);
        setStURL(sturl);
        setThingid(thingid);
        setDatastreamId(datastreamid, dsidSuffix);
    }


    // public methods

    /**
     * @return true when DataObject contains valid measurements and metadata. false otherwise
     */
    public boolean isValid() {
        if (isvalid == false) {
            return false;
        }

        // TODO: make additional checks

        return true;
    }

    /**
     * @param valid will set DataObject to be valid (= true) or not (= false)
     */
    public void setValid(boolean valid) {
        this.isvalid = valid;
    }


    /**
     * @return phenomenonTime
     */
    public ISODateInstance getTime() {
        return time;
    }

    /**
     * @param time phenomenonTime
     */
    public void setTime(ISODateInstance time) {
        if (time == null) {
            isvalid = false;
        }

        this.time = time;
    }


    /**
     * @param location Android Location object
     */
    public void setLocation(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            height = location.getAltitude();
        }
        else {
            isvalid = false;
            latitude = 0.0;
            longitude = 0.0;
            height = 0.0;
        }
    }


    /**
     * @return Latitude. Zero if invalid
     */
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    /**
     * @return Longitude. Zero if invalid
     */
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    /**
     * @return Elevation/Height. Zero if invalid
     */
    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }


    /**
     * @return JSON String with format {"PM10": value, "PM25": value, "TEMP": value, "HUM": value, "ATM": value} or {} if invalid
     */
    public String getData() {
        return data;
    }

    /**
     * @param data JSON String with format {"PM10": value, "PM25": value, "TEMP": value, "HUM": value, "ATM": value}
     */
    public void setData(String data) {
        if (data != null) {
            this.data = data;
        }
        else {
            isvalid = false;
            this.data = "{}";
        }

        Gson gson = new Gson();
        Type hashMapType = new TypeToken<HashMap<String, Double>>(){}.getType();
        try {
            data_map = gson.fromJson(this.data, hashMapType);
        }
        catch (Exception e) {
            Log.e(TAG, "Data is malformed: " + data);
            e.printStackTrace();
            isvalid = false;
        }
    }


    /**
     * @return SensorThings server url. Example: http://domain:8080/FROST-Server (no trailing slash and /v1.0)
     */
    public String getStURL() {
        return sturl;
    }

    public void setStURL(String sturl) {
        if (sturl == null) {
            isvalid = false;
        }

        this.sturl = sturl;
    }


    public String getThingId() {
        return thingid;
    }

    public void setThingid(String thingid) {
        if (thingid == null) {
            isvalid = false;
        }

        this.thingid = thingid;
    }


    public String getDatastreamId() {
        if (datastreamid_suffix == null) {
            return datastreamid;
        }

        return (datastreamid + ":" + datastreamid_suffix);
    }

    public void setDatastreamId(String datastreamid, String suffix) {
        if (datastreamid == null) {
            isvalid = false;
        }

        this.datastreamid = datastreamid;
        this.datastreamid_suffix = suffix;
    }


    /**
     * @return PM10 value. -1.0 if invalid
     */
    public double getPM10() {
        if (!isValid()) {
            return -1.0;
        }

        if (!data_map.containsKey(key_PM10)) {
            return -1.0;
        }

        return data_map.get(key_PM10).doubleValue();
    }


    /**
     * @return PM2.5 value. -1.0 if invalid
     */
    public double getPM25() {
        if (!isValid()) {
            return -1.0;
        }

        if (!data_map.containsKey(key_PM25)) {
            return -1.0;
        }

        return data_map.get(key_PM25).doubleValue();
    }


    /**
     * @return Temperature value. -1000.0 if invalid
     */
    public double getTemperature() {
        if (!isValid()) {
            return -1000.0;
        }

        if (!data_map.containsKey(key_TEMP)) {
            return -1000.0;
        }

        return data_map.get(key_TEMP).doubleValue();
    }


    /**
     * @return Humidity value. -1.0 if invalid
     */
    public double getHumidity() {
        if (!isValid()) {
            return -1.0;
        }

        if (!data_map.containsKey(key_HUM)) {
            return -1.0;
        }

        return data_map.get(key_HUM).doubleValue();
    }


    /**
     * @return Atmospheric Pressure value. -1.0 if invalid
     */
    public double getAtmosphericPressure() {
        if (!isValid()) {
            return -1.0;
        }

        if (!data_map.containsKey(key_ATM)) {
            return -1.0;
        }

        return data_map.get(key_ATM).doubleValue();
    }


    // static methods

    /**
     * @param object DataObject to serialize into byte[]
     * @return Array of bytes
     * @throws IOException Thrown when serialization fails
     */
    public static byte[] serialize(DataObject object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(object);
        return out.toByteArray();
    }


    /**
     * @param bytes Array of bytes that can be deserialize into DataObject
     * @return DataObject
     * @throws IOException Thrown when serialization fails
     * @throws ClassNotFoundException Thrown when DataObject class is not present
     */
    public static DataObject deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(in);
        return ((DataObject) is.readObject());
    }

}
