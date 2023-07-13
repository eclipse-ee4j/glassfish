package org.glassfish.main.test.app.ejb;

import jakarta.ejb.Stateless;

import java.io.Serializable;

@Stateless
public class EchoServiceEJB implements EchoServiceLocal, EchoServiceRemote, Serializable {
    @Override
    public String echo(String message) {
        return message;
    }
}
