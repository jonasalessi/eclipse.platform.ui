/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.property.set;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IPropertyChangeListener;

/**
 * Simplified abstract implementation of ISetProperty. This class takes care of
 * most of the functional requirements for an ISetProperty implementation,
 * leaving only the property-specific details to subclasses.
 * <p>
 * Subclasses must implement these methods:
 * <ul>
 * <li> {@link #getElementType()}
 * <li> {@link #doGetSet(Object)}
 * <li> {@link #doSetSet(Object, Set, SetDiff)}
 * <li> {@link #adaptListener(IPropertyChangeListener)}
 * <li> {@link #doAddListener(Object, INativePropertyListener)}
 * <li> {@link #doRemoveListener(Object, INativePropertyListener)}
 * </ul>
 * <p>
 * In addition, we recommended overriding {@link #toString()} to return a
 * description suitable for debugging purposes.
 * 
 * @since 1.2
 */
public abstract class SimpleSetProperty extends SetProperty {
	public IObservableSet observeSet(Realm realm, Object source) {
		return new SimpleSetPropertyObservableSet(realm, source, this);
	}

	public IObservableSet observeDetailSet(IObservableValue master) {
		final Realm realm = master.getRealm();
		IObservableFactory factory = new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return SimpleSetProperty.this.observeSet(realm, target);
			}
		};
		return MasterDetailObservables.detailSet(master, factory,
				getElementType());
	}

	/**
	 * Returns the type of the elements in the collection or <code>null</code>
	 * if untyped
	 * 
	 * @return the type of the elements in the collection or <code>null</code>
	 *         if untyped
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract Object getElementType();

	// Accessors

	/**
	 * Returns a Set with the current contents of the source's set property
	 * 
	 * @param source
	 *            the property source
	 * @return a Set with the current contents of the source's set property
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected final Set getSet(Object source) {
		if (source == null)
			return Collections.EMPTY_SET;
		return Collections.unmodifiableSet(doGetSet(source));
	}

	/**
	 * Returns an unmodifiable Set with the current contents of the source's set
	 * property
	 * 
	 * @param source
	 *            the property source
	 * @return an unmodifiable Set with the current contents of the source's set
	 *         property
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract Set doGetSet(Object source);

	// Mutators

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param set
	 *            the new set
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected final void setSet(Object source, Set set, SetDiff diff) {
		if (source != null && !diff.isEmpty())
			doSetSet(source, set, diff);
	}

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param set
	 *            the new set
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doSetSet(Object source, Set set, SetDiff diff);

	// Listeners

	/**
	 * Returns a listener which implements the correct listener interface for
	 * the expected source object, and which parlays property change events from
	 * the source object to the given listener. If there is no listener API for
	 * this property, this method returns null.
	 * 
	 * @param listener
	 *            the property listener to receive events
	 * @return a native listener which parlays property change events to the
	 *         specified listener.
	 * @throws ClassCastException
	 *             if the provided listener does not implement the correct
	 *             listener interface (IValueProperty, IListProperty,
	 *             ISetProperty or IMapProperty) depending on the property.
	 * @noreference This method is not intended to be referenced by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract INativePropertyListener adaptListener(
			IPropertyChangeListener listener);

	/**
	 * Adds the specified listener as a listener for this property on the
	 * specified property source. If the source object has no listener API for
	 * this property (i.e. {@link #adaptListener(IPropertyChangeListener)}
	 * returns null), this method does nothing.
	 * 
	 * @param source
	 *            the property source
	 * @param listener
	 *            a listener obtained from calling
	 *            {@link #adaptListener(IPropertyChangeListener)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected final void addListener(Object source,
			INativePropertyListener listener) {
		if (source != null)
			doAddListener(source, listener);
	}

	/**
	 * Adds the specified listener as a listener for this property on the
	 * specified property source. If the source object has no listener API for
	 * this property (i.e. {@link #adaptListener(IPropertyChangeListener)}
	 * returns null), this method does nothing.
	 * 
	 * @param source
	 *            the property source
	 * @param listener
	 *            a listener obtained from calling
	 *            {@link #adaptListener(IPropertyChangeListener)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doAddListener(Object source,
			INativePropertyListener listener);

	/**
	 * Removes the specified listener as a listener for this property on the
	 * specified property source. If the source object has no listener API for
	 * this property (i.e. {@link #adaptListener(IPropertyChangeListener)}
	 * returns null), this method does nothing.
	 * 
	 * @param source
	 *            the property source
	 * @param listener
	 *            a listener obtained from calling
	 *            {@link #adaptListener(IPropertyChangeListener)} .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected final void removeListener(Object source,
			INativePropertyListener listener) {
		if (source != null)
			doRemoveListener(source, listener);
	}

	/**
	 * Removes the specified listener as a listener for this property on the
	 * specified property source. If the source object has no listener API for
	 * this property (i.e. {@link #adaptListener(IPropertyChangeListener)}
	 * returns null), this method does nothing.
	 * 
	 * @param source
	 *            the property source
	 * @param listener
	 *            a listener obtained from calling
	 *            {@link #adaptListener(IPropertyChangeListener)} .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doRemoveListener(Object source,
			INativePropertyListener listener);
}
