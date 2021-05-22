# Test certificate

The certificate required for the tests was created like this:

    openssl genrsa -out server.key 4096
    openssl req -new -sha256 -key server.key -out server.csr \
        -subj "/C=BE/L=Brussels/O=Eclipse Foundation/OU=Eclipse Glassfish/CN=localhost"
    openssl x509 -req -sha256 -days 3650 -in server.csr -signkey server.key -out server.crt
    openssl pkcs12 -in server.crt -inkey server.key -export -out server.p12 -name s1as -password pass:changeit
    keytool -importkeystore \
        -srckeystore server.p12 -srcstoretype PKCS12 -srcstorepass changeit \
        -destkeystore keystore.jks -deststorepass changeit
    keytool -import -noprompt -keystore cacerts.jks -storepass changeit -alias s1as -file server.crt
