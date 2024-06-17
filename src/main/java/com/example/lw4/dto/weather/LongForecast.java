package com.example.lw4.dto.weather;

import com.example.lw4.dto.system.Day;
import com.example.lw4.dto.tracking.Location;

import java.util.ArrayList;


public class LongForecast {

  public Location location;
  public Day day;
  public ArrayList<Daily> daily;

}