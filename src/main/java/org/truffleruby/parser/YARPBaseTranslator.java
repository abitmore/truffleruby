/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.List;

import org.prism.AbstractNodeVisitor;
import org.prism.Nodes;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.DummyNode;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.NoKeywordArgumentsDescriptor;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.SequenceNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.literal.NilLiteralNode;

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;

public abstract class YARPBaseTranslator extends AbstractNodeVisitor<RubyNode> {

    protected final RubyLanguage language;
    protected final TranslatorEnvironment environment;
    protected final RubySource rubySource;

    // For convenient/concise access, actually redundant with the rubySource field
    protected final Source source;
    protected final byte[] sourceBytes;
    protected final RubyEncoding sourceEncoding;

    public static final Nodes.Node[] EMPTY_NODE_ARRAY = Nodes.Node.EMPTY_ARRAY;

    public static final Nodes.ParametersNode ZERO_PARAMETERS_NODE = new Nodes.ParametersNode(EMPTY_NODE_ARRAY,
            EMPTY_NODE_ARRAY, null, EMPTY_NODE_ARRAY, EMPTY_NODE_ARRAY, null, null, 0, 0);

    public static final short NO_FLAGS = 0;

    public YARPBaseTranslator(
            RubyLanguage language,
            TranslatorEnvironment environment,
            RubySource rubySource) {
        this.language = language;
        this.environment = environment;
        this.rubySource = rubySource;
        this.source = rubySource.getSource();
        this.sourceBytes = rubySource.getBytes();
        this.sourceEncoding = rubySource.getEncoding();
    }

    public final TranslatorEnvironment getEnvironment() {
        return environment;
    }

    protected RuntimeException fail(Nodes.Node node) {
        var context = RubyLanguage.getCurrentContext();
        String code = toString(node);
        var message = this.getClass().getSimpleName() + " does not know how to translate " +
                node.getClass().getSimpleName() + " at " + context.fileLine(getSourceSection(node)) +
                "\nCode snippet:\n" + code + "\nPrism AST:\n" + node;
        throw new RaiseException(context,
                context.getCoreExceptions().syntaxError(message, null, getSourceSection(node)));
        // throw CompilerDirectives.shouldNotReachHere(message); // Useful for debugging
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        throw fail(node);
    }

    protected static RubyNode[] createArray(int size) {
        return size == 0 ? RubyNode.EMPTY_ARRAY : new RubyNode[size];
    }

    protected final RubyNode translateNodeOrNil(Nodes.Node node) {
        final RubyNode rubyNode;
        if (node == null) {
            rubyNode = new NilLiteralNode();
        } else {
            rubyNode = node.accept(this);
        }
        return rubyNode;
    }

    protected final RubyNode translateNodeOrDeadNode(Nodes.Node node, String label) {
        if (node != null) {
            return node.accept(this);
        } else {
            return new DeadNode(label);
        }
    }

    protected final RubyContextSourceNode createCallNode(RubyNode receiver, String method, RubyNode... arguments) {
        return createCallNode(true, receiver, method, arguments);
    }

    protected final RubyContextSourceNode createCallNode(boolean ignoreVisibility, RubyNode receiver, String method,
            RubyNode... arguments) {
        var parameters = new RubyCallNodeParameters(
                receiver,
                method,
                null,
                NoKeywordArgumentsDescriptor.INSTANCE,
                arguments,
                ignoreVisibility);
        return language.coreMethodAssumptions.createCallNode(parameters);
    }

    protected static Nodes.CallNode callNode(Nodes.Node location, Nodes.Node receiver, String methodName,
            Nodes.Node... arguments) {
        return callNode(location, NO_FLAGS, receiver, methodName, arguments);
    }

    protected static Nodes.CallNode callNode(Nodes.Node location, short flags, Nodes.Node receiver, String methodName,
            Nodes.Node... arguments) {
        return new Nodes.CallNode(flags, receiver, methodName,
                new Nodes.ArgumentsNode(NO_FLAGS, arguments, location.startOffset, location.length), null,
                location.startOffset, location.length);
    }

    protected final TruffleString toTString(Nodes.Node node) {
        return TruffleString.fromByteArrayUncached(sourceBytes, node.startOffset, node.length, sourceEncoding.tencoding,
                false);
    }

    protected final TruffleString toTString(String string) {
        return TStringUtils.fromJavaString(string, sourceEncoding);
    }

    protected final String toString(Nodes.Node node) {
        return TStringUtils.toJavaStringOrThrow(toTString(node), sourceEncoding);
    }

    protected final TruffleString toTString(byte[] bytes) {
        return TruffleString.fromByteArrayUncached(bytes, sourceEncoding.tencoding, false);
    }

    protected final String toString(byte[] bytes) {
        return TStringUtils.toJavaStringOrThrow(toTString(bytes), sourceEncoding);
    }

    protected final SourceSection getSourceSection(Nodes.Node yarpNode) {
        return source.createSection(yarpNode.startOffset, yarpNode.length);
    }

    public final RubyNode assignPositionAndFlags(Nodes.Node yarpNode, RubyNode rubyNode) {
        assignPositionOnly(yarpNode, rubyNode);
        copyNewlineFlag(yarpNode, rubyNode);
        return rubyNode;
    }

    public final RubyNode assignPositionAndFlagsIfMissing(Nodes.Node yarpNode, RubyNode rubyNode) {
        if (rubyNode.hasSource()) {
            return rubyNode;
        }

        assignPositionOnly(yarpNode, rubyNode);
        copyNewlineFlag(yarpNode, rubyNode);
        return rubyNode;
    }

    protected static void assignPositionOnly(Nodes.Node yarpNode, RubyNode rubyNode) {
        rubyNode.unsafeSetSourceSection(yarpNode.startOffset, yarpNode.length);
    }

    // assign position based on a list of nodes (arguments list, exception classes list in a rescue section, etc)
    protected final void assignPositionOnly(Nodes.Node[] nodes, RubyNode rubyNode) {
        final Nodes.Node first = nodes[0];
        final Nodes.Node last = nodes[nodes.length - 1];

        final int length = last.endOffset() - first.startOffset;
        rubyNode.unsafeSetSourceSection(first.startOffset, length);
    }

    protected final void copyNewlineFlag(Nodes.Node yarpNode, RubyNode rubyNode) {
        if (yarpNode.hasNewLineFlag()) {
            TruffleSafepoint.poll(DummyNode.INSTANCE);

            if (environment.getParseEnvironment().isCoverageEnabled()) {
                rubyNode.unsafeSetIsCoverageLine();
                int startLine = environment.getParseEnvironment().yarpSource.line(yarpNode.startOffset);
                language.coverageManager.setLineHasCode(source, startLine);
            }

            rubyNode.unsafeSetIsNewLine();
        }
    }

    protected static RubyNode sequence(Nodes.Node yarpNode, List<RubyNode> sequence) {
        assert !yarpNode.hasNewLineFlag() : "Expected node passed to sequence() to not have a newline flag";

        RubyNode sequenceNode = sequence(sequence);

        if (!sequenceNode.hasSource()) {
            assignPositionOnly(yarpNode, sequenceNode);
        }

        return sequenceNode;
    }

    protected static RubyNode sequence(List<RubyNode> sequence) {
        final List<RubyNode> flattened = Translator.flatten(sequence, true);

        if (flattened.isEmpty()) {
            return new NilLiteralNode();
        } else if (flattened.size() == 1) {
            return flattened.get(0);
        } else {
            final RubyNode[] flatSequence = flattened.toArray(RubyNode.EMPTY_ARRAY);
            return new SequenceNode(flatSequence);
        }
    }

    protected final RubyNode[] translate(Nodes.Node[] nodes) {
        if (nodes.length == 0) {
            return RubyNode.EMPTY_ARRAY;
        }

        RubyNode[] rubyNodes = new RubyNode[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            rubyNodes[i] = nodes[i].accept(this);
        }

        return rubyNodes;
    }

}
