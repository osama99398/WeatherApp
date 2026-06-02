package com.example.weatherApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ForecastApiResponse {

    private List<ForecastItem> list;

    @Data
    public static class ForecastItem {
        private long dt;
        private Main main;
        private Weather[] weather;

        @JsonProperty("dt_txt")
        private String dtTxt;

        @Data
        public static class Main {
            private double temp;
            @JsonProperty("feels_like")
            private double feelsLike;
            @JsonProperty("temp_min")
            private double tempMin;
            @JsonProperty("temp_max")
            private double tempMax;
            private int pressure;
            private int humidity;
        }

        @Data
        public static class Weather {
            private int id;
            private String main;
            private String description;
            private String icon;
        }
    }
}

