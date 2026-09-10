# Enterprise Beans over HTTP

An HTTP transport for remote Enterprise Bean invocation and JNDI lookup,
alongside the existing IIOP one. IIOP is not changed, deprecated, or
competed with: it keeps working exactly as before, and this is an
additional way to reach the same beans.

The reason to want one is narrow and practical. IIOP needs a dedicated
port, it does not survive most load balancers and reverse proxies
unaltered, and it is the first thing a corporate network blocks. HTTP is
already open, already terminated by the front end, and already
understood by every piece of infrastructure in between.

## Migrating an existing client

The bean does not change. The remote interface does not change. The
lookup and the calls do not change. What changes is the JNDI
configuration - two properties.

Over IIOP:

```java
Hashtable<String, String> environment = new Hashtable<>();
environment.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.enterprise.naming.SerialInitContextFactory");
environment.put("org.omg.CORBA.ORBInitialHost", "localhost");
environment.put("org.omg.CORBA.ORBInitialPort", "3700");

InitialContext context = new InitialContext(environment);
```

Over HTTP:

```java
Hashtable<String, String> environment = new Hashtable<>();
environment.put(Context.INITIAL_CONTEXT_FACTORY,
        "org.glassfish.orb.http.client.HttpInitialContextFactory");
environment.put(Context.PROVIDER_URL,
        "http://localhost:8080/glassfish-services");

InitialContext context = new InitialContext(environment);
```

Everything after that point is the same code:

```java
Greeter greeter = (Greeter) context.lookup("java:global/app/module/GreeterBean!org.example.Greeter");
String reply = greeter.greet("world");
```

This is not an illustration written to look tidy. The transport is
exercised by two client programs that differ only in the block above and
then call the *same* method - one source file, not a copy - so anything
that behaved differently between the two would show up as a failure
rather than as prose.

For HTTP/2, add:

```java
environment.put("org.glassfish.orb.http.http2", "true");
```

The protocol on the wire is unchanged; only the HTTP version carrying it
differs. Use `https://` in the provider URL and the client negotiates
h2 via ALPN.

### What is supported

| Capability | Status |
| --- | --- |
| Stateless and singleton beans | supported |
| Business interface and 2.x home/component views | supported |
| Asynchronous methods returning `Future` | supported, including HTTP 202 |
| Application exceptions, including `inherited` | supported, semantics match IIOP |
| JNDI lookup, list, bind and the rest | supported |
| Distributed transactions | supported on a separate branch |
| Stateful session creation | **not yet wired to the container** |

The last row is the honest one: the client, the protocol and the server
dispatcher all handle stateful sessions, but the GlassFish adapter
cannot yet create one, because the container exposes session creation
through the 2.x home view and not through the EJB 3 business view. A
call that needs one is refused with a clear reason rather than silently
returning a shared instance.

## Choosing a codec without changing the application

Every content type on the wire carries a codec token
(`application/x-gf-<codec>-<kind>; version=<n>`), so an endpoint can
serve several codecs at once. Which codec is used is decided by what is
on the class path, not by the application:

- `jser` - Java serialization. Built in, always present, and the codec
  two peers fall back to when they share nothing else.
- `fory` - [Apache Fory](https://fory.apache.org/), in the optional
  `orb-http-codec-fory` module.

Adding the Fory module to the class path is the entire change. No
import, no property, no configuration file: the codec is found through
`META-INF/services`, ranked above the built-in one, and used. Inside the
GlassFish OSGi runtime this works because GlassFish already ships Aries
SPI-Fly, which makes a provider in another bundle visible to
`ServiceLoader`.

The two ends do not have to agree in advance, and the choice is made per
request rather than per server. One endpoint serves the same bean to a
client using `jser` and a client using `fory` at the same time: the codec
is read from the request's content type, and the reply is written in that
same codec, so a client is never handed something it did not ask to
decode. A naming lookup is a GET with no body, so there the client states
its preference in `Accept`; a client that states nothing gets the
server's default, which is how an older client that knows one codec keeps
working untouched. If the server cannot read what the client
offered, it says so, and the client drops to `jser` and retries once,
remembering the outcome so the failed round trip is paid once rather
than per call. Adding a codec to one side can therefore never break a
peer that lacks it.

### Semantics are preserved, not just encoding

A faster codec that quietly changed what an application observes would
be a bug, not an optimisation. Remote calls already depend on Java
serialization tracking object identity, closing cycles, and skipping
`transient` fields. The Fory codec is configured to match on all three,
and each is pinned by a test rather than assumed.

### Security

Swapping the codec changes the encoding and nothing about what is
allowed to be decoded. The same `ObjectInputFilter` that guards Java
serialization guards Fory: one policy, both codecs. Class registration
is deliberately not required - an application cannot reasonably be asked
to enumerate its own types for a codec it never opted into - so the
filter takes that duty instead.

Reading and writing use separate codec instances, which is a security
property rather than tidiness: a shared instance caches what it has
resolved, so a class written on the way out would already be resolved by
the time an attacker names it on the way in, and the inbound check would
never run.

## Module map

| Module | Contains |
| --- | --- |
| `orb-http-protocol` | the wire format, routes, codec SPI and discovery |
| `orb-http-client` | the client, the JNDI provider, the proxies |
| `orb-http-server` | the dispatchers, independent of any container |
| `orb-http-glassfish` | the GlassFish adapter and Grizzly endpoint |
| `orb-http-codec-fory` | the optional Apache Fory codec |

`orb-http-server` deliberately knows nothing about GlassFish: it talks
to `ContainerBridge`, `NamingBridge` and `SecurityBridge`. That is what
keeps the protocol testable without a server and what would let another
container reuse it.
