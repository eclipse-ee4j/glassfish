// Calls a Fory gRPC endpoint published by GlassFish for a remote EJB view.
//
// The models and the codec come from the code the Fory compiler generates for
// greeter.fdl; only the method path is supplied here. The generated stub calls
// the canonical gRPC path, "/<package>.<Service>/<Method>", while GlassFish
// mounts its endpoints under a context path, so the path is passed in. A gRPC
// method name is just the HTTP/2 :path, so invoking it directly is enough.
//
// Usage:
//
//	fory-grpc-client -target host:port -path /glassfish-services/fory/<service>/SayHello -name Ada
package main

import (
	"context"
	"flag"
	"fmt"
	"os"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	greeter "compatibility/fory-grpc/generated/go/greeter"
)

func main() {
	target := flag.String("target", "localhost:8080", "host:port of the GlassFish HTTP listener")
	path := flag.String("path", "/compatibility.greeter.Greeter/SayHello", "gRPC method path to invoke")
	name := flag.String("name", "Ada", "value to send")
	expect := flag.String("expect", "", "fail unless the reply equals this")
	timeout := flag.Duration("timeout", 10*time.Second, "call timeout")
	flag.Parse()

	conn, err := grpc.NewClient(*target, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		fail("dial %s: %v", *target, err)
	}
	defer conn.Close()

	ctx, cancel := context.WithTimeout(context.Background(), *timeout)
	defer cancel()

	request := &greeter.SayHelloRequest{Value: *name}
	response := new(greeter.SayHelloResponse)
	// ForceCodecV2 puts "application/grpc+fory" on the wire, which is what the
	// GlassFish adapter answers to; the status comes back in the trailers.
	if err := conn.Invoke(ctx, *path, request, response, grpc.ForceCodecV2(greeter.CodecV2{})); err != nil {
		fail("invoke %s: %v", *path, err)
	}

	fmt.Println(response.Value)
	if *expect != "" && response.Value != *expect {
		fail("expected %q, got %q", *expect, response.Value)
	}
}

func fail(format string, args ...any) {
	fmt.Fprintf(os.Stderr, format+"\n", args...)
	os.Exit(1)
}
