/*******************************************************************************
 * Copyright (c) 2016 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.server.cluster.serialization;

import static org.junit.Assert.assertEquals;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.leshan.Link;
import org.eclipse.leshan.LwM2m;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.junit.Test;

public class ClientUpdateSerDesTest {

    @Test
    public void ser_and_des_are_equals() throws Exception {
        Link[] objs = new Link[2];
        Map<String, Object> att = new HashMap<>();
        att.put("ts", 12);
        att.put("rt", "test");
        objs[0] = new Link("/0/1024/2", att);
        objs[1] = new Link("/0/2");

        RegistrationUpdate ru = new RegistrationUpdate("myId", Inet4Address.getByName("127.0.0.1"),
                LwM2m.DEFAULT_COAP_PORT, 60000l, null, BindingMode.U, objs);

        byte[] ser = RegistrationUpdateSerDes.bSerialize(ru);
        RegistrationUpdate ru2 = RegistrationUpdateSerDes.deserialize(ser);

        assertEquals(ru, ru2);
    }
}
