# Eclipse GlassFish

## About

Eclipse GlassFish is a [Jakarta EE compatible implementation](compatibility)
sponsored by the Eclipse Foundation. Eclipse GlassFish 5.1 is also Java EE 8 Compatible.

Building
--------

Prerequisites:

* JDK11+
* Maven 3.5.4+

Currently in the master branch artifacts are being pulled from OSSRH staging.

Run the full build:

`mvn -Pstaging install`

Locate the Zip distributions:
- appserver/distributions/glassfish/target/glassfish.zip
- appserver/distributions/web/target/web.zip

Locate staged distributions:
- appserver/distributions/glassfish/target/stage
- appserver/distributions/web/target/stage

Testing
--------


Testing QuickLook directly
--------------------------

Running Eclipse GlassFish QuickLook tests:

`mvn -f appserver/tests/quicklook/pom.xml test -Dglassfish.home=appserver/distributions/glassfish/target/stage/glassfish6/glassfish`

For more details, see [QuickLook_Test_Instructions](https://github.com/eclipse-ee4j/glassfish/blob/master/appserver/tests/quicklook/QuickLook_Test_Instructions.html)


Testing Full
------------

Build Eclipse GlassFish using the `gfbuild.sh` script, OR build as stated above and copy the distributions to the `bundles` folder using:

`./gfbuild.sh archive_bundles`

This will result in:

```
bundles
   glassfish.zip	
   nucleus-new.zip
   web.zip
```

Run tests using:

```
./gftest [name of test]
```


Where [name of test] is one or more off:

```
       "deployment_all" 
       "ejb_group_1" 
       "ejb_group_2" 
       "ejb_group_3" 
       "ejb_web_all" 
       "cdi_all" 
       "ql_gf_full_profile_all" 
       "ql_gf_nucleus_all" 
       "ql_gf_web_profile_all"
       "nucleus_admin_all"
       "jdbc_all"
       "batch_all"
       "persistence_all"
       "connector_group_1"
       "connector_group_2"
       "connector_group_3"
       "connector_group_4"
```

(note the project contains more than these tests, but they may not be up to date)

E.g.

```
./gftest deployment_all
```




Starting Eclipse GlassFish
------------------

`glassfish6/bin/asadmin start-domain`

Stopping Eclipse GlassFish
------------------

`glassfish6/bin/asadmin stop-domain`
