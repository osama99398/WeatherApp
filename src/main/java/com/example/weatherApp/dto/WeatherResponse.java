package com.example.weatherApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeatherResponse {

    private double temperature;
    private String description;
    private String date;

}
