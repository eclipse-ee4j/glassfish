# Frequently Asked Questions About Eclipse GlassFish

* [What is Eclipse GlassFish?](#what-is-eclipse-glassfish)
* [What is Embedded GlassFish and when should I use it?](#what-is-embedded-glassfish-and-when-should-i-use-it)
* [Is Eclipse GlassFish open-source?](#is-eclipse-glassfish-open-source)
* [Is Eclipse GlassFish supported?](#is-eclipse-glassfish-supported)

## What is Eclipse GlassFish?

Eclipse GlassFish is an open-source Jakarta EE platform application server project started by Sun Microsystems, then sponsored by Oracle Corporation, and now living at the Eclipse Foundation.

## What is Embedded GlassFish and when should I use it?

Embedded GlassFish is a self-contained executable JAR that runs a full GlassFish instance without any installation or configuration. You start it with `java -jar glassfish-embedded-all.jar` and optionally pass applications and configuration on the command line.

Use Embedded GlassFish when you want to:

- Deploy to cloud environments or containers without managing a server installation
- Run Jakarta EE applications as self-contained microservices
- Embed GlassFish as a library directly inside your application
- Run integration tests against a real Jakarta EE runtime as part of your build

Embedded GlassFish is a fully supported and actively maintained feature since GlassFish 7.1.0, covered by the internal test suite, and suitable for production use.

## Is Eclipse GlassFish open-source?

Yes, Eclipse GlassFish is an open-source project made available under the terms of the [Eclipse Public License v. 2.0](http://www.eclipse.org/legal/epl-2.0). Find out more in the [Eclipse GlassFish NOTICE file](https://github.com/eclipse-ee4j/glassfish/blob/master/NOTICE.md).

## Can I contribute to the project?

Yes, the Eclipse GlassFish Project is open for contributions and your help is
greatly appreciated. You can report an [issue](https://github.com/eclipse-ee4j/glassfish/issues) with a bug, feedback, or an enhancement request. You can also contribute code or improve the documentation or the website. Find out more in the [Contributing to Eclipse GlassFish](CONTRIBUTING.md) page.

## Is Eclipse GlassFish supported?

Yes, there are multiple companies that provide Enterprise Support and other Professional Services. There's a list of those companies on the [Professional Services and Enterprise Support](support.md) page.