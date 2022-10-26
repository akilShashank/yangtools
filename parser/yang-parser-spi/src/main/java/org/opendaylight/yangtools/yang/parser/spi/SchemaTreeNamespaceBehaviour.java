/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * {@link NamespaceBehaviour} handling {@link ParserNamespaces#schemaTree()}.
 */
// FIXME: 11.0.0: this contract seems to fall on the reactor side of things rather than parser-spi. Consider moving this
//                into yang-(parser-)reactor-api.
final class SchemaTreeNamespaceBehaviour<D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
        extends NamespaceBehaviour<QName, StmtContext<QName, D, E>> {
    SchemaTreeNamespaceBehaviour() {
        super(ParserNamespaces.schemaTree());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This method is analogous to {@link SchemaTreeAwareEffectiveStatement#findSchemaTreeNode(QName)}.
     */
    @Override
    public StmtContext<QName, D, E> getFrom(final NamespaceStorageNode storage, final QName key) {
        // Get the backing storage node for the requested storage
        final NamespaceStorageNode storageNode = globalOrStatementSpecific(storage);
        // Check try to look up existing node
        final StmtContext<QName, D, E> existing = storageNode.getFromLocalStorage(getIdentifier(), key);

        // An existing node takes precedence, if it does not exist try to request it
        return existing != null ? existing : requestFrom(storageNode, key);
    }

    @Override
    public Map<QName, StmtContext<QName, D, E>> getAllFrom(final NamespaceStorageNode storage) {
        // FIXME: 7.0.0: this method needs to be well-defined
        return null;
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final QName key, final StmtContext<QName, D, E> value) {
        final StmtContext<?, D, E> prev = globalOrStatementSpecific(storage).putToLocalStorageIfAbsent(getIdentifier(),
            key, value);

        if (prev != null) {
            throw new SourceException(value,
                "Error in module '%s': cannot add '%s'. Node name collision: '%s' already declared at %s",
                value.getRoot().rawArgument(), key, prev.argument(), prev.sourceReference());
        }
    }

    private static <D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
            StmtContext<QName, D, E> requestFrom(final NamespaceStorageNode storageNode, final QName key) {
        return storageNode instanceof OnDemandSchemaTreeStorageNode ondemand ? ondemand.requestSchemaTreeChild(key)
            : null;
    }

    private static NamespaceStorageNode globalOrStatementSpecific(final NamespaceStorageNode storage) {
        NamespaceStorageNode current = requireNonNull(storage);
        while (!isLocalOrGlobal(current.getStorageNodeType())) {
            current = verifyNotNull(current.getParentNamespaceStorage());
        }
        return current;
    }

    private static boolean isLocalOrGlobal(final StorageNodeType type) {
        return type == StorageNodeType.STATEMENT_LOCAL || type == StorageNodeType.GLOBAL;
    }
}