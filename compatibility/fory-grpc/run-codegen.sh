#!/bin/sh
set -eu

language=${1:?usage: $0 python|go}
root=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
out="$root/generated/$language"
mkdir -p "$out"

case "$language" in
  python)
    command -v foryc >/dev/null 2>&1 || {
      echo "foryc is required (install the Apache Fory compiler)" >&2
      exit 2
    }
    foryc "$root/greeter.fdl" --python_out="$out" --grpc
    ;;
  go)
    command -v foryc >/dev/null 2>&1 || {
      echo "foryc is required (install the Apache Fory compiler)" >&2
      exit 2
    }
    foryc "$root/greeter.fdl" --go_out="$out" --grpc
    ;;
  *)
    echo "unsupported language: $language" >&2
    exit 2
    ;;
esac

echo "generated $language Fory gRPC client sources under $out"
