/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
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

package com.anatawa12.nashorn.internal.ir;

import java.util.Arrays;
import java.util.List;
import com.anatawa12.nashorn.internal.codegen.types.Type;
import com.anatawa12.nashorn.internal.ir.annotations.Immutable;
import com.anatawa12.nashorn.internal.ir.visitor.NodeVisitor;

/**
 * IR representation for a runtime call.
 */
@Immutable
public class RuntimeNode extends Expression {
    private static final long serialVersionUID = 1L;

    /**
     * Request enum used for meta-information about the runtime request
     */
    public enum Request {
        /** Reference error type */
        REFERENCE_ERROR,
        /** Get template object from raw and cooked string arrays. */
        GET_TEMPLATE_OBJECT(Type.SCRIPT_OBJECT);

        /** return type for request */
        private final Type returnType;

        private Request() {
            this(Type.OBJECT);
        }

        private Request(final Type returnType) {
            this.returnType    = returnType;
        }

        /**
         * Get the return type
         *
         * @return return type for request
         */
        public Type getReturnType() {
            return returnType;
        }

    }

    /** Runtime request. */
    private final Request request;

    /** Call arguments. */
    private final List<Expression> args;

    /**
     * Constructor
     *
     * @param token   token
     * @param finish  finish
     * @param request the request
     * @param args    arguments to request
     */
    public RuntimeNode(final long token, final int finish, final Request request, final List<Expression> args) {
        super(token, finish);

        this.request      = request;
        this.args         = args;
    }

    private RuntimeNode(final RuntimeNode runtimeNode, final Request request, final List<Expression> args) {
        super(runtimeNode);

        this.request      = request;
        this.args         = args;
    }

    /**
     * Constructor
     *
     * @param token   token
     * @param finish  finish
     * @param request the request
     * @param args    arguments to request
     */
    public RuntimeNode(final long token, final int finish, final Request request, final Expression... args) {
        this(token, finish, request, Arrays.asList(args));
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterRuntimeNode(this)) {
            return visitor.leaveRuntimeNode(setArgs(Node.accept(visitor, args)));
        }

        return this;
    }

    /**
     * Set the arguments of this runtime node
     * @param args new arguments
     * @return new runtime node, or identical if no change
     */
    public RuntimeNode setArgs(final List<Expression> args) {
        if (this.args == args) {
            return this;
        }
        return new RuntimeNode(this, request, args);
    }

}
