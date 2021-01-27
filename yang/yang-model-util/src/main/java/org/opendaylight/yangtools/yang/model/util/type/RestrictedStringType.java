/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

final class RestrictedStringType extends AbstractLengthRestrictedType<StringTypeDefinition>
        implements StringTypeDefinition {
    private final @NonNull ImmutableList<PatternConstraint> patternConstraints;

    RestrictedStringType(final StringTypeDefinition baseType, final QName qname,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final @Nullable LengthConstraint lengthConstraints,
            final List<PatternConstraint> patternConstraints) {
        super(baseType, qname, unknownSchemaNodes, lengthConstraints);
        this.patternConstraints = ImmutableList.copyOf(patternConstraints);
    }

    private RestrictedStringType(final RestrictedStringType original, final QName qname) {
        super(original, qname);
        this.patternConstraints = original.patternConstraints;
    }

    @Override
    RestrictedStringType bindTo(final QName newQName) {
        return new RestrictedStringType(this, newQName);
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return patternConstraints;
    }

    @Override
    public int hashCode() {
        return StringTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return StringTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return StringTypeDefinition.toString(this);
    }
}
