//Copyright 2010 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.client;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides an abstract generic cache implementation which can be used to
 * minimize disk IO operation. This, however, does not make any assumption about
 * locality of reference, neither uses any specific algorithm for optimizing
 * data storage and access.
 * <p/>
 * The cache does not impose any limits on the elements as such. Rather, leaves
 * the concern of freeing up the memory to the JVM. Java references are used for
 * this purpose which ensures that the cache size will never lead to any memory
 * issues. Elements are stored as
 * <p/>
 * Views are used to optimize the access, updation and deletion of elements in
 * cache. This, of course, comes with a cost of extra processing. Views, here,
 * are analogous to the SQL views wherein it is used as a sub-representation of
 * the actual record. Along with every view, a list of actual elements are
 * stored. These are the actual elements that have been cached. Such data
 * structure allows view based deletion which is a common case in User Data
 * Store Cache implementation.
 * <p/>
 *
 * @author nitendra_thakur
 * @param <T>
 */
public abstract class CacheProvider<T> {

    /**
     * A marker interface for the views which can be used along with the cache
     *
     * @author nitendra_thakur
     */
    protected interface View {
    }

    /**
     * A subclass of Java's SoftReference whose equality and ordering is decided
     * by its referral and not by the reference itself. Hence, a new definition
     * for equals() and compareTo() is given
     *
     * @author nitendra_thakur
     */

    private class SPSoftReference extends SoftReference<T> {
        T t;

        SPSoftReference(T t) {
            super(t);
            this.t = t;
        }

        SPSoftReference(T t, ReferenceQueue<? super T> refQueue) {
            super(t, refQueue);
            this.t = t;
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj || !(obj instanceof CacheProvider.SPSoftReference)) {
                return false;
            }
            SPSoftReference inViewSoftRef = (SPSoftReference) obj;
            if (null == get()) {
                if (null == inViewSoftRef.get()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return get().equals(inViewSoftRef.get());
            }
        }

        @Override
        public int hashCode() {
            int part = super.hashCode();
            if (null != t) {
                part += t.hashCode();
            }
            return 11 * part;
        }
    }

    /**
     * A subclass of Java's WeakReference whose equality and ordering is decided
     * by its referral and not by the reference itself. Hence, a new definition
     * for equals() and compareTo() is given
     *
     * @author nitendra_thakur
     */

    private class SPWeakReference extends WeakReference<T> {
        T t;


        SPWeakReference(T t) {
            super(t);
            this.t = t;
        }


        SPWeakReference(T t, ReferenceQueue<? super T> refQueue) {
            super(t, refQueue);
            this.t = t;
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj || !(obj instanceof CacheProvider.SPWeakReference)) {
                return false;
            }
            SPWeakReference inViewWeakRef = (SPWeakReference) obj;
            if (null == get()) {
                if (null == inViewWeakRef.get()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return get().equals(inViewWeakRef.get());
            }
        }

        @Override
        public int hashCode() {
            int part = super.hashCode();
            if (null != t) {
                part += t.hashCode();
            }
            return 11 * part;
        }
    }

    /**
     * The set being used as the cache. Elements are stored as soft reference
     * and not as strong reference
     */
    Set<? super SPSoftReference> cache = new HashSet<SPSoftReference>();
    /**
     * The reference queue to collect the unused references
     */
    ReferenceQueue<T> cacheRefQueue = new ReferenceQueue<T>();

    /**
     * The map contains one entry for every view. Corresponding to every view,
     * there are elements which satisfies this view and are in cache
     */
    Map<View, Set<? super SPWeakReference>> registeredViews = new HashMap<View, Set<? super SPWeakReference>>();
    /**
     * The reference queue to collect the unused references
     */
    ReferenceQueue<T> viewsRefQueue = new ReferenceQueue<T>();

    /**
     * Checks if the element is in cache
     *
     * @param t
     * @return
     */
    public boolean contains(T t) {
        if (null == t) {
            return false;
        }
        return cache.contains(new SPSoftReference(t));
    }

    /**
     * adds an element into the cache and updates the views
     *
     * @param t
     */
    public void add(T t) {
        if (null == t) {
            return;
        }
        cache.add(new SPSoftReference(t, cacheRefQueue));
        Set<View> views = getViews(t);
        for (View view : views) {
            if (registeredViews.containsKey(view)) {
                Set<? super SPWeakReference> viewRefs = registeredViews.get(view);
                if (null == viewRefs) {
                    viewRefs = new HashSet<SPWeakReference>();
                }
                viewRefs.add(new SPWeakReference(t, viewsRefQueue));
            }
        }
    }

    /**
     * Removes the element from cache. Views are not updated at this point of
     * time. Since, views only have weak references of the element, garbage
     * collector will nullify their references. It is also not unsafe to keep
     * these references in views till the time gc does its job. That is because,
     * the existence of an element in cache in not determined by the views.
     *
     * @param t
     */
    public void remove(T t) {
        if (null == t) {
            return;
        }
        cache.remove(new SPSoftReference(t));
    }

    /**
     * removes all the elements which satisfies one view. This is helpful in
     * cases when records are deleted from database based on subset of columns
     * and their values. An SQL WHERE predicate used in such cases can be
     * thought of as a view. This, actually, is the single motivation behind the
     * using views with cache
     *
     * @param view
     */
    protected void removeUsingView(View view) {
        if (registeredViews.containsKey(view)) {
            Set<? super SPWeakReference> viewRefs = registeredViews.get(view);
            if (null != viewRefs) {
                for (Object viewRef : viewRefs) {
                    remove(((SPWeakReference) viewRef).get());
                }
            }
            registeredViews.remove(view);
        }
    }

    /**
     * An abstract factory method to get all the views that are to be
     * maintained.
     *
     * @param t
     * @return
     */

    protected abstract Set<View> getViews(T t);

    /**
     * Calling this will delete all the references whose referent has been
     * garbage collected by garbage collector.
     */
    public void handleEnqued() {
        Reference<? extends T> ref = cacheRefQueue.poll();
        while (null != ref) {
            cache.remove(ref);
            ref = cacheRefQueue.poll();
        }

        ref = viewsRefQueue.poll();
        while (null != ref) {
            registeredViews.remove(ref);
            ref = viewsRefQueue.poll();
        }
    }
}
