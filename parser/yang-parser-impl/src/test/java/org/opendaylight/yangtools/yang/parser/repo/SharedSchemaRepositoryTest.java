/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

class SharedSchemaRepositoryTest extends AbstractSchemaRepositoryTest {
    @Test
    void testSourceWithAndWithoutRevision() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final var idNoRevision = loadAndRegisterSource(sharedSchemaRepository, "/no-revision/imported.yang");
        final var id2 = loadAndRegisterSource(sharedSchemaRepository, "/no-revision/imported@2012-12-12.yang");

        var source = sharedSchemaRepository.getSchemaSource(idNoRevision, YangIRSource.class);
        assertEquals(idNoRevision, source.get().sourceId());
        source = sharedSchemaRepository.getSchemaSource(id2, YangIRSource.class);
        assertEquals(id2, source.get().sourceId());
    }

    private static SourceIdentifier loadAndRegisterSource(final SharedSchemaRepository sharedSchemaRepository,
            final String resourceName) {
        final var sourceProvider = assertYangTextResource(resourceName);
        sourceProvider.setResult();
        final var idNoRevision = sourceProvider.getId();
        sourceProvider.register(sharedSchemaRepository);
        return idNoRevision;
    }

    @Test
    void testSimpleSchemaContext() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final var remoteInetTypesYang = assertYangTextResource("/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);
        final var registeredSourceFuture = sharedSchemaRepository.getSchemaSource(
            remoteInetTypesYang.getId(), YangIRSource.class);
        assertFalse(registeredSourceFuture.isDone());

        final var fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final var schemaContextFuture = fact.createEffectiveModelContext(remoteInetTypesYang.getId());
        assertFalse(schemaContextFuture.isDone());

        // Make source appear
        remoteInetTypesYang.setResult();
        assertEquals(remoteInetTypesYang.getSchemaSourceRepresentation(), registeredSourceFuture.get());

        // Verify schema created successfully
        assertTrue(schemaContextFuture.isDone());
        final var firstSchemaContext = schemaContextFuture.get();
        assertSchemaContext(firstSchemaContext, 1);

        // Try same schema second time
        final var secondSchemaFuture = sharedSchemaRepository.createEffectiveModelContextFactory()
            .createEffectiveModelContext(remoteInetTypesYang.getId());

        // Verify second schema created successfully immediately
        assertTrue(secondSchemaFuture.isDone());
        // Assert same context instance is returned from first and second attempt
        assertSame(firstSchemaContext, secondSchemaFuture.get());
    }

    @Test
    void testTwoSchemaContextsSharingSource() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final var remoteInetTypesYang = assertYangTextResource("/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);
        remoteInetTypesYang.setResult();
        final var remoteTopologyYang = assertYangTextResource("/ietf/network-topology@2013-10-21.yang");
        remoteTopologyYang.register(sharedSchemaRepository);
        remoteTopologyYang.setResult();
        final var remoteModuleNoRevYang = assertYangTextResource("/no-revision/module-without-revision.yang");
        remoteModuleNoRevYang.register(sharedSchemaRepository);

        final var fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final var inetAndTopologySchemaContextFuture = fact.createEffectiveModelContext(
            remoteInetTypesYang.getId(), remoteTopologyYang.getId());
        assertTrue(inetAndTopologySchemaContextFuture.isDone());
        assertSchemaContext(inetAndTopologySchemaContextFuture.get(), 2);

        final var inetAndNoRevSchemaContextFuture = fact.createEffectiveModelContext(
            remoteInetTypesYang.getId(), remoteModuleNoRevYang.getId());
        assertFalse(inetAndNoRevSchemaContextFuture.isDone());

        remoteModuleNoRevYang.setResult();
        assertTrue(inetAndNoRevSchemaContextFuture.isDone());
        assertSchemaContext(inetAndNoRevSchemaContextFuture.get(), 2);
    }

    @Test
    void testFailedSchemaContext() {
        final var sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final var remoteInetTypesYang = assertYangTextResource("/ietf/ietf-inet-types@2010-09-24.yang");
        remoteInetTypesYang.register(sharedSchemaRepository);

        final var fact = sharedSchemaRepository.createEffectiveModelContextFactory();

        // Make source appear
        final var ise = new IllegalStateException("failed schema");
        remoteInetTypesYang.setException(ise);

        final var schemaContextFuture = fact.createEffectiveModelContext(remoteInetTypesYang.getId());
        final var ex = assertThrows(ExecutionException.class, schemaContextFuture::get);
        final var cause = assertInstanceOf(MissingSchemaSourceException.class, ex.getCause());
        assertSame(ise, cause.getCause());
    }

    @Test
    void testDifferentCosts() throws Exception {
        final var sharedSchemaRepository = new SharedSchemaRepository("netconf-mounts");

        final var immediateInetTypesYang = spy(assertYangTextResource("/ietf/ietf-inet-types@2010-09-24.yang"));
        immediateInetTypesYang.register(sharedSchemaRepository);
        immediateInetTypesYang.setResult();

        final var remoteInetTypesYang = spy(
            getRemoteYangSourceProviderFromResource("/ietf/ietf-inet-types@2010-09-24.yang"));
        remoteInetTypesYang.register(sharedSchemaRepository);
        remoteInetTypesYang.setResult();

        final var fact = sharedSchemaRepository.createEffectiveModelContextFactory();
        final var schemaContextFuture = fact.createEffectiveModelContext(remoteInetTypesYang.getId());

        assertSchemaContext(schemaContextFuture.get(), 1);

        final var id = immediateInetTypesYang.getId();
        verify(remoteInetTypesYang, times(0)).getSource(id);
        verify(immediateInetTypesYang).getSource(id);
    }

    static SettableSchemaProvider<YangIRSource> getRemoteYangSourceProviderFromResource(final String resourceName)
            throws Exception {
        return SettableSchemaProvider.createRemote(TextToIRTransformer.transformText(
            new URLYangTextSource(SharedSchemaRepositoryTest.class.getResource(resourceName))),
            YangIRSource.class);
    }
}
