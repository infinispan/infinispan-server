/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */
package com.redhat.datagrid;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Package privileged actions
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 */
public class SecurityActions {
    interface TCLAction {
        class UTIL {
            static TCLAction getTCLAction() {
                return System.getSecurityManager() == null? NON_PRIVILEGED
                        : PRIVILEGED;
            }

            static ClassLoader getContextClassLoader() {
                return getTCLAction().getContextClassLoader();
            }

            static ClassLoader getContextClassLoader(Thread thread) {
                return getTCLAction().getContextClassLoader(thread);
            }

            static void setContextClassLoader(ClassLoader cl) {
                getTCLAction().setContextClassLoader(cl);
            }

            static void setContextClassLoader(Thread thread, ClassLoader cl) {
                getTCLAction().setContextClassLoader(thread, cl);
            }
        }

        TCLAction NON_PRIVILEGED = new TCLAction() {
            @Override
            public ClassLoader getContextClassLoader() {
                return Thread.currentThread().getContextClassLoader();
            }

            @Override
            public ClassLoader getContextClassLoader(Thread thread) {
                return thread.getContextClassLoader();
            }

            @Override
            public void setContextClassLoader(ClassLoader cl) {
                Thread.currentThread().setContextClassLoader(cl);
            }

            @Override
            public void setContextClassLoader(Thread thread, ClassLoader cl) {
                thread.setContextClassLoader(cl);
            }
        };

        TCLAction PRIVILEGED = new TCLAction() {
            private final PrivilegedAction<ClassLoader> getTCLPrivilegedAction =
                    new PrivilegedAction<ClassLoader>() {
                        @Override
                        public ClassLoader run() {
                            return Thread.currentThread()
                                    .getContextClassLoader();
                        }
                    };

            @Override
            public ClassLoader getContextClassLoader() {
                return AccessController.doPrivileged(getTCLPrivilegedAction);
            }

            @Override
            public ClassLoader getContextClassLoader(final Thread thread) {
                return AccessController
                        .doPrivileged(new PrivilegedAction<ClassLoader>() {
                            @Override
                            public ClassLoader run() {
                                return thread.getContextClassLoader();
                            }
                        });
            }

            @Override
            public void setContextClassLoader(final ClassLoader cl) {
                AccessController
                        .doPrivileged(new PrivilegedAction<ClassLoader>() {
                            @Override
                            public ClassLoader run() {
                                Thread.currentThread()
                                        .setContextClassLoader(cl);
                                return null;
                            }
                        });
            }

            @Override
            public void setContextClassLoader(final Thread thread,
                    final ClassLoader cl) {
                AccessController
                        .doPrivileged(new PrivilegedAction<ClassLoader>() {
                            @Override
                            public ClassLoader run() {
                                thread.setContextClassLoader(cl);
                                return null;
                            }
                        });
            }
        };

        ClassLoader getContextClassLoader();

        ClassLoader getContextClassLoader(Thread thread);

        void setContextClassLoader(ClassLoader cl);

        void setContextClassLoader(Thread thread, ClassLoader cl);
    }

    public static ClassLoader getContextClassLoader() {
        return TCLAction.UTIL.getContextClassLoader();
    }

    public static void setContextClassLoader(ClassLoader loader) {
        TCLAction.UTIL.setContextClassLoader(loader);
    }
}
