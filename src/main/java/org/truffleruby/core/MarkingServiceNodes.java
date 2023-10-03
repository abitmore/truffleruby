/*
 * Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import org.truffleruby.cext.ValueWrapper;
import org.truffleruby.core.MarkingService.ExtensionCallStack;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.DispatchNode;

import java.util.ArrayList;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class MarkingServiceNodes {

    @GenerateUncached
    @GenerateCached(false)
    @GenerateInline
    public abstract static class KeepAliveNode extends RubyBaseNode {

        public abstract void execute(Node node, ValueWrapper object);

        @Specialization(guards = "!stack.hasKeptObjects()")
        static void keepFirstObject(Node node, ValueWrapper object,
                @Bind("getStack(node)") ExtensionCallStack stack) {
            stack.current.preservedObject = object;
        }

        @Specialization(guards = "stack.hasSingleKeptObject()")
        static void keepCreatingList(Node node, ValueWrapper object,
                @Bind("getStack(node)") ExtensionCallStack stack,
                @Cached InlinedConditionProfile sameObjectProfile) {
            if (sameObjectProfile.profile(node, object != stack.current.preservedObject)) {
                createKeptList(object, stack);
            }
        }

        @Specialization(guards = { "stack.hasKeptObjects()", "!stack.hasSingleKeptObject()" })
        @TruffleBoundary
        static void keepAddingToList(Node node, ValueWrapper object,
                @Bind("getStack(node)") ExtensionCallStack stack) {
            stack.current.preservedObjects.add(object);
        }

        @TruffleBoundary
        private static void createKeptList(ValueWrapper object, ExtensionCallStack stack) {
            stack.current.preservedObjects = new ArrayList<>();
            stack.current.preservedObjects.add(stack.current.preservedObject);
            stack.current.preservedObjects.add(object);
        }

        protected static ExtensionCallStack getStack(Node node) {
            return getLanguage(node).getCurrentThread().getCurrentFiber().extensionCallStack;
        }
    }

    public static final class QueueForMarkOnExitNode extends RubyBaseNode {

        @NeverDefault
        public static QueueForMarkOnExitNode create() {
            return new QueueForMarkOnExitNode();
        }

        public void execute(ValueWrapper object) {
            addToList(getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack, object);
        }

        protected void addToList(ExtensionCallStack stack, ValueWrapper object) {
            stack.markOnExitObject(object);
        }
    }

    public abstract static class RunMarkOnExitNode extends RubyBaseNode {

        public abstract void execute(ExtensionCallStack stack);

        @Specialization(guards = "!stack.hasMarkObjects()")
        void nothingToMark(ExtensionCallStack stack) {
            // Do nothing.
        }

        @Specialization(guards = "stack.hasSingleMarkObject()")
        void markSingleObject(ExtensionCallStack stack,
                @Cached @Shared DispatchNode callNode) {
            ValueWrapper value = stack.getSingleMarkObject();
            callNode.call(getContext().getCoreLibrary().truffleCExtModule, "run_marker", value.getObject());
        }

        @TruffleBoundary
        @Specialization(guards = { "stack.hasMarkObjects()", "!stack.hasSingleMarkObject()" })
        void marksToRun(ExtensionCallStack stack,
                @Cached @Shared DispatchNode callNode) {
            // Run the markers...
            var valuesForMarking = stack.getMarkOnExitObjects();
            // Push a new stack frame because we should
            // mutate the list while iterating, and we
            // don't know what the mark routine might do.
            stack.push(false, nil, nil);
            try {
                for (var value : valuesForMarking) {
                    callNode.call(getContext().getCoreLibrary().truffleCExtModule, "run_marker", value.getObject());
                }
            } finally {
                stack.pop();
            }
        }


        @NeverDefault
        public static RunMarkOnExitNode create() {
            return MarkingServiceNodesFactory.RunMarkOnExitNodeGen.create();
        }
    }
}
