/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.CompilerDirectives;
import org.prism.AbstractNodeVisitor;
import org.prism.Nodes;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.array.MultipleAssignmentNode;
import org.truffleruby.core.array.NoopAssignableNode;
import org.truffleruby.core.cast.SplatCastNode;
import org.truffleruby.core.cast.SplatCastNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.locals.ReadLocalNode;
import org.truffleruby.language.locals.WriteLocalNode;

/** Translate Nodes.MultiTargetNode.
 *
 * Used in the following cases: descturturing in multi-assignment and destructuring in method or block parameters:
 *
 * <pre>
 *     a, (b, c) = 1, [2, 3]
 *
 *     def foo(a, (b, c))
 *     end
 * </pre>
 *
 * NOTE: cannot inherit from YARPBaseTranslator because it returns AssignableNode instead of RubyNode. */
public final class YARPMultiTargetNodeTranslator extends AbstractNodeVisitor<AssignableNode> {

    private final Nodes.MultiTargetNode node;
    private final RubyLanguage language;
    private final YARPTranslator yarpTranslator;
    /** a node that will be destructured */
    private final RubyNode readNode;

    public YARPMultiTargetNodeTranslator(
            Nodes.MultiTargetNode node,
            RubyLanguage language,
            YARPTranslator yarpTranslator,
            RubyNode readNode) {
        this.node = node;
        this.language = language;
        this.yarpTranslator = yarpTranslator;
        this.readNode = readNode;
    }

    public MultipleAssignmentNode translate() {
        final RubyNode rhsNode;

        if (readNode == null) {
            rhsNode = new NilLiteralNode();
        } else {
            rhsNode = readNode;
        }

        final SplatCastNode splatCastNode = SplatCastNodeGen.create(
                language,
                SplatCastNode.NilBehavior.ARRAY_WITH_NIL,
                true,
                null);

        final AssignableNode[] preNodes = new AssignableNode[node.lefts.length];
        for (int i = 0; i < node.lefts.length; i++) {
            preNodes[i] = node.lefts[i].accept(this);
        }

        final AssignableNode restNode;
        if (node.rest != null) {
            // implicit rest in nested target is allowed only for multi-assignment and isn't allowed in block parameters
            if (node.rest instanceof Nodes.ImplicitRestNode) {
                // a, = []
                // do nothing
                restNode = null;
            } else {
                restNode = node.rest.accept(this);
            }
        } else {
            restNode = null;
        }

        final AssignableNode[] postNodes = new AssignableNode[node.rights.length];
        for (int i = 0; i < node.rights.length; i++) {
            postNodes[i] = node.rights[i].accept(this);
        }

        final var multipleAssignmentNode = new MultipleAssignmentNode(
                preNodes,
                restNode,
                postNodes,
                splatCastNode,
                rhsNode);
        return multipleAssignmentNode;
    }

    @Override
    public AssignableNode visitClassVariableTargetNode(Nodes.ClassVariableTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitCallTargetNode(Nodes.CallTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitConstantPathTargetNode(Nodes.ConstantPathTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitConstantTargetNode(Nodes.ConstantTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitGlobalVariableTargetNode(Nodes.GlobalVariableTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitImplicitRestNode(Nodes.ImplicitRestNode node) {
        throw CompilerDirectives.shouldNotReachHere("handled in #translate");
    }

    @Override
    public AssignableNode visitIndexTargetNode(Nodes.IndexTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitInstanceVariableTargetNode(Nodes.InstanceVariableTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitLocalVariableTargetNode(Nodes.LocalVariableTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitMultiTargetNode(Nodes.MultiTargetNode node) {
        final var translator = new YARPMultiTargetNodeTranslator(node, language, yarpTranslator, null);
        final MultipleAssignmentNode multipleAssignmentNode = translator.translate();

        return multipleAssignmentNode.toAssignableNode();
    }

    @Override
    public AssignableNode visitSplatNode(Nodes.SplatNode node) {
        if (node.expression != null) {
            return node.expression.accept(this);
        } else {
            // do nothing for single "*"
            return new NoopAssignableNode();
        }
    }

    @Override
    public AssignableNode visitRequiredParameterNode(Nodes.RequiredParameterNode node) {
        final String name = node.name;
        final ReadLocalNode lhs = yarpTranslator.getEnvironment().findLocalVarNode(name);
        final RubyNode rhs = new DeadNode("YARPMultiTargetNodeTranslator#visitRequiredParameterNode");
        final WriteLocalNode rubyNode = lhs.makeWriteNode(rhs);

        return rubyNode.toAssignableNode();
    }

    @Override
    protected AssignableNode defaultVisit(Nodes.Node node) {
        throw new Error("Unknown node: " + node);
    }

}
