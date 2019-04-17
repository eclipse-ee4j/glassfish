package com.sun.enterprise.deployment;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvironmentPropertyTest {

  @Test
  public void constructedEnvironmentPropertyShouldHaveKnownValue() {
    EnvironmentProperty ep = new EnvironmentProperty("name", "value", "description");

    assertEquals("value", ep.getValue());
  }

}
