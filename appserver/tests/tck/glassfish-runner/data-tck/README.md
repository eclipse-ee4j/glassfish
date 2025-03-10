Sample runner repo Jakarta Data 1.0 TCK against Hibernate Data Repositories
========================================
A sample runner for validating the Hibernate Data Repositories implementation against the Jakarta Data 1.0 TCK.

This uses the Hibernate ORM 6.6.0.Alpha1 release

## Dependencies:
### Java SE
The Java SE version in use needs to be 17 or higher.

### Jakarta Data API and TCK 1.0.0
1. download https://www.eclipse.org/downloads/download.php?file=/ee4j/data/jakartaee/staged/eftl/data-tck-1.0.0.zip
1. unizip data-tck-1.0.0.zip
1. cd data-tck-1.0.0/artifacts
2. bash artifact-install.sh

### Jakarta Data Tools Fork
1. cd tools
2. mvn -Pstaging install

## Build the augmented TCK test jar in this repo
1. cd testjar
1. mvn -Pstaging install

## Running the TCK in GlassFish
1. cd runner-web
2. mvn -Pstaging -Pinstall-glassfish clean process-sources
3. mvn -Pstaging test
