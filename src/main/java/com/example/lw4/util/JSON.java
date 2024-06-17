package com.example.lw4.util;

import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.lang.NonNull;
import static org.springframework.util.ResourceUtils.toURL;


public class JSON {

  private final JsonNode node;

  private JSON(@NonNull JsonNode node) {
    this.node = node;
  }

  public static JSON get(String url) {
    var result = new StringBuilder();
    try {
      var conn = (HttpURLConnection)toURL(url).openConnection();
      conn.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null)
        result.append(line);
      rd.close();
      ObjectMapper mapper = new ObjectMapper();
      return new JSON(mapper.readTree(result.toString()));
    } catch (Exception ignored) {
      return null;
    }
  }

  public JSON obj(String key) {
    var val = node.get(key);
    return val != null && val.isContainerNode() && !val.isArray() ? new JSON(val) : null;
  }

  public ArrayList<JSON> arr(String key) {
    var val = node.get(key);
    if (val != null && val.isArray()) {
      var list = new ArrayList<JSON>();
      val.elements().forEachRemaining(x -> list.add(new JSON(x)));
      return list;
    }
    return null;
  }

  public JsonNode prop(String key) {
    var val = node.get(key);
    return val != null && val.isValueNode() ? val : null;
  }

}