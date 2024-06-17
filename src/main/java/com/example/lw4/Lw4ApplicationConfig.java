package com.example.lw4;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;


@Component
@PropertySource("classpath:application.properties")
public class Lw4ApplicationConfig {

  private final Environment env;

  public Lw4ApplicationConfig(Environment env) {
    this.env = env;
  }

  public String get(String key) {
    return env.getProperty(key);
  }

}