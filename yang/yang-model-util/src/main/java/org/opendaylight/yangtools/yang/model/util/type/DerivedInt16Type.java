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
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;

final class DerivedInt16Type extends AbstractRangeRestrictedDerivedType<Int16TypeDefinition, Short>
        implements Int16TypeDefinition {
    DerivedInt16Type(final Int16TypeDefinition baseType, final QName qname, final Object defaultValue,
            final String description, final String reference, final Status status, final String units,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, qname, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    private DerivedInt16Type(final DerivedInt16Type original, final QName qname) {
        super(original, qname);
    }

    @Override
    DerivedInt16Type bindTo(final QName newQName) {
        return new DerivedInt16Type(this, newQName);
    }

    @Override
    public int hashCode() {
        return Int16TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Int16TypeDefinition.equals(this, obj);
    }
}
