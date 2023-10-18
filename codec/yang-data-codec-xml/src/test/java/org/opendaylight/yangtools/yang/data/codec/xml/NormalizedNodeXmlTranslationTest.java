/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.dom.DOMResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@RunWith(Parameterized.class)
public class NormalizedNodeXmlTranslationTest extends AbstractXmlTest {
    private final EffectiveModelContext schema;

    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        return List.of(new Object[][] {
                { "/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok.xml", augmentChoiceHell() },
                { "/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok2.xml", null },
                { "/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok3.xml", augmentChoiceHell2() },
                { "/schema/test.yang", "/schema/simple.xml", null },
                { "/schema/test.yang", "/schema/simple2.xml", null },
                // TODO check attributes
                { "/schema/test.yang", "/schema/simple_xml_with_attributes.xml", withAttributes() }
        });
    }

    private static final QNameModule MODULE = QNameModule.create(
        XMLNamespace.of("urn:opendaylight:params:xml:ns:yang:controller:test"), Revision.of("2014-03-13"));

    private static ContainerNode augmentChoiceHell2() {
        final var container = getNodeIdentifier("container");
        final var augmentChoice1QName = QName.create(container.getNodeType(), "augment-choice1");
        final var augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final var containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final var leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        return Builders.containerBuilder()
            .withNodeIdentifier(container)
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(augmentChoice1QName))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(augmentChoice2QName))
                    .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(containerQName))
                        .withChild(ImmutableNodes.leafNode(leafQName, "leaf-value"))
                        .build())
                    .build())
                .build())
            .build();
    }

    private static ContainerNode withAttributes() {
        return Builders.containerBuilder()
            .withNodeIdentifier(getNodeIdentifier("container"))
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(getNodeIdentifier("list"))
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(getNodeIdentifier("list").getNodeType(),
                        getNodeIdentifier("uint32InList").getNodeType(), Uint32.valueOf(3)))
                    .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(getNodeIdentifier("uint32InList"))
                        .withValue(Uint32.valueOf(3))
                        .build())
                    .build())
                .build())
            .withChild(Builders.leafBuilder()
                .withNodeIdentifier(getNodeIdentifier("boolean"))
                .withValue(Boolean.FALSE)
                .build())
            .withChild(Builders.leafSetBuilder()
                .withNodeIdentifier(getNodeIdentifier("leafList"))
                .withChild(Builders.leafSetEntryBuilder()
                    .withNodeIdentifier(new NodeWithValue<>(getNodeIdentifier("leafList").getNodeType(), "a"))
                    .withValue("a")
                    .build())
                .build())
            .build();
    }

    private static ContainerNode augmentChoiceHell() {
        return Builders.containerBuilder()
            .withNodeIdentifier(getNodeIdentifier("container"))
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("ch2"))
                .withChild(Builders.leafBuilder()
                    .withNodeIdentifier(getNodeIdentifier("c2Leaf"))
                    .withValue("2")
                    .build())
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(getNodeIdentifier("c2DeepChoice"))
                    .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(getNodeIdentifier("c2DeepChoiceCase1Leaf2"))
                        .withValue("2")
                        .build())
                    .build())
                .build())
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("ch3"))
                .withChild(Builders.leafBuilder()
                    .withNodeIdentifier(getNodeIdentifier("c3Leaf"))
                    .withValue("3")
                    .build())
                .build())
            .withChild(Builders.leafBuilder()
                .withNodeIdentifier(getNodeIdentifier("augLeaf"))
                .withValue("augment")
                .build())
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("ch"))
                .withChild(Builders.leafBuilder()
                    .withNodeIdentifier(getNodeIdentifier("c1Leaf")).withValue("1")
                    .build())
                .withChild(Builders.leafBuilder()
                    .withNodeIdentifier(getNodeIdentifier("c1Leaf_AnotherAugment"))
                    .withValue("1")
                    .build())
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(getNodeIdentifier("deepChoice"))
                    .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(getNodeIdentifier("deepLeafc1"))
                        .withValue("1")
                        .build())
                    .build())
                .build())
            .build();
    }

    private static NodeIdentifier getNodeIdentifier(final String localName) {
        return new NodeIdentifier(QName.create(MODULE, localName));
    }

    private final ContainerNode expectedNode;
    private final String xmlPath;

    public NormalizedNodeXmlTranslationTest(final String yangPath, final String xmlPath,
            final ContainerNode expectedNode) {
        schema = YangParserTestUtils.parseYangResource(yangPath);
        this.xmlPath = xmlPath;
        this.expectedNode = expectedNode;
    }

    @Test
    public void testTranslationRepairing() throws Exception {
        testTranslation(TestFactories.REPAIRING_OUTPUT_FACTORY);
    }

    @Test
    public void testTranslation() throws Exception {
        testTranslation(TestFactories.DEFAULT_OUTPUT_FACTORY);
    }

    private void testTranslation(final XMLOutputFactory factory) throws Exception {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(xmlPath);

        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schema, QName.create(MODULE, "container")));
        xmlParser.parse(reader);

        final var built = result.getResult().data();
        assertNotNull(built);

        if (expectedNode != null) {
            assertEquals(expectedNode, built);
        }

        final var document = UntrustedXML.newDocumentBuilder().newDocument();
        final var domResult = new DOMResult(document);
        final var xmlStreamWriter = factory.createXMLStreamWriter(domResult);
        final var xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, schema);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(built);

        final var doc = loadDocument(xmlPath);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());

        final Diff diff = new Diff(expectedXml, serializedXml);
        diff.overrideDifferenceListener(new IgnoreTextAndAttributeValuesDifferenceListener());
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());

        // FIXME the comparison cannot be performed, since the qualifiers supplied by XMLUnit do not work correctly in
        // this case
        // We need to implement custom qualifier so that the element ordering does not mess the DIFF
        // dd.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(100, true));
        // assertTrue(dd.toString(), dd.similar());

        // XMLAssert.assertXMLEqual(diff, true);
    }
}
