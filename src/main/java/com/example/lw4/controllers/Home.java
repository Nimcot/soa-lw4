package com.example.lw4.controllers;

import com.example.lw4.Lw4ApplicationConfig;
import com.example.lw4.util.JSON;
import com.example.lw4.dto.system.*;
import com.example.lw4.dto.tracking.*;
import com.example.lw4.dto.weather.*;

import java.util.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;


@Controller
public class Home {

  private final Lw4ApplicationConfig config;
  private final String geoSearchUrl;
  private final String currentWeatherUrl;
  private final String forecastWeatherUrl;
  private final String nearbyCitiesUrl;
  private final String[] dayNames = {
      "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
  };
  private final String[] dayShortNames = {
      "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
  private final String[] monthNames = {
      "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
  private final String[] directionNames = {
      "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
  };

  public Home(Lw4ApplicationConfig config) {
    this.config = config;
    this.geoSearchUrl = "https://" + config.get("geonames.domain") +
        "/searchJSON?q=%s&maxRows=1&username=" + config.get("geonames.username");
    this.currentWeatherUrl = "https://" + config.get("open-weather.domain") +
        "/data/2.5/weather?units=metric&lat=%s&lon=%s&appid=" + config.get("open-weather.app-id");
    this.forecastWeatherUrl = "https://" + config.get("open-weather.domain") +
        "/data/2.5/forecast?units=metric&lat=%s&lon=%s&appid=" + config.get("open-weather.app-id");
    this.nearbyCitiesUrl = "https://" + config.get("geonames.domain") + "/findNearbyPlaceNameJSON?style=short" +
        "&cities=cities15000&radius=300&maxRows=500&lat=%s&lng=%s&username=" + config.get("geonames.username");
  }

  @RequestMapping({"", "/"})
  public String getForecast(@RequestParam("lat") Optional<Double> lat, @RequestParam("city") Optional<String> city,
                            @RequestParam("lon") Optional<Double> lon, Model model) {
    try {
      // Geo-decoding query to coordinates
      if (lat.isEmpty() || lon.isEmpty()) {
        model.addAttribute("usedSearch", city.isPresent());
        String query = city.orElse(config.get("geonames.search.default")).trim();
        JSON json;
        if (!query.matches("[a-zA-Z][a-zA-Z, ]{0," + config.get("geonames.search.max-length") + '}')
            || (json = JSON.get(String.format(geoSearchUrl, query))) == null)
          return "page404";
        json = json.arr("geonames").get(0);
        lat = Optional.of(json.prop("lat").asDouble());
        lon = Optional.of(json.prop("lng").asDouble());
      } else {
        model.addAttribute("usedSearch", true);
      }

      // Retrieving and parsing services' json into DTO
      JSON current, forecast;
      if ((current = JSON.get(String.format(currentWeatherUrl, lat.get(), lon.get()))) == null
          || (forecast = JSON.get(String.format(forecastWeatherUrl, lat.get(), lon.get()))) == null)
        return "page404";
      var now = new Date().getTime();
      var sunriseUnix = current.obj("sys").prop("sunrise").asLong();
      var sunsetUnix = current.obj("sys").prop("sunset").asLong();
      var offsetUnix = current.prop("timezone").asLong();
      var weatherForecast = new LongForecast() {{
        var locale = new Locale("en", current.obj("sys").prop("country").asText());
        location = new Location() {{
          name = current.prop("name").asText();
          country = locale.getDisplayCountry(locale);
          coordinates = new Coordinates() {{
            latitude = current.obj("coord").prop("lat").asDouble();
            longitude = current.obj("coord").prop("lon").asDouble();
          }};
        }};
        var timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        timeFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        day = new Day() {{
          sunrise = timeFormat.format(new Date((sunriseUnix + offsetUnix) * 1000));
          sunset = timeFormat.format(new Date((sunsetUnix + offsetUnix) * 1000));
          duration = timeFormat.format(new Date((sunsetUnix - sunriseUnix) * 1000));
        }};
        daily = new ArrayList<>();
      }};
      var dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
      var shortDateFormat = new SimpleDateFormat("MMM d", Locale.US);
      var timeFormat = new SimpleDateFormat("K a", Locale.US);
      dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
      shortDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
      timeFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
      weatherForecast.daily.add(new Daily() {{
        weekday = sunriseUnix * 1000 < now && now < sunsetUnix * 1000 ? "TODAY" : "TONIGHT";
        weekdayShort = "TODAY";
        date = dateFormat.format(now + offsetUnix * 1000);
        var dateTime = LocalDateTime.ofEpochSecond(now / 1000 + offsetUnix, 0, ZoneOffset.UTC);
        dateShort = monthNames[dateTime.getMonthValue() - 1] + ' ' + dateTime.getDayOfMonth();
        icon = current.arr("weather").get(0).prop("icon").asText();
        condition = current.arr("weather").get(0).prop("main").asText();
        description = current.arr("weather").get(0).prop("description").asText();
        temperature = (int) Math.round(current.obj("main").prop("temp").asDouble());
        feelsLike = (int) Math.round(current.obj("main").prop("feels_like").asDouble());
        hourly = new ArrayList<>();
      }});
      weatherForecast.daily.get(0).hourly.add(new Hourly() {{
        dt = current.prop("dt").asLong();
        time = "Now";
        icon = weatherForecast.daily.get(0).icon;
        condition = weatherForecast.daily.get(0).condition;
        description = weatherForecast.daily.get(0).description;
        temperature = weatherForecast.daily.get(0).temperature;
        feelsLike = weatherForecast.daily.get(0).feelsLike;
        pressure = current.obj("main").prop("pressure").asInt();
        humidity = current.obj("main").prop("humidity").asInt();
        cloudiness = current.obj("clouds").prop("all").asInt();
        visibility = current.prop("visibility").asInt();
        ;
        wind = new Wind() {{
          speed = current.obj("wind").prop("speed").asDouble();
          direction = directionNames[(int) Math.floor((current.obj("wind").prop("deg").asInt() / 22.5) + 0.5) % 16];
        }};
      }});
      var startOfTodayUnix = LocalDateTime
          .ofEpochSecond(current.prop("dt").asLong() + offsetUnix, 0, ZoneOffset.UTC)
          .toLocalDate()
          .atStartOfDay()
          .toEpochSecond(ZoneOffset.UTC);
      for (var hour : forecast.arr("list")) {
        if (current.prop("dt").asLong() > hour.prop("dt").asLong())
          continue;
        var index = (short) ((hour.prop("dt").asLong() + offsetUnix - startOfTodayUnix) / 86400);
        if (index == 5) break;
        if (index >= weatherForecast.daily.size())
          weatherForecast.daily.add(new Daily() {{
            var dateTime = LocalDateTime.ofEpochSecond(hour.prop("dt").asLong() + offsetUnix, 0, ZoneOffset.UTC);
            weekday = dayNames[dateTime.getDayOfWeek().get(ChronoField.DAY_OF_WEEK)];
            weekdayShort = dayShortNames[dateTime.getDayOfWeek().get(ChronoField.DAY_OF_WEEK)];
            date = dateFormat.format((hour.prop("dt").asLong() + offsetUnix) * 1000);
            dateShort = monthNames[dateTime.getMonthValue() - 1] + ' ' + dateTime.getDayOfMonth();
            hourly = new ArrayList<>();
          }});
        weatherForecast.daily.get(index).hourly.add(new Hourly() {{
          dt = hour.prop("dt").asLong();
          time = timeFormat.format(new Date((hour.prop("dt").asLong() + offsetUnix) * 1000));
          icon = hour.arr("weather").get(0).prop("icon").asText();
          condition = hour.arr("weather").get(0).prop("main").asText();
          description = hour.arr("weather").get(0).prop("description").asText();
          temperature = (int) Math.round(hour.obj("main").prop("temp").asDouble());
          feelsLike = (int) Math.round(hour.obj("main").prop("feels_like").asDouble());
          pressure = hour.obj("main").prop("pressure").asInt();
          humidity = hour.obj("main").prop("humidity").asInt();
          cloudiness = hour.obj("clouds").prop("all").asInt();
          visibility = hour.prop("visibility") != null ? hour.prop("visibility").asInt() : 0;
          wind = new Wind() {{
            speed = hour.obj("wind").prop("speed").asDouble();
            direction = directionNames[(int) Math.floor((hour.obj("wind").prop("deg").asInt() / 22.5) + 0.5) % 16];
          }};
        }});
      }
      for (var i = 1; i < weatherForecast.daily.size(); i++) {
        var closest = weatherForecast.daily.get(i).hourly.stream()
            .min(Comparator.comparingInt(h -> ((int) ((h.dt - current.prop("dt").asLong()) % 86000)))).get();
        weatherForecast.daily.get(i).icon = closest.icon;
        weatherForecast.daily.get(i).condition = closest.condition;
        weatherForecast.daily.get(i).description = closest.description;
        weatherForecast.daily.get(i).temperature = closest.temperature;
        weatherForecast.daily.get(i).feelsLike = closest.feelsLike;
      }
      // ->
      model.addAttribute("weatherForecast", weatherForecast);
    } catch (Exception ignored) {
      ignored.printStackTrace();
      return "page404";
    }
    try {
      double latitude = lat.get(), longitude = lon.get();
      var nearbyForecast = new ArrayList<ShortForecast>();
      JSON.get(String.format(nearbyCitiesUrl, latitude, longitude)).arr("geonames").stream().filter(x -> {
        var code = x.prop("fcode").asText();
        return code.equals("PPLC") || code.equals("PPLA") || code.equals("PPLA2");
      }).skip(2).limit(3).forEach(g -> {
        var json = JSON.get(String.format(currentWeatherUrl, g.prop("lat").asText(), g.prop("lng").asText()));
        nearbyForecast.add(new ShortForecast() {{
          location = new Location() {{
            name = json.prop("name").asText();
            var locale = new Locale("en", json.obj("sys").prop("country").asText());
            country = locale.getDisplayCountry(locale);
            coordinates = new Coordinates() {{
              latitude = json.obj("coord").prop("lat").asDouble();
              longitude = json.obj("coord").prop("lon").asDouble();
            }};
          }};
          icon = json.arr("weather").get(0).prop("icon").asText();
          temperature = (int) Math.round(json.obj("main").prop("temp").asDouble());
          feelsLike = (int) Math.round(json.obj("main").prop("feels_like").asDouble());
        }});
      });
      // ->
      model.addAttribute("nearbyForecast", nearbyForecast);
    } catch (Exception ignored) {
      model.addAttribute("nearbyForecast", new ArrayList<ShortForecast>());
    }
    return "index";
  }

}