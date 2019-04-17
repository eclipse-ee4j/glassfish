package com.sun.enterprise.deployment;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnvironmentPropertyTest {

  @Test
  public void constructedEnvironmentPropertyShouldHaveKnownValue() {
    EnvironmentProperty ep = new EnvironmentProperty("name", "value", "description");

    assertEquals("value", ep.getValue());
  }

  @Test
  public void constructedEnvironmentPropertyShouldHaveValue() {
    EnvironmentProperty ep = new EnvironmentProperty("name", "value", "description");

    assertTrue(ep.hasAValue());
  }
}
