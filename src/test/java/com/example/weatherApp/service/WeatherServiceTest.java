package com.example.weatherApp.service;

import com.example.weatherApp.client.WeatherClient;
import com.example.weatherApp.dto.Coordinates;
import com.example.weatherApp.dto.ForecastApiResponse;
import com.example.weatherApp.dto.WeatherApiResponse;
import com.example.weatherApp.dto.WeatherResponse;
import com.example.weatherApp.exception.WeatherAppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private WeatherService weatherService;

    private static final String PIN_CODE = "110001";
    private static final Coordinates COORDS = new Coordinates(28.6, 77.2);

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private WeatherApiResponse buildCurrentWeatherResponse(double temp, String desc) {
        WeatherApiResponse response = new WeatherApiResponse();
        WeatherApiResponse.Main main = new WeatherApiResponse.Main();
        main.setTemp(temp);
        response.setMain(main);
        WeatherApiResponse.Weather weather = new WeatherApiResponse.Weather();
        weather.setDescription(desc);
        response.setWeather(new WeatherApiResponse.Weather[]{weather});
        return response;
    }

    private ForecastApiResponse buildForecastResponse(String datePrefix, double temp, String desc) {
        ForecastApiResponse forecastApiResponse = new ForecastApiResponse();
        ForecastApiResponse.ForecastItem item = new ForecastApiResponse.ForecastItem();
        item.setDtTxt(datePrefix + " 12:00:00");

        ForecastApiResponse.ForecastItem.Main main = new ForecastApiResponse.ForecastItem.Main();
        main.setTemp(temp);
        item.setMain(main);

        ForecastApiResponse.ForecastItem.Weather w = new ForecastApiResponse.ForecastItem.Weather();
        w.setDescription(desc);
        item.setWeather(new ForecastApiResponse.ForecastItem.Weather[]{w});

        forecastApiResponse.setList(List.of(item));
        return forecastApiResponse;
    }

    // ─── Current weather (today) ─────────────────────────────────────────────

    @Test
    void getWeather_today_returnsCurrentWeather() {
        String today = LocalDate.now().toString();
        when(weatherClient.getCoordinates(PIN_CODE)).thenReturn(COORDS);
        when(weatherClient.fetchWeather(COORDS.getLat(), COORDS.getLon()))
                .thenReturn(buildCurrentWeatherResponse(22.5, "clear sky"));

        WeatherResponse result = weatherService.getWeather(PIN_CODE, today);

        assertThat(result.getTemperature()).isEqualTo(22.5);
        assertThat(result.getDescription()).isEqualTo("clear sky");
        assertThat(result.getDate()).isEqualTo(today);
        verify(weatherClient, never()).fetchForecast(anyDouble(), anyDouble());
    }

    @Test
    void getWeather_today_nullApiResponse_throwsWeatherAppException() {
        String today = LocalDate.now().toString();
        when(weatherClient.getCoordinates(PIN_CODE)).thenReturn(COORDS);
        when(weatherClient.fetchWeather(COORDS.getLat(), COORDS.getLon())).thenReturn(null);

        assertThatThrownBy(() -> weatherService.getWeather(PIN_CODE, today))
                .isInstanceOf(WeatherAppException.class)
                .hasMessageContaining("No weather data returned");
    }

    @Test
    void getWeather_today_emptyWeatherArray_throwsWeatherAppException() {
        String today = LocalDate.now().toString();
        WeatherApiResponse response = new WeatherApiResponse();
        WeatherApiResponse.Main main = new WeatherApiResponse.Main();
        main.setTemp(20.0);
        response.setMain(main);
        response.setWeather(new WeatherApiResponse.Weather[]{});

        when(weatherClient.getCoordinates(PIN_CODE)).thenReturn(COORDS);
        when(weatherClient.fetchWeather(COORDS.getLat(), COORDS.getLon())).thenReturn(response);

        assertThatThrownBy(() -> weatherService.getWeather(PIN_CODE, today))
                .isInstanceOf(WeatherAppException.class)
                .hasMessageContaining("missing weather description");
    }

    // ─── Forecast (future date) ───────────────────────────────────────────────

    @Test
    void getWeather_futureDate_returnsForecastWeather() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        when(weatherClient.getCoordinates(PIN_CODE)).thenReturn(COORDS);
        when(weatherClient.fetchForecast(COORDS.getLat(), COORDS.getLon()))
                .thenReturn(buildForecastResponse(tomorrow, 30.0, "haze"));

        WeatherResponse result = weatherService.getWeather(PIN_CODE, tomorrow);

        assertThat(result.getTemperature()).isEqualTo(30.0);
        assertThat(result.getDescription()).isEqualTo("haze");
        assertThat(result.getDate()).isEqualTo(tomorrow);
        verify(weatherClient, never()).fetchWeather(anyDouble(), anyDouble());
    }

    @Test
    void getWeather_futureDate_noMatchingForecastEntry_throwsWeatherAppException() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String dayAfter = LocalDate.now().plusDays(2).toString();
        // Forecast only has entry for day-after, not tomorrow
        when(weatherClient.getCoordinates(PIN_CODE)).thenReturn(COORDS);
        when(weatherClient.fetchForecast(COORDS.getLat(), COORDS.getLon()))
                .thenReturn(buildForecastResponse(dayAfter, 28.0, "clouds"));

        assertThatThrownBy(() -> weatherService.getWeather(PIN_CODE, tomorrow))
                .isInstanceOf(WeatherAppException.class)
                .hasMessageContaining("No forecast data available for date");
    }

    // ─── Date validation ─────────────────────────────────────────────────────

    @Test
    void getWeather_pastDate_throwsWeatherAppException() {
        String yesterday = LocalDate.now().minusDays(1).toString();

        assertThatThrownBy(() -> weatherService.getWeather(PIN_CODE, yesterday))
                .isInstanceOf(WeatherAppException.class)
                .hasMessageContaining("Historical weather data is not supported");
    }

    @Test
    void getWeather_dateTooFarInFuture_throwsWeatherAppException() {
        String farFuture = LocalDate.now().plusDays(10).toString();

        assertThatThrownBy(() -> weatherService.getWeather(PIN_CODE, farFuture))
                .isInstanceOf(WeatherAppException.class)
                .hasMessageContaining("only available for up to 5 days");
    }

    @Test
    void getWeather_invalidDateFormat_throwsDateTimeParseException() {
        assertThatThrownBy(() -> weatherService.getWeather(PIN_CODE, "01-06-2026"))
                .isInstanceOf(DateTimeParseException.class);
    }
}

