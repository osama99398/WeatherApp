package com.example.weatherApp.service;

import com.example.weatherApp.client.WeatherClient;
import com.example.weatherApp.dto.Coordinates;
import com.example.weatherApp.dto.ForecastApiResponse;
import com.example.weatherApp.dto.WeatherApiResponse;
import com.example.weatherApp.dto.WeatherResponse;
import com.example.weatherApp.exception.WeatherAppException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class WeatherService {

    private final WeatherClient weatherClient;

    public WeatherService(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    public WeatherResponse getWeather(String pinCode, String date) {

        // Validate date format (throws DateTimeParseException if invalid → caught by GlobalExceptionHandler)
        LocalDate requestedDate = LocalDate.parse(date);
        LocalDate today = LocalDate.now();
        long daysFromNow = ChronoUnit.DAYS.between(today, requestedDate);

        if (daysFromNow < 0) {
            throw new WeatherAppException(
                    "Historical weather data is not supported. Please provide today's or a future date.");
        }
        if (daysFromNow > 5) {
            throw new WeatherAppException(
                    "Forecast is only available for up to 5 days from today.");
        }

        // Step 1: Convert pincode → lat/lon
        Coordinates coordinates = weatherClient.getCoordinates(pinCode);

        double temperature;
        String description;

        if (daysFromNow == 0) {
            // Step 2a: Fetch current weather for today
            WeatherApiResponse apiResponse = weatherClient.fetchWeather(
                    coordinates.getLat(), coordinates.getLon());

            if (apiResponse == null) {
                throw new WeatherAppException("No weather data returned from external API.");
            }
            if (apiResponse.getMain() == null) {
                throw new WeatherAppException("Weather data is incomplete: missing temperature information.");
            }
            WeatherApiResponse.Weather[] weatherArr = apiResponse.getWeather();
            if (weatherArr == null || weatherArr.length == 0) {
                throw new WeatherAppException("Weather data is incomplete: missing weather description.");
            }

            temperature = apiResponse.getMain().getTemp();
            description = weatherArr[0].getDescription();

        } else {
            // Step 2b: Fetch 5-day forecast and pick the entry matching the requested date
            ForecastApiResponse forecastResponse = weatherClient.fetchForecast(
                    coordinates.getLat(), coordinates.getLon());

            if (forecastResponse == null || forecastResponse.getList() == null) {
                throw new WeatherAppException("No forecast data returned from external API.");
            }

            List<ForecastApiResponse.ForecastItem> items = forecastResponse.getList();

            // Find the forecast entry whose dt_txt starts with the requested date (noon preferred)
            ForecastApiResponse.ForecastItem item = items.stream()
                    .filter(f -> f.getDtTxt() != null && f.getDtTxt().startsWith(date))
                    .reduce((first, second) -> {
                        // prefer entry closest to 12:00 noon
                        String firstTime = first.getDtTxt().substring(11);
                        String secondTime = second.getDtTxt().substring(11);
                        return Math.abs(firstTime.compareTo("12:00:00")) <= Math.abs(secondTime.compareTo("12:00:00"))
                                ? first : second;
                    })
                    .orElseThrow(() -> new WeatherAppException(
                            "No forecast data available for date: " + date +
                            ". Note: forecast entries are in 3-hour intervals."));

            if (item.getMain() == null) {
                throw new WeatherAppException("Forecast data is incomplete: missing temperature information.");
            }
            ForecastApiResponse.ForecastItem.Weather[] weatherArr = item.getWeather();
            if (weatherArr == null || weatherArr.length == 0) {
                throw new WeatherAppException("Forecast data is incomplete: missing weather description.");
            }

            temperature = item.getMain().getTemp();
            description = weatherArr[0].getDescription();
        }

        return new WeatherResponse(temperature, description, date);
    }
}
