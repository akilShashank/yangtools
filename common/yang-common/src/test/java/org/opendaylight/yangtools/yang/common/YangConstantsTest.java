/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class YangConstantsTest {
    @Test
    void testYangConstants() {
        final var uriYang = YangConstants.RFC6020_YANG_NAMESPACE;
        final var uriYin = YangConstants.RFC6020_YIN_NAMESPACE;
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:1"), uriYang);
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:yin:1"), uriYin);
        assertEquals(QNameModule.of(uriYang).intern(), YangConstants.RFC6020_YANG_MODULE);
        assertEquals(QNameModule.of(uriYin).intern(), YangConstants.RFC6020_YIN_MODULE);
    }
}
