/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

final class RestrictedIdentityrefType extends AbstractRestrictedType<IdentityrefTypeDefinition>
        implements IdentityrefTypeDefinition {
    RestrictedIdentityrefType(final IdentityrefTypeDefinition baseType, final SchemaPath path,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, unknownSchemaNodes);
    }

    @Override
    public Set<? extends IdentitySchemaNode> getIdentities() {
        return getBaseType().getIdentities();
    }

    @Override
    public int hashCode() {
        return IdentityrefTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return IdentityrefTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return IdentityrefTypeDefinition.toString(this);
    }
}