/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;

final class DerivedUint64Type extends AbstractRangeRestrictedDerivedType<Uint64TypeDefinition, Uint64>
        implements Uint64TypeDefinition {
    DerivedUint64Type(final Uint64TypeDefinition baseType, final QName qname, final Object defaultValue,
            final String description, final String reference, final Status status, final String units,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, qname, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    private DerivedUint64Type(final DerivedUint64Type original, final QName qname) {
        super(original, qname);
    }

    @Override
    DerivedUint64Type bindTo(final QName newQName) {
        return new DerivedUint64Type(this, newQName);
    }

    @Override
    public int hashCode() {
        return Uint64TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Uint64TypeDefinition.equals(this, obj);
    }
}
