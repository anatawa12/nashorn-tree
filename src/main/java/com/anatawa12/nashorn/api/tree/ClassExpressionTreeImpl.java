/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.anatawa12.nashorn.api.tree;

import java.util.List;
import com.anatawa12.nashorn.internal.ir.ClassNode;

final class ClassExpressionTreeImpl extends ExpressionTreeImpl implements ClassExpressionTree {

    private final IdentifierTree name;
    private final ExpressionTree classHeritage;
    private final PropertyTree constructor;
    private final List<? extends PropertyTree> classElements;

    ClassExpressionTreeImpl(final ClassNode cn, final IdentifierTree name,
            final ExpressionTree classHeritage, final PropertyTree constructor,
            final List<? extends PropertyTree> classElements) {
        super(cn);
        this.name = name;
        this.classHeritage = classHeritage;
        this.constructor = constructor;
        this.classElements = classElements;
    }

    @Override
    public Kind getKind() {
        return Kind.CLASS_EXPRESSION;
    }

    @Override
    public IdentifierTree getName() {
        return name;
    }

    @Override
    public ExpressionTree getClassHeritage() {
        return classHeritage;
    }

    @Override
    public PropertyTree getConstructor() {
        return constructor;
    }

    @Override
    public List<? extends PropertyTree> getClassElements() {
        return classElements;
    }

    @Override
    public <R,D> R accept(final TreeVisitor<R,D> visitor, final D data) {
        return visitor.visitClassExpression(this, data);
    }
}