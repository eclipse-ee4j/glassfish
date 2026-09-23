# Fory gRPC compatibility smoke test

This fixture exercises the same Fory IDL shape that GlassFish publishes for a
unary remote EJB view. It generates language-native models and gRPC companions
with the official `foryc` compiler; it does not implement a second wire codec.

Requirements: `foryc`, Python or Go, and the matching gRPC runtime. Generate
the clients with:

```sh
./run-codegen.sh python
./run-codegen.sh go
```

The generated clients can then target a GlassFish endpoint once the server
adapter is enabled. The endpoint must use the service path
`/compatibility.greeter.Greeter/SayHello` and Fory gRPC payload encoding.
