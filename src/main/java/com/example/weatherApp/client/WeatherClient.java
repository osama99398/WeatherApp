package com.example.weatherApp.client;

import com.example.weatherApp.dto.Coordinates;
import com.example.weatherApp.dto.ForecastApiResponse;
import com.example.weatherApp.dto.GeoResponse;
import com.example.weatherApp.dto.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class WeatherClient {

    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    private static final String GEO_API =
            "https://api.openweathermap.org/geo/1.0/zip?zip={zip},IN&appid={apiKey}";

    private static final String WEATHER_API =
            "https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric&appid={apiKey}";

    private static final String FORECAST_API =
            "https://api.openweathermap.org/data/2.5/forecast?lat={lat}&lon={lon}&units=metric&appid={apiKey}";

    public Coordinates getCoordinates(String pinCode) {
        GeoResponse response = restTemplate.getForObject(
                GEO_API,
                GeoResponse.class,
                pinCode,
                apiKey
        );

        if (response == null) {
            throw new com.example.weatherApp.exception.WeatherAppException(
                    "Unable to retrieve coordinates for PIN code: " + pinCode);
        }

        return new Coordinates(response.getLat(), response.getLon());
    }

    public WeatherApiResponse fetchWeather(double lat, double lon) {
        return restTemplate.getForObject(
                WEATHER_API,
                WeatherApiResponse.class,
                lat,
                lon,
                apiKey
        );
    }

    public ForecastApiResponse fetchForecast(double lat, double lon) {
        return restTemplate.getForObject(
                FORECAST_API,
                ForecastApiResponse.class,
                lat,
                lon,
                apiKey
        );
    }
}