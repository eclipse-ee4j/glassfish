# Eclipse GlassFish Documentation

The subdirectories of this directory contain the Eclipse GlassFish
website and documentation content, and the Maven projects to publish it.

The entire gh-pages content is published from here.
**DO NOT** update the gh-pages branch directly.

The `website` project contains the top level web site content.
The content is in `src/main/resources`.

The `parent` project effectively acts as an include file that contains
all the common jbake configuration that the other projects need.
(This project needs to be able to be installed with `mvn install`
and unfortunately I couldn't figure out how to do that without having
some valid jbake configuration in this project.
So, there's a dummy `jbake.properties` file but no real content.)

The `distribution` project combines all the separate documentation
project contents into a single file, to make it convenient for
the `publish` project to reference published versions of the
documentation for previous releases.

The `publish` project contains the configuration to publish the
entire web site.

Each of the documentation projects generates a jar file with the
content for that document, using jbake.
The content is installed with `mvn install`.
The content of all these jar files is combined into a single jar
file by the `distribution` project.
Only this combine jar file needs to be deployed to Maven.
The `publish` project collects all of these distribution jar files
for the current relase and previous releases  using the
`maven-dependency-plugin`, lays them out in the proper directory
structure, and uses the `maven-scm-publish-plugin` to publish
the content to the `gh-pages` branch of the GitHub repository.

For each of the documents, there's a current version published under
the `docs/SNAPSHOT` directory, as well as stable released versions
published under the `docs/<version>` directories.
There's a `docs/latest` symlink that refers to the latest (final) release.

When a GlassFish release is done, the documents need to be generated
and deployed, so that final version numbered documents will be in the
Maven repository forever in the distribution artifact, and can be
collected by the `publish` project to create the web site.
The `publish/pom.xml` file needs to be updated to add the newly released
version.
The `website/src/main/resources/docs/README.md` file needs to be updated
to add the previous versions to a "previous versions" section.

## To Do

* Add a Jenkins job that waits for changes and publishes the web site.
