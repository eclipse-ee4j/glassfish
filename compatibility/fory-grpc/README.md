# Fory gRPC cross-language client

This fixture holds the same Fory IDL shape that GlassFish publishes for a unary
remote EJB view, including explicit message ids: GlassFish derives them from the
message name so that they survive a redeployment, and the Fory compiler honours
them rather than deriving its own.

`run-codegen.sh` generates language-native models and their gRPC companions with
the official `foryc` compiler:

```sh
./run-codegen.sh go
./run-codegen.sh python
```

`client/` is a Go client built on those generated sources. It sends and receives
Fory-encoded messages through the generated `CodecV2`, which puts
`application/grpc+fory` on the wire, and reads the status from the trailers like
any gRPC client.

One thing it does not take from the generated code is the method path. A
generated stub calls the canonical `/<package>.<Service>/<Method>`, while
GlassFish mounts its endpoints under a context path, so the path is a flag:

```sh
go build ./client
./client -target localhost:8080 \
         -path /glassfish-services/fory/glassfish.<app>.<module>.<bean>_<View>/SayHello \
         -name Ada -expect "Hello Ada"
```

The `.fdl` for a deployed view is published under `/.well-known/ejb/...fdl`, so a
client can be generated from the running server.
