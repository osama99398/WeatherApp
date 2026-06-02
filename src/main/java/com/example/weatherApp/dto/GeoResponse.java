package com.example.weatherApp.dto;

import lombok.Data;

@Data
public class GeoResponse {
    private String name;
    private double lat;
    private double lon;
    private String country;
    private String zip;
}

