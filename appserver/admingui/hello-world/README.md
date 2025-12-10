# Hello World Admin Console Plugin

This is an example plugin that demonstrates how to create Jakarta Faces (JSF) pages in the GlassFish Admin Console.

## Features

- Creates a "Hello World" navigation item in the admin console sidebar
- Displays a JSF page with content from a CDI bean
- Demonstrates modern Jakarta Faces integration with the legacy admin console

## Files

- `HelloWorldConsolePlugin.java` - HK2 service that registers the plugin. Needed to register the integration points in `console-config.xml`
- `console-config.xml` - Plugin configuration defining integration points. Adds `helloWorldNavNode.jsf` to the sidebar
- `helloWorldNavNode.jsf` - Navigation node for the sidebar, opens `hello.xhtml`
- `hello.xhtml` - Main JSF page displayed when clicking the navigation item
- `HelloWorldBean.java` - CDI bean that provides the message content

## Usage

1. Build GlassFish with this plugin included
2. Start GlassFish domain
3. Access the admin console at http://localhost:4848
4. Look for "Hello World" in the sidebar navigation
5. Click it to see the JSF page

## Tutorial

See the [Creating JSF Pages in GlassFish Admin Console](https://github.com/eclipse-ee4j/glassfish/wiki/admin-console-faces-tutorial.adoc) tutorial in GlassFish Wiki for a complete step-by-step tutorial on creating similar plugins.
