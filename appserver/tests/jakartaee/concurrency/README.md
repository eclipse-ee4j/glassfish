# Set of tests that extend Jakarta Concurrency TCK

This test suite is based on the Jakarta Concurrency TCK runner and contains additional tests on top of the TCK. These tests intentionally follow the structure of the TCK so that they can be easily donated to the TCK in the future.

The package `org.glassfish.main.jakartaee.tests.concurrent` in this module matches the package `ee.jakarta.tck.concurrent` in the TCK.

## Running the test suite

```
mvn verify
```

Run in the same way as the Jakarta Concurrency TCK runner. By default, GlassFish Full Profile edition is tested.
