# A JBake project template

## About JBake

JBake is a static site generator, it's inspired from jekyll and written
in java.  The basic idea is to have templates for the structure of the
page, and the body generated from asciidoc content.

## Pre requisites

- Maven
- JDK8+

Deploying to Github will require password less authentication.

This is done by exporting your SSH public key into your Github account.

## Build the site locally

The site is generated under target/staging.

Open file:///PATH_TO_PROJECT_DIR/target/staging in a browser to view the site.

```
mvn generate-resources
```

Or you can invoke the JBake plugin directly.

```
mvn jbake:build
```

### Rebuild the site on changes

```
mvn jbake:watch
```

If you keep this command running, changes to the sources will be
detected and the site will be rendered incrementally.

This is convenient when writing content.

### Serve the site locally

```
mvn jbake:serve
```

If a webserver is required (e.g. absolute path are used), this command
will start a webserver (jetty) at http://localhost:8820.  It will also
watch for changes and rebuild incrementally.

## Deploy the site to Github Pages

```
mvn deploy
```

## Produce a zip file for download

To produce a zip file containing the generated html files, use:

```
mvn package
```

When making a release on GitHub, this zip file should be added to the release.

## Links

- [JBake maven plugin documentation](https://github.com/Blazebit/jbake-maven-plugin)
- [JBake documentation](http://jbake.org/docs/2.5.1)
- [Freemarker documentation](http://freemarker.org/docs)
- [AsciiDoc User Guide](http://asciidoc.org/userguide.html)
- [Asciidoctor quick reference](http://asciidoctor.org/docs/asciidoc-syntax-quick-reference)
