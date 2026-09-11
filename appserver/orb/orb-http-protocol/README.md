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
| Stateful session beans | supported |

Looking up a stateful bean is what creates its session, so two lookups of
the same name are two conversations - the same thing an IIOP client gets,
and what an application written against the EJB semantics expects. The
session travels inside the reference the lookup returns, so the proxy is
already addressed to its own instance.

All of the above is exercised against a running GlassFish on every change,
over both codecs, rather than asserted here.

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

The caller's credential is checked against the server's realm - the same
check the web container makes for the same `Authorization` header. A
header names a user; it does not establish that the request came from
them, and believing it unchecked would let any caller assert any identity.
The subject the realm returns is the one installed, not an empty one: it
carries the caller's groups, and every authorization decision afterwards
reads them from there.

An absent credential and a rejected one are different answers. Absent is
anonymous, which is what an unsecured bean expects. Rejected is refused
with a 403: continuing with fewer rights than were asked for is still an
authorization decision, taken on a credential nobody accepted.

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

### What this changes outside the transport's own modules

Most of this work is new modules, which touch nothing that exists. Two
changes are not:

| Module | Change | Why |
| --- | --- | --- |
| `orb-connector` | `EjbContainerFacade` gains `createSession` and `removeSession` | the interface is shaped by IIOP, where a session key never has to be named because it rides inside a reference. A transport that is not IIOP has nowhere to hide it. |
| `ejb-container` | `BaseContainer` refuses both; `StatefulSessionContainer` implements them | only a stateful container has sessions. Every other one throws rather than returning a shared instance and letting a caller believe it holds a conversation. |

This matters for review and for deployment. It is no longer a change that
can be dropped into a released server as extra modules: those two have to
be the rebuilt ones, and the CI job replaces them for exactly that reason.

### Third-party dependencies

The Fory codec is the only part of this work that introduces third-party
code, and it is optional: nothing else here depends on it, and a build
that leaves the module out has no new dependency at all.

| Artifact | Version | License | Why |
| --- | --- | --- | --- |
| `org.apache.fory:fory-core` | 1.7.1 | Apache-2.0 | the codec |
| `org.codehaus.janino:janino` | 3.1.12 | BSD-3-Clause | compiles Fory's generated serializers |
| `org.codehaus.janino:commons-compiler` | 3.1.12 | BSD-3-Clause | required by janino |

Fory declares Guava and slf4j-api as optional; neither is used and
neither is embedded.

The janino entries are worth stating plainly rather than leaving to a
scan: they arrive transitively, they are **not** Apache-2.0, and they end
up inside the bundle. Fory needs janino to compile the serializers it
generates, which is where its speed comes from - a build without it
produces a bundle that works until the first object is encoded.

Both Apache-2.0 and BSD-3-Clause are on the Eclipse Foundation's approved
list, but this still needs a dependency review before the module can be
released, since none of these have been vetted for this project before.
What a review needs is above: the exact coordinates, the versions, the
licences, and the fact that all three are **embedded in the bundle** rather
than referenced - none of the three publish OSGi bundles, so dropping them
into `modules/` would leave them unresolvable.

Nothing else depends on any of this. A build that omits
`orb-http-codec-fory` has no new third-party dependency at all, and the
transport keeps working on the built-in codec.

#### A limitation, stated plainly

Fory compiles a serializer per type, and that is where most of its speed
comes from. Inside this server it does not work, and the reason is now
known rather than guessed at.

Fory defines each generated serializer in the class loader of the type it
serializes - the "neighbour" - and that generated class references Fory's
own runtime. On a class path one loader sees both. In a container the type
belongs to the application or to the protocol bundle while Fory is embedded
in the codec bundle, so the loader that owns the type cannot link what was
defined in it. Generation fails with "Create sequential serializer failed",
naming the type rather than the visibility behind it.

Three arrangements were measured directly:

| The type's loader | Code generation |
| --- | --- |
| cannot see Fory | fails |
| bridged - Fory handed a loader that sees both | fails |
| can see Fory | works |

The middle row is the useful one: bridging the loader *handed to* Fory
changes nothing, because the loader that matters is the type's own and Fory
selects it itself. Only the last row works, and it is reachable for this
transport's own types and not for an application's - an application bundle
cannot be made to import a codec it never asked for.

That is a property of Fory's strategy meeting OSGi's rules, not something
this module can work around.

Code generation is therefore off by default and the reflective path runs
instead: slower than generated code, still well ahead of Java
serialization, which is the comparison that matters for a codec adopted to
be faster than it. Where it does work - a plain client JVM, with an
ordinary class path and nothing to bridge - set

```
-Dorg.glassfish.orb.http.codec.fory.codegen=true
```

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
