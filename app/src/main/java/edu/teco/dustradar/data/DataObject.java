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

    // constants
    private final String key_PM10 = "PM10";
    private final String key_PM25 = "PM25";
    private final String key_TEMP = "TEMP";
    private final String key_HUM = "HUM";
    private final String key_ATM = "ATM";


    // constructors

    public DataObject(String data) {
        isvalid = true;

        setData(data);
        time = new ISODateInstance();
        Location location = GPSService.getLocation();
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


    public DataObject() {
        this(null);
    }


    // public methods

    public boolean isValid() {
        if (isvalid == false) {
            return false;
        }

        // TODO: check data

        return true;
    }

    public void setValid(boolean valid) {
        this.isvalid = valid;
    }


    public ISODateInstance getTime() {
        return time;
    }

    public void setTime(ISODateInstance time) {
        this.time = time;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }


    public String getData() {
        return data;
    }

    public void setData(String data) {
        isvalid = true;

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


    public double getPM10() {
        if (!isValid()) {
            return -1.0;
        }

        if (!data_map.containsKey(key_PM10)) {
            return -1.0;
        }

        return data_map.get(key_PM10).doubleValue();
    }


    public double getPM25() {
        if (!isValid()) {
            return -1.0;
        }

        if (!data_map.containsKey(key_PM25)) {
            return -1.0;
        }

        return data_map.get(key_PM25).doubleValue();
    }


    public double getTemperature() {
        if (!isValid()) {
            return -1000.0;
        }

        if (!data_map.containsKey(key_TEMP)) {
            return -1000.0;
        }

        return data_map.get(key_TEMP).doubleValue();
    }


    public double getHumidity() {
        if (!isValid()) {
            return -1.0;
        }

        if (!data_map.containsKey(key_HUM)) {
            return -1.0;
        }

        return data_map.get(key_HUM).doubleValue();
    }


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

    public static byte[] serialize(DataObject object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(object);
        return out.toByteArray();
    }


    public static DataObject deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(in);
        return ((DataObject) is.readObject());
    }

}
