package com.example.tracker;

public class Card {
    private String path;
    private String title;
    private String dv1;
    private String dv2;
    private String sensor;


    public Card(String title, String path, String dv1, String dv2, String sensor) {
        this.title = title;
        this.path = path;
        this.dv1 = dv1;
        this.dv2 = dv2;
        this.sensor = sensor;
    }

    public String getTitle() {
        return title;
    }
    public String getPath() {
        return path;
    }
    public String getDv1() {
        return dv1;
    }
    public String getDv2() {
        return dv2;
    }
    public String getSensor(){ return sensor; }

}
