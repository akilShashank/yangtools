/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.opendaylight.yangtools.testutils.mockito.MoreAnswers.exception;

import java.io.Closeable;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.testutils.mockito.UnstubbedMethodException;

public class MockitoUnstubbedMethodExceptionAnswerTest {
    @Test
    public void testAnswering() throws IOException {
        Closeable mock = Mockito.mock(Closeable.class, exception());
        String message = assertThrows(UnstubbedMethodException.class, mock::close).getMessage();
        assertEquals("close() is not stubbed in mock of java.io.Closeable", message);
    }
}