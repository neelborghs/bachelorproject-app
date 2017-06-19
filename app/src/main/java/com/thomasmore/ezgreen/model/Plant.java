package com.thomasmore.ezgreen.model;

/**
 * Created by Tomas-Laptop on 26/04/2017.
 */

public class Plant {
    private String user_id;
    private String temperature;
    private String light;
    private String humidity;
    private String moisture;
    private String module_id;
    private String datetime;

    public String getUser_id() {
        return user_id;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getLight() {
        return light;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getMoisture() {
        return moisture;
    }

    public String getModule_id() {
        return module_id;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public void setMoisture(String moisture) {
        this.moisture = moisture;
    }

    public void setModule_id(String module_id) {
        this.module_id = module_id;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }


}
