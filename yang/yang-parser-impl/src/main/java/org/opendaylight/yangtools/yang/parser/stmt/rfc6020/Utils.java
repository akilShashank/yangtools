/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Deviation.Deviate;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameCacheNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final CharMatcher DOUBLE_QUOTE_MATCHER = CharMatcher.is('"');
    private static final CharMatcher SINGLE_QUOTE_MATCHER = CharMatcher.is('\'');
    private static final CharMatcher LEFT_PARENTHESIS_MATCHER = CharMatcher.is('(');
    private static final CharMatcher RIGHT_PARENTHESIS_MATCHER = CharMatcher.is(')');
    private static final CharMatcher AMPERSAND_MATCHER = CharMatcher.is('&');
    private static final CharMatcher QUESTION_MARK_MATCHER = CharMatcher.is('?');
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings().trimResults();
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static final Pattern PATH_ABS = Pattern.compile("/[^/].*");

    private static final Map<String, Deviate> KEYWORD_TO_DEVIATE_MAP;
    static {
        Builder<String, Deviate> keywordToDeviateMapBuilder = ImmutableMap.builder();
        for (Deviate deviate : Deviation.Deviate.values()) {
            keywordToDeviateMapBuilder.put(deviate.getKeyword(), deviate);
        }
        KEYWORD_TO_DEVIATE_MAP = keywordToDeviateMapBuilder.build();
    }

    private static final ThreadLocal<XPathFactory> XPATH_FACTORY = new ThreadLocal<XPathFactory>() {
        @Override
        protected XPathFactory initialValue() {
            return XPathFactory.newInstance();
        }
    };

    private Utils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Cleanup any resources attached to the current thread. Threads interacting with this class can cause thread-local
     * caches to them. Invoke this method if you want to detach those resources.
     */
    public static void detachFromCurrentThread() {
        XPATH_FACTORY.remove();
    }

    public static Collection<SchemaNodeIdentifier.Relative> transformKeysStringToKeyNodes(final StmtContext<?, ?, ?> ctx,
            final String value) {
        List<String> keyTokens = SPACE_SPLITTER.splitToList(value);

        // to detect if key contains duplicates
        if ((new HashSet<>(keyTokens)).size() < keyTokens.size()) {
            // FIXME: report all duplicate keys
            throw new SourceException(ctx.getStatementSourceReference(), "Duplicate value in list key: %s", value);
        }

        Set<SchemaNodeIdentifier.Relative> keyNodes = new HashSet<>();

        for (String keyToken : keyTokens) {

            SchemaNodeIdentifier.Relative keyNode = (Relative) SchemaNodeIdentifier.Relative.create(false,
                    Utils.qNameFromArgument(ctx, keyToken));
            keyNodes.add(keyNode);
        }

        return keyNodes;
    }

    private static String trimSingleLastSlashFromXPath(final String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    static RevisionAwareXPath parseXPath(final StmtContext<?, ?, ?> ctx, final String path) {
        final XPath xPath = XPATH_FACTORY.get().newXPath();
        xPath.setNamespaceContext(StmtNamespaceContext.create(ctx));

        final String trimmed = trimSingleLastSlashFromXPath(path);
        try {
            // TODO: we could capture the result and expose its 'evaluate' method
            xPath.compile(trimmed);
        } catch (XPathExpressionException e) {
            LOG.warn("Argument \"{}\" is not valid XPath string at \"{}\"", path, ctx.getStatementSourceReference(), e);
        }

        return new RevisionAwareXPathImpl(path, PATH_ABS.matcher(path).matches());
    }

    public static QName trimPrefix(final QName identifier) {
        String prefixedLocalName = identifier.getLocalName();
        String[] namesParts = prefixedLocalName.split(":");

        if (namesParts.length == 2) {
            String localName = namesParts[1];
            return QName.create(identifier.getModule(), localName);
        }

        return identifier;
    }

    /**
     *
     * Based on identifier read from source and collections of relevant prefixes and statement definitions mappings
     * provided for actual phase, method resolves and returns valid QName for declared statement to be written.
     * This applies to any declared statement, including unknown statements.
     *
     * @param prefixes - collection of all relevant prefix mappings supplied for actual parsing phase
     * @param stmtDef - collection of all relevant statement definition mappings provided for actual parsing phase
     * @param identifier - statement to parse from source
     * @return valid QName for declared statement to be written
     *
     */
    public static QName getValidStatementDefinition(final PrefixToModule prefixes, final QNameToStatementDefinition
            stmtDef, final QName identifier) {
        if (stmtDef.get(identifier) != null) {
            return stmtDef.get(identifier).getStatementName();
        } else {
            String prefixedLocalName = identifier.getLocalName();
            String[] namesParts = prefixedLocalName.split(":");

            if (namesParts.length == 2) {
                String prefix = namesParts[0];
                String localName = namesParts[1];
                if (prefixes != null && prefixes.get(prefix) != null
                        && stmtDef.get(QName.create(prefixes.get(prefix), localName)) != null) {
                    return QName.create(prefixes.get(prefix), localName);
                }
            }
        }
        return null;
    }

    static SchemaNodeIdentifier nodeIdentifierFromPath(final StmtContext<?, ?, ?> ctx, final String path) {
        // FIXME: is the path trimming really necessary??
        final List<QName> qNames = new ArrayList<>();
        for (String nodeName : SLASH_SPLITTER.split(trimSingleLastSlashFromXPath(path))) {
            try {
                final QName qName = Utils.qNameFromArgument(ctx, nodeName);
                qNames.add(qName);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    String.format("Failed to parse node '%s' in path '%s'", nodeName, path), e);
            }
        }

        return SchemaNodeIdentifier.create(qNames, PATH_ABS.matcher(path).matches());
    }

    public static String stringFromStringContext(final YangStatementParser.ArgumentContext context) {
        StringBuilder sb = new StringBuilder();
        List<TerminalNode> strings = context.STRING();
        if (strings.isEmpty()) {
            strings = Arrays.asList(context.IDENTIFIER());
        }
        for (TerminalNode stringNode : strings) {
            final String str = stringNode.getText();
            char firstChar = str.charAt(0);
            final CharMatcher quoteMatcher;
            if (SINGLE_QUOTE_MATCHER.matches(firstChar)) {
                quoteMatcher = SINGLE_QUOTE_MATCHER;
            } else if (DOUBLE_QUOTE_MATCHER.matches(firstChar)) {
                quoteMatcher = DOUBLE_QUOTE_MATCHER;
            } else {
                sb.append(str);
                continue;
            }
            sb.append(quoteMatcher.removeFrom(str.substring(1, str.length() - 1)));
        }
        return sb.toString();
    }

    public static QName qNameFromArgument(StmtContext<?, ?, ?> ctx, final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return ctx.getPublicDefinition().getStatementName();
        }

        String prefix;
        QNameModule qNameModule = null;
        String localName = null;

        String[] namesParts = value.split(":");
        switch (namesParts.length) {
        case 1:
            localName = namesParts[0];
            qNameModule = getRootModuleQName(ctx);
            break;
        default:
            prefix = namesParts[0];
            localName = namesParts[1];
            qNameModule = getModuleQNameByPrefix(ctx, prefix);
            // in case of unknown statement argument, we're not going to parse it
            if (qNameModule == null
                    && ctx.getPublicDefinition().getDeclaredRepresentationClass()
                    .isAssignableFrom(UnknownStatementImpl.class)) {
                localName = value;
                qNameModule = getRootModuleQName(ctx);
            }
            if (qNameModule == null
                    && Iterables.getLast(ctx.getCopyHistory()) == StmtContext.TypeOfCopy.ADDED_BY_AUGMENTATION) {
                ctx = ctx.getOriginalCtx();
                qNameModule = getModuleQNameByPrefix(ctx, prefix);
            }
            break;
        }

        Preconditions.checkArgument(qNameModule != null,
                "Error in module '%s': can not resolve QNameModule for '%s'. Statement source at %s",
                ctx.getRoot().rawStatementArgument(), value, ctx.getStatementSourceReference());
        final QNameModule resultQNameModule;
        if (qNameModule.getRevision() == null) {
            resultQNameModule = QNameModule.create(qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV)
                .intern();
        } else {
            resultQNameModule = qNameModule;
        }

        return ctx.getFromNamespace(QNameCacheNamespace.class, QName.create(resultQNameModule, localName));
    }

    public static QNameModule getModuleQNameByPrefix(final StmtContext<?, ?, ?> ctx, final String prefix) {
        final ModuleIdentifier modId = ctx.getRoot().getFromNamespace(ImpPrefixToModuleIdentifier.class, prefix);
        final QNameModule qNameModule = ctx.getFromNamespace(ModuleIdentifierToModuleQName.class, modId);

        if (qNameModule == null && StmtContextUtils.producesDeclared(ctx.getRoot(), SubmoduleStatement.class)) {
            String moduleName = ctx.getRoot().getFromNamespace(BelongsToPrefixToModuleName.class, prefix);
            return ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
        }
        return qNameModule;
    }

    public static QNameModule getRootModuleQName(final StmtContext<?, ?, ?> ctx) {
        if (ctx == null) {
            return null;
        }

        final StmtContext<?, ?, ?> rootCtx = ctx.getRoot();
        final QNameModule qNameModule;

        if (StmtContextUtils.producesDeclared(rootCtx, ModuleStatement.class)) {
            qNameModule = rootCtx.getFromNamespace(ModuleCtxToModuleQName.class, rootCtx);
        } else if (StmtContextUtils.producesDeclared(rootCtx, SubmoduleStatement.class)) {
            final String belongsToModuleName = firstAttributeOf(rootCtx.substatements(), BelongsToStatement.class);
            qNameModule = rootCtx.getFromNamespace(ModuleNameToModuleQName.class, belongsToModuleName);
        } else {
            qNameModule = null;
        }

        Preconditions.checkArgument(qNameModule != null, "Failed to look up root QNameModule for %s", ctx);
        if (qNameModule.getRevision() != null) {
            return qNameModule;
        }

        return QNameModule.create(qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV).intern();
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findNode(final StmtContext<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {
        return (StatementContextBase<?, ?, ?>) rootStmtCtx.getFromNamespace(SchemaNodeIdentifierBuildNamespace.class, node);
    }

    public static boolean isUnknownNode(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.getPublicDefinition().getDeclaredRepresentationClass()
                .isAssignableFrom(UnknownStatementImpl.class);
    }

    public static Deviation.Deviate parseDeviateFromString(final StmtContext<?, ?, ?> ctx, final String deviateKeyword) {
        return Preconditions.checkNotNull(KEYWORD_TO_DEVIATE_MAP.get(deviateKeyword),
                "String '%s' is not valid deviate argument. Statement source at %s", deviateKeyword,
                ctx.getStatementSourceReference());
    }

    public static Status parseStatus(final String value) {
        switch (value) {
        case "current":
            return Status.CURRENT;
        case "deprecated":
            return Status.DEPRECATED;
        case "obsolete":
            return Status.OBSOLETE;
        default:
            LOG.warn("Invalid 'status' statement: {}", value);
            return null;
        }
    }

    public static Date getLatestRevision(final Iterable<? extends StmtContext<?, ?, ?>> subStmts) {
        Date revision = null;
        for (StmtContext<?, ?, ?> subStmt : subStmts) {
            if (subStmt.getPublicDefinition().getDeclaredRepresentationClass().isAssignableFrom(RevisionStatement
                    .class)) {
                if (revision == null && subStmt.getStatementArgument() != null) {
                    revision = (Date) subStmt.getStatementArgument();
                } else if (subStmt.getStatementArgument() != null && ((Date) subStmt.getStatementArgument()).compareTo
                        (revision) > 0) {
                    revision = (Date) subStmt.getStatementArgument();
                }
            }
        }
        return revision;
    }

    public static boolean isModuleIdentifierWithoutSpecifiedRevision(final Object o) {
        return (o instanceof ModuleIdentifier)
                && (((ModuleIdentifier) o).getRevision() == SimpleDateFormatUtil.DEFAULT_DATE_IMP || ((ModuleIdentifier) o)
                        .getRevision() == SimpleDateFormatUtil.DEFAULT_BELONGS_TO_DATE);
    }

    /**
     * Replaces illegal characters of QName by the name of the character (e.g.
     * '?' is replaced by "QuestionMark" etc.).
     *
     * @param string
     *            input String
     * @return result String
     */
    public static String replaceIllegalCharsForQName(String string) {
        string = LEFT_PARENTHESIS_MATCHER.replaceFrom(string, "LeftParenthesis");
        string = RIGHT_PARENTHESIS_MATCHER.replaceFrom(string, "RightParenthesis");
        string = AMPERSAND_MATCHER.replaceFrom(string, "Ampersand");
        string = QUESTION_MARK_MATCHER.replaceFrom(string, "QuestionMark");

        return string;
    }
}
