/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

@Beta
public interface MountEffectiveStatement extends UnknownEffectiveStatement<Empty, MountStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return OpenDaylightExtensionsStatements.MOUNT;
    }
}
