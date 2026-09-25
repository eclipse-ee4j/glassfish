# EJB over HTTP: live call

A client that calls a remote EJB on a running GlassFish over the HTTP
transport, once with the built-in codec and once with Fory. It is run by
`.github/workflows/ejb-http-live.yml`.

## Why this exists

The module's own tests round-trip the transport over a real
`com.sun.net.httpserver` with the real thin client, which proves the wire
format and the dispatch. They run against a fake container, so there is a half
they cannot reach:

- the module's OSGi manifest, and whether the bundle really carries what it
  says it carries;
- the endpoint being mounted on the server's listener;
- the container resolving an application, module and bean name to a deployed
  bean;
- the deployment's own classloader on both sides of the codec.

The Fory gRPC live job found five defects, every one of them in that half and
none of them visible from a unit test. This job is the same argument applied
to the transport the rest of the stack is built on.

## The fixture

The bean is `compatibility/testapp`, the one all the live jobs deploy: the
built-in codec, Fory and the gRPC bridge answer calls to the *same* deployed
bean, so they cannot quietly be testing different things. The client depends
on that fixture rather than declaring its own copy of `demo.Greeter`, so the
two ends cannot describe different interfaces.

## The codec is named, not negotiated

`HttpEjbClient` drops to Java serialization when a peer cannot read the codec
it would have preferred - deliberately, and without saying so, because a call
that can be served beats a failure the caller can do nothing about. That is
right for an application and wrong for a test: a Fory run could report success
having sent no Fory at all. A client that *names* a codec is never walked back
(`HttpEjbClient.isUnsupportedCodec` refuses to retry when `config.codec()` is
set), so `-codec fory` either speaks Fory or fails.

## Running it by hand

With a GlassFish that already has the modules installed and the bean deployed:

```sh
mvn -q install -f compatibility/testapp/pom.xml
mvn -q package dependency:copy-dependencies -DoutputDirectory=target/deps \
    -f compatibility/ejb-http/client/pom.xml

java -cp "compatibility/ejb-http/client/target/classes:compatibility/ejb-http/client/target/deps/*" \
    demo.client.EjbHttpLiveClient \
    -target http://localhost:8080/glassfish-services \
    -codec fory -name Ada -expect 'Hello Ada'
```
