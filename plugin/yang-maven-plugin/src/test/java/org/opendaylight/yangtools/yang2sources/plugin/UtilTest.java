/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class UtilTest {
    @Test
    public void getClassPathTest() {
        final MavenProject project = mock(MavenProject.class);
        final File file = mock(File.class);
        final File file2 = mock(File.class);
        final Artifact artifact = mock(Artifact.class);
        final Artifact artifact2 = mock(Artifact.class);

        final Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifact);
        artifacts.add(artifact2);

        doReturn(artifacts).when(project).getArtifacts();
        doReturn(file).when(artifact).getFile();
        doReturn(true).when(file).isFile();
        doReturn("iamjar.jar").when(file).getName();
        doReturn(file2).when(artifact2).getFile();
        doReturn(true).when(file2).isDirectory();

        final List<File> files = Util.getClassPath(project);
        assertEquals(2, files.size());
        assertTrue(files.contains(file) && files.contains(file2));
    }

    @Test
    public void contextHolderTest() throws Exception {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResources(getClass(), "/test.yang",
            "/test2.yang");
        final Set<Module> yangModules = new HashSet<>();
        final ContextHolder cxH = new ContextHolder(context, yangModules, ImmutableSet.of());
        assertEquals(context, cxH.getContext());
        assertEquals(yangModules, cxH.getYangModules());
    }
}
