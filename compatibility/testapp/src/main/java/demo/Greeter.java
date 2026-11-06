/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package demo;

import jakarta.ejb.Remote;

/** One method with a portable signature, which is what the IDL generator accepts. */
@Remote
public interface Greeter {
    String sayHello(String name);
}
