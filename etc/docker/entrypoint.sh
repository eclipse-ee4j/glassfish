#!/bin/bash

echo "starting sendmail..."
/usr/sbin/sendmail -bd -q1h

exec "$@"