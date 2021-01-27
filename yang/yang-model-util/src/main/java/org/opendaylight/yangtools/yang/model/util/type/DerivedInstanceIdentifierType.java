/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

final class DerivedInstanceIdentifierType extends AbstractDerivedType<InstanceIdentifierTypeDefinition>
        implements InstanceIdentifierTypeDefinition {
    private final boolean requireInstance;

    DerivedInstanceIdentifierType(final InstanceIdentifierTypeDefinition baseType, final QName qname,
            final Object defaultValue, final String description, final String reference, final Status status,
            final String units, final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final boolean requireInstance) {
        super(baseType, qname, defaultValue, description, reference, status, units, unknownSchemaNodes);
        this.requireInstance = requireInstance;
    }

    private DerivedInstanceIdentifierType(final DerivedInstanceIdentifierType original, final QName qname) {
        super(original, qname);
        this.requireInstance = original.requireInstance;
    }

    @Override
    DerivedInstanceIdentifierType bindTo(final QName newQName) {
        return new DerivedInstanceIdentifierType(this, newQName);
    }

    @Override
    public boolean requireInstance() {
        return requireInstance;
    }

    @Override
    public int hashCode() {
        return InstanceIdentifierTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return InstanceIdentifierTypeDefinition.equals(this, obj);
    }
}
