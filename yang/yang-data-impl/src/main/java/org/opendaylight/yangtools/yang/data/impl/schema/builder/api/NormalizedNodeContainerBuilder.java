/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface NormalizedNodeContainerBuilder<K extends PathArgument, CK extends PathArgument,
        CV extends NormalizedNode, P extends NormalizedNode> extends NormalizedNodeBuilder<K, Collection<CV>, P> {

    @Override
    NormalizedNodeContainerBuilder<K, CK, CV, P> withNodeIdentifier(K nodeIdentifier);

    @Override
    NormalizedNodeContainerBuilder<K, CK, CV, P> withValue(Collection<CV> value);

    @NonNull NormalizedNodeContainerBuilder<K, CK, CV, P> addChild(CV child);

    @NonNull NormalizedNodeContainerBuilder<K, CK, CV, P> removeChild(CK key);
}
