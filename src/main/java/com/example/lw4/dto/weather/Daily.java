package com.example.lw4.dto.weather;

import java.util.ArrayList;


public class Daily {

  public String weekday;
  public String weekdayShort;
  public String date;
  public String dateShort;
  public String icon;
  public String condition ;
  public String description;
  public int temperature;
  public int feelsLike;
  public ArrayList<Hourly> hourly;

}