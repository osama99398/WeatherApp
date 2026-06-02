package com.example.weatherApp.exception;

public class WeatherAppException extends RuntimeException {

    public WeatherAppException(String message) {
        super(message);
    }

    public WeatherAppException(String message, Throwable cause) {
        super(message, cause);
    }
}

