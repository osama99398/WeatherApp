package com.example.weatherApp.controller;

import com.example.weatherApp.dto.WeatherResponse;
import com.example.weatherApp.exception.WeatherAppException;
import com.example.weatherApp.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    private static final String TODAY = LocalDate.now().toString();
    private static final String PIN_CODE = "110001";

    @Test
    void getWeather_validParams_returnsWeatherResponse() throws Exception {
        WeatherResponse response = new WeatherResponse(22.5, "clear sky", TODAY);
        when(weatherService.getWeather(PIN_CODE, TODAY)).thenReturn(response);

        mockMvc.perform(get("/api/weather")
                        .param("pinCode", PIN_CODE)
                        .param("date", TODAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(22.5))
                .andExpect(jsonPath("$.description").value("clear sky"))
                .andExpect(jsonPath("$.date").value(TODAY));
    }

    @Test
    void getWeather_missingPinCode_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/weather")
                        .param("date", TODAY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter: pinCode"));
    }

    @Test
    void getWeather_missingDate_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/weather")
                        .param("pinCode", PIN_CODE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter: date"));
    }

    @Test
    void getWeather_serviceThrowsWeatherAppException_returnsBadRequest() throws Exception {
        when(weatherService.getWeather(PIN_CODE, TODAY))
                .thenThrow(new WeatherAppException("PIN code not found."));

        mockMvc.perform(get("/api/weather")
                        .param("pinCode", PIN_CODE)
                        .param("date", TODAY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("PIN code not found."));
    }

    @Test
    void getWeather_serviceThrowsUnexpectedException_returnsInternalServerError() throws Exception {
        when(weatherService.getWeather(PIN_CODE, TODAY))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/api/weather")
                        .param("pinCode", PIN_CODE)
                        .param("date", TODAY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}

