/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Writer;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A generated text file with a body is available as a {@link CharSequence}.
 */
@NonNullByDefault
final class CharSeqGeneratedTextFile extends AbstractGeneratedTextFile {
    private final CharSequence body;

    CharSeqGeneratedTextFile(final GeneratedFileLifecycle lifecycle, final CharSequence body) {
        super(lifecycle);
        this.body = requireNonNull(body);
    }

    @Override
    protected void writeBody(final Writer output) throws IOException {
        output.append(body);
    }
}
