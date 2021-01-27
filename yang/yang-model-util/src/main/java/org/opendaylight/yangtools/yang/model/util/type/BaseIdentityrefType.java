/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

final class BaseIdentityrefType extends AbstractBaseType<IdentityrefTypeDefinition>
        implements IdentityrefTypeDefinition {
    private final @NonNull Set<? extends IdentitySchemaNode> identities;

    BaseIdentityrefType(final QName qname, final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final Set<? extends IdentitySchemaNode> identities) {
        super(qname, unknownSchemaNodes);
        this.identities = requireNonNull(identities);
    }

    private BaseIdentityrefType(final BaseIdentityrefType original, final QName qname) {
        super(original, qname);
        this.identities = original.identities;
    }

    @Override
    BaseIdentityrefType bindTo(final QName newQName) {
        return new BaseIdentityrefType(this, newQName);
    }

    @Override
    public Set<? extends IdentitySchemaNode> getIdentities() {
        return identities;
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
