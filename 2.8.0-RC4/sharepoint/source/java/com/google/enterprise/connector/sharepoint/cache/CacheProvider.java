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

package com.google.enterprise.connector.sharepoint.cache;


import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Provides an abstract generic cache implementation which can be used to
 * minimize disk IO operation. This, however, does not make any assumption about
 * locality of reference, neither uses any specific algorithm for optimizing
 * data storage and access.
 * <p/>
 * The cache does not impose any limits on the elements as such. Rather, leaves
 * the concern of freeing up the memory to the JVM. Java references are used for
 * this purpose which ensures that the cache size will never lead to any memory
 * issues. Hence, elements in cache are stored as soft references.
 * <p/>
 * Views are used to optimize the access, updates and deletion of elements in
 * cache. These are analogous to the SQL views in the sense that they gives a
 * partial view of the original object. Along with every view, a list of
 * elements are stored that are there in the cache. Such data structure allows
 * view based deletion which is a common case in User Data Store DAO
 * implementation. Views are stored as weak references because their lifetime
 * depends on the actual elements that are their in the cache.
 * <p/>
 *
 * @author nitendra_thakur
 * @param <T>
 */
public abstract class CacheProvider<T> implements ICache<T> {

    /**
     * A marker interface for the views which can be used along with the cache
     * The implementors are not supposed to keep any strong references of the
     * objects being cached. Since, the cache relies on soft and weak
     * references, if the caller will maintain any strong reference of the
     * objects, the cache will keep on growing.
     *
     * @author nitendra_thakur
     */
    protected interface View {
    }

    /**
     * A subclass of Java's SoftReference whose equality and ordering is decided
     * by its referral and not by the reference itself. Hence, a new definition
     * for equals() and hasCode() is given
     *
     * @author nitendra_thakur
     */
    private static final class SPSoftReference<T> extends SoftReference<T> {
        int hashcode;

        SPSoftReference(T t) {
            super(t);
            this.hashcode = t.hashCode();
        }

        SPSoftReference(T t, ReferenceQueue<? super T> refQueue) {
            super(t, refQueue);
            this.hashcode = t.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj || !(obj instanceof Reference)) {
                return false;
            }
            Reference<T> inRef = (Reference) obj;
            if (null == get()) {
                if (null == inRef.get()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return get().equals(inRef.get());
            }
        }

        @Override
        public int hashCode() {
            return hashcode;
        }
    }

    /**
     * A subclass of Java's WeakReference whose equality and ordering is decided
     * by its referral and not by the reference itself. Hence, a new definition
     * for equals() and hasCodeTo() is given
     *
     * @author nitendra_thakur
     */
    private static final class SPWeakReference<T> extends WeakReference<T> {
        int hashcode;

        SPWeakReference(T t) {
            super(t);
            this.hashcode = t.hashCode();
        }

        SPWeakReference(T t, ReferenceQueue<? super T> refQueue) {
            super(t, refQueue);
            this.hashcode = t.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj || !(obj instanceof Reference)) {
                return false;
            }
            Reference<T> inRef = (Reference) obj;
            if (null == get()) {
                if (null == inRef.get()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return get().equals(inRef.get());
            }
        }

        @Override
        public int hashCode() {
            return hashcode;
        }
    }

    /*
     * The KeySet of this map is used as the cache. Elements are stored as soft
     * reference to let GC handle their life span.
     */
    private Map<SPSoftReference<T>, Set<View>> cacheMap = new HashMap<SPSoftReference<T>, Set<View>>();
    /* Keep track of those element references which should be removed from cache */
    private ReferenceQueue<T> cacheRefQueue = new ReferenceQueue<T>();

    /*
     * Allows access to all elements corresponding to a view. Only those views
     * that have at least one corresponding element stored in cache will be
     * present here. A strong reference of view in the cacheMap assures this
     * invariant.
     */
    private Map<View, Set<SPWeakReference<T>>> viewRefsMap = new WeakHashMap<View, Set<SPWeakReference<T>>>();

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
        return cacheMap.containsKey(new SPSoftReference<T>(t));
    }

    /**
     * adds an element into the cache and updates the views. Views are
     * maintained separate from the actual cache. Implementors can specify what
     * all views are to be maintained by giving an appropriate implementation of
     * {@link CacheProvider#getViews(Object)}
     * <p/>
     * After this method call returns, caller can get the cached element using
     * any of the views that was registered.
     *
     * @param t
     */
    public void add(T t) {
        if (null == t) {
            return;
        }
        Set<View> views = getViews(t);
        cacheMap.put(new SPSoftReference<T>(t, cacheRefQueue), views);

        for (View view : views) {
            Set<SPWeakReference<T>> referents;
            if (viewRefsMap.containsKey(view)) {
                referents = viewRefsMap.get(view);
                if (null == referents) {
                    referents = new HashSet<SPWeakReference<T>>();
                    viewRefsMap.put(view, referents);
                }
            } else {
                referents = new HashSet<SPWeakReference<T>>();
                viewRefsMap.put(view, referents);
            }
            referents.add(new SPWeakReference<T>(t));
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
        cacheMap.remove(new SPSoftReference<T>(t));
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
        Set<SPWeakReference<T>> refs = viewRefsMap.get(view);
        if (null == refs) {
            return;
        }
        for (SPWeakReference<T> ref : refs) {
            remove(ref.get());
        }
    }

    /**
     * An abstract factory method to get all the views that are to be
     * maintained. Views are requested for an element that is to be cached. The
     * specified views can be used later to get the actual cached element that
     * is the argument t of the method
     *
     * @param t the element that is cached
     * @return All the views that should be maintained along with t
     */
    protected abstract Set<View> getViews(T t);

    /**
     * Calling this will delete all the references whose referent has been
     * garbage collected. There is no point in keeping these references in
     * cache.
     */
    public void clearCache() {
        Reference<? extends T> ref = cacheRefQueue.poll();
        while (null != ref) {
            cacheMap.remove(ref);
            ref = cacheRefQueue.poll();
        }
    }

    public int size() {
        return cacheMap.size();
    }
}
