#!/bin/bash

echo "starting sendmail..."
/usr/sbin/sendmail -bd -q1h

if [ ! -z "${WORKSPACE}" ] ; then
  mkdip -p ${WORKSPACE}
  chmod -R ugo+rwx ${WORKSPACE}
fi

exec "$@"