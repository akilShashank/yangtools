/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Base64;

abstract sealed class AbstractValueNode<N extends ValueNode<V>, V> extends AbstractNormalizedSimpleValueNode<N, V>
        implements ValueNode<V> permits AbstractLeafNode, AbstractLeafSetEntryNode {
    @Override
    final Object toStringBody() {
        final var body = body();
        return body instanceof byte[] bytes ? "b64:" + Base64.getEncoder().encodeToString(bytes) : body;
    }
}
