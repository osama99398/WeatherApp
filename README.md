# 🌤️ WeatherApp

A Spring Boot REST API that provides current and forecasted weather data for Indian PIN codes using the [OpenWeatherMap API](https://openweathermap.org/api).

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Error Handling](#error-handling)
- [Running Tests](#running-tests)

---

## ✨ Features

- 🔍 Convert Indian PIN codes to geographic coordinates via OpenWeatherMap Geocoding API
- 🌡️ Fetch **current weather** for today (temperature in °C + description)
- 📅 Fetch **5-day forecast** for any date up to 5 days ahead
- 🛡️ Robust error handling with descriptive JSON error responses
- ✅ Comprehensive unit tests for controller and service layers

---

## 🛠️ Tech Stack

| Technology | Version |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.3 |
| Spring Web MVC | 7.0.5 |
| Lombok | 1.18.x |
| JUnit Jupiter | 6.0.3 |
| Mockito | 5.20.0 |
| Maven | Wrapper included |

---

## 📁 Project Structure

```
src/
└── main/
│   ├── java/com/example/weatherApp/
│   │   ├── WeatherAppApplication.java       # Entry point
│   │   ├── client/
│   │   │   └── WeatherClient.java           # OpenWeatherMap API calls
│   │   ├── config/
│   │   │   └── RestTemplateConfig.java      # RestTemplate bean
│   │   ├── controller/
│   │   │   └── WeatherController.java       # REST endpoint
│   │   ├── dto/
│   │   │   ├── Coordinates.java             # lat/lon holder
│   │   │   ├── ForecastApiResponse.java     # Forecast API response DTO
│   │   │   ├── GeoResponse.java             # Geocoding API response DTO
│   │   │   ├── WeatherApiResponse.java      # Current weather API response DTO
│   │   │   └── WeatherResponse.java         # API response returned to client
│   │   ├── exception/
│   │   │   ├── ErrorResponse.java           # Standard error response body
│   │   │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   │   │   └── WeatherAppException.java     # Custom runtime exception
│   │   └── service/
│   │       └── WeatherService.java          # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/weatherApp/
        ├── controller/
        │   └── WeatherControllerTest.java   # MockMvc integration tests
        └── service/
            └── WeatherServiceTest.java      # Mockito unit tests
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven (or use the included `mvnw` wrapper)
- An [OpenWeatherMap API key](https://home.openweathermap.org/users/sign_up) (free tier works)

### Clone & Run

```bash
# Clone the repository
git clone <your-repo-url>
cd weatherApp

# Run with default API key (set in application.properties)
./mvnw spring-boot:run

# Or pass your own API key via environment variable (recommended)
WEATHER_API_KEY=your_api_key_here ./mvnw spring-boot:run
```

> **Windows (PowerShell):**
> ```powershell
> $env:WEATHER_API_KEY="your_api_key_here"; .\mvnw.cmd spring-boot:run
> ```

The application starts on **`http://localhost:8080`** by default.

---

## ⚙️ Configuration

| Property | Environment Variable | Default | Description |
|---|---|---|---|
| `weather.api.key` | `WEATHER_API_KEY` | *(set in properties)* | OpenWeatherMap API key |
| `server.port` | — | `8080` | Server port |

> ⚠️ **Security Note:** Never commit your real API key to source control. Use the `WEATHER_API_KEY` environment variable in production.

---

## 📡 API Reference

### Get Weather

```
GET /api/weather?pinCode={pinCode}&date={date}
```

#### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `pinCode` | `String` | ✅ | 6-digit Indian PIN code (e.g. `110001`) |
| `date` | `String` | ✅ | Date in `yyyy-MM-dd` format. Today or up to 5 days in the future. |

#### Example Request

```bash
curl "http://localhost:8080/api/weather?pinCode=110001&date=2026-06-02"
```

#### Success Response `200 OK`

```json
{
  "temperature": 38.5,
  "description": "haze",
  "date": "2026-06-02"
}
```

> Temperature is returned in **°Celsius**.

#### Routing Logic

| Date | API Used |
|---|---|
| Today | OpenWeatherMap Current Weather (`/data/2.5/weather`) |
| 1–5 days ahead | OpenWeatherMap 5-Day Forecast (`/data/2.5/forecast`) — entry closest to noon |
| Past date | ❌ 400 Bad Request |
| > 5 days ahead | ❌ 400 Bad Request |

---

## ❌ Error Handling

All errors return a consistent JSON body:

```json
{
  "message": "Human-readable error message",
  "status": 400,
  "timestamp": "2026-06-02T10:30:00"
}
```

| Scenario | HTTP Status |
|---|---|
| Missing `pinCode` or `date` parameter | `400 Bad Request` |
| Invalid date format (not `yyyy-MM-dd`) | `400 Bad Request` |
| Past date provided | `400 Bad Request` |
| Date more than 5 days in future | `400 Bad Request` |
| Invalid/unknown PIN code | `404 Not Found` |
| OpenWeatherMap API error | Mirrors upstream status |
| Unexpected server error | `500 Internal Server Error` |

---

## 🧪 Running Tests

```bash
# Run all tests
./mvnw test

# Windows
.\mvnw.cmd test
```

### Test Coverage

| Test Class | Tests | What's Covered |
|---|---|---|
| `WeatherControllerTest` | 5 | Valid requests, missing params, exception propagation |
| `WeatherServiceTest` | 8 | Today/forecast routing, null API responses, date validation edge cases |
| `WeatherAppApplicationTests` | 1 | Spring context loads successfully |

**Total: 14 tests — all passing ✅**

---

## 📝 License

This project is for demonstration purposes.

