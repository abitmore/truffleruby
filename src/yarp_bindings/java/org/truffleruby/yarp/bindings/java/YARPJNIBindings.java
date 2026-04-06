/*
 * Copyright (c) 2026 TruffleRuby contributors
 * This code is released under a tri EPL/GPL/LGPL license.
 * You can use it, redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.yarp.bindings.java;

public abstract class YARPJNIBindings {

    @SuppressWarnings("restricted")
    public static void loadLibrary(String path) {
        System.load(path);
    }

    public static native byte[] parseAndSerialize(byte[] source, byte[] options);

}
