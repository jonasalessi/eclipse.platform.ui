/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.common.HandleObjectManager;

/**
 * <p>
 * A central repository for commands -- both in the defined and undefined
 * states. Commands can be created and retrieved using this manager. It is
 * possible to listen to changes in the collection of commands by attaching a
 * listener to the manager.
 * </p>
 * 
 * @see CommandManager#getCommand(String)
 * @since 3.1
 */
public final class CommandManager extends HandleObjectManager implements
		ICategoryListener, ICommandListener {

	/**
	 * A listener that forwards incoming execution events to execution listeners
	 * on this manager. The execution events will come from any command on this
	 * manager.
	 * 
	 * @since 3.1
	 */
	private final class ExecutionListener implements IExecutionListener {

		public final void notHandled(final String commandId,
				final NotHandledException exception) {
			if (executionListeners != null) {
				final int executionListenersSize = executionListeners.size();
				if (executionListenersSize > 0) {
					/*
					 * Bug 88629. Copying to an array avoids a
					 * ConcurrentModificationException if someone tries to
					 * remove the listener while handling the event.
					 */
					final IExecutionListener[] listeners = (IExecutionListener[]) executionListeners
							.toArray(new IExecutionListener[executionListenersSize]);
					for (int i = 0; i < executionListenersSize; i++) {
						final IExecutionListener listener = listeners[i];
						listener.notHandled(commandId, exception);
					}
				}
			}
		}

		public final void postExecuteFailure(final String commandId,
				final ExecutionException exception) {
			if (executionListeners != null) {
				final int executionListenersSize = executionListeners.size();
				if (executionListenersSize > 0) {
					/*
					 * Bug 88629. Copying to an array avoids a
					 * ConcurrentModificationException if someone tries to
					 * remove the listener while handling the event.
					 */
					final IExecutionListener[] listeners = (IExecutionListener[]) executionListeners
							.toArray(new IExecutionListener[executionListenersSize]);
					for (int i = 0; i < executionListenersSize; i++) {
						final IExecutionListener listener = listeners[i];
						listener.postExecuteFailure(commandId, exception);
					}
				}
			}
		}

		public final void postExecuteSuccess(final String commandId,
				final Object returnValue) {
			if (executionListeners != null) {
				final int executionListenersSize = executionListeners.size();
				if (executionListenersSize > 0) {
					/*
					 * Bug 88629. Copying to an array avoids a
					 * ConcurrentModificationException if someone tries to
					 * remove the listener while handling the event.
					 */
					final IExecutionListener[] listeners = (IExecutionListener[]) executionListeners
							.toArray(new IExecutionListener[executionListenersSize]);
					for (int i = 0; i < executionListenersSize; i++) {
						final IExecutionListener listener = listeners[i];
						listener.postExecuteSuccess(commandId, returnValue);
					}
				}
			}
		}

		public final void preExecute(final String commandId,
				final ExecutionEvent event) {
			if (executionListeners != null) {
				final int executionListenersSize = executionListeners.size();
				if (executionListenersSize > 0) {
					/*
					 * Bug 88629. Copying to an array avoids a
					 * ConcurrentModificationException if someone tries to
					 * remove the listener while handling the event.
					 */
					final IExecutionListener[] listeners = (IExecutionListener[]) executionListeners
							.toArray(new IExecutionListener[executionListenersSize]);
					for (int i = 0; i < executionListenersSize; i++) {
						final IExecutionListener listener = listeners[i];
						listener.preExecute(commandId, event);
					}
				}
			}
		}
	}

	/**
	 * The identifier of the category in which all auto-generated commands will
	 * appear. This value must never be <code>null</code>.
	 */
	public static final String AUTOGENERATED_CATEGORY_ID = "org.eclipse.core.commands.categories.autogenerated"; //$NON-NLS-1$

	/**
	 * The map of category identifiers (<code>String</code>) to categories (
	 * <code>Category</code>). This collection may be empty, but it is never
	 * <code>null</code>.
	 */
	private final Map categoriesById = new HashMap();

	/**
	 * The set of identifiers for those categories that are defined. This value
	 * may be empty, but it is never <code>null</code>.
	 */
	private final Set definedCategoryIds = new HashSet();

	/**
	 * The execution listener for this command manager. This just forwards
	 * events from commands controlled by this manager to listeners on this
	 * manager.
	 */
	private IExecutionListener executionListener = null;

	/**
	 * The collection of execution listeners. This collection is
	 * <code>null</code> if there are no listeners.
	 */
	private Collection executionListeners = null;

	/**
	 * Adds a listener to this command manager. The listener will be notified
	 * when the set of defined commands changes. This can be used to track the
	 * global appearance and disappearance of commands.
	 * 
	 * @param listener
	 *            The listener to attach; must not be <code>null</code>.
	 */
	public final void addCommandManagerListener(
			final ICommandManagerListener listener) {
		addListenerObject(listener);
	}

	/**
	 * Adds an execution listener to this manager. This listener will be
	 * notified if any of the commands controlled by this manager execute. This
	 * can be used to support macros and instrumentation of commands.
	 * 
	 * @param listener
	 *            The listener to attach; must not be <code>null</code>.
	 */
	public final void addExecutionListener(final IExecutionListener listener) {
		if (listener == null) {
			throw new NullPointerException(
					"Cannot add a null execution listener"); //$NON-NLS-1$
		}

		if (executionListeners == null) {
			executionListeners = new ArrayList(1);

			// Add an execution listener to every command.
			executionListener = new ExecutionListener();
			final Iterator commandItr = handleObjectsById.values().iterator();
			while (commandItr.hasNext()) {
				final Command command = (Command) commandItr.next();
				command.addExecutionListener(executionListener);
			}

		} else if (executionListeners.contains(listener)) {
			return; // Listener already exists

		}

		executionListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.ICategoryListener#categoryChanged(org.eclipse.core.commands.CategoryEvent)
	 */
	public final void categoryChanged(CategoryEvent categoryEvent) {
		if (categoryEvent.isDefinedChanged()) {
			final Category category = categoryEvent.getCategory();
			final String categoryId = category.getId();
			final boolean categoryIdAdded = category.isDefined();
			if (categoryIdAdded) {
				definedCategoryIds.add(categoryId);
			} else {
				definedCategoryIds.remove(categoryId);
			}
			fireCommandManagerChanged(new CommandManagerEvent(this, null,
					false, false, categoryId, categoryIdAdded, true));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.commands.ICommandListener#commandChanged(org.eclipse.commands.CommandEvent)
	 */
	public final void commandChanged(final CommandEvent commandEvent) {
		if (commandEvent.isDefinedChanged()) {
			final Command command = commandEvent.getCommand();
			final String commandId = command.getId();
			final boolean commandIdAdded = command.isDefined();
			if (commandIdAdded) {
				definedHandleObjects.add(command);
			} else {
				definedHandleObjects.remove(command);
			}
			fireCommandManagerChanged(new CommandManagerEvent(this, commandId,
					commandIdAdded, true, null, false, false));
		}
	}

	/**
	 * Sets the name and description of the category for uncategorized commands.
	 * This is the category that will be returned if
	 * {@link #getCategory(String)} is called with <code>null</code>.
	 * 
	 * @param name
	 *            The name of the category for uncategorized commands; must not
	 *            be <code>null</code>.
	 * @param description
	 *            The description of the category for uncategorized commands;
	 *            may be <code>null</code>.
	 * @since 3.2
	 */
	public final void defineUncategorizedCategory(final String name,
			final String description) {
		final Category category = getCategory(AUTOGENERATED_CATEGORY_ID);
		category.define(name, description);
	}

	/**
	 * Notifies all of the listeners to this manager that the set of defined
	 * command identifiers has changed.
	 * 
	 * @param event
	 *            The event to send to all of the listeners; must not be
	 *            <code>null</code>.
	 */
	private final void fireCommandManagerChanged(final CommandManagerEvent event) {
		if (event == null)
			throw new NullPointerException();

		final Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final ICommandManagerListener listener = (ICommandManagerListener) listeners[i];
			listener.commandManagerChanged(event);
		}
	}

	/**
	 * Gets the category with the given identifier. If no such category
	 * currently exists, then the category will be created (but be undefined).
	 * 
	 * @param categoryId
	 *            The identifier to find; must not be <code>null</code>. If
	 *            the category is <code>null</code>, then a category suitable
	 *            for uncategorized items is defined and returned.
	 * @return The category with the given identifier; this value will never be
	 *         <code>null</code>, but it might be undefined.
	 * @see Category
	 */
	public final Category getCategory(final String categoryId) {
		if (categoryId == null) {
			return getCategory(AUTOGENERATED_CATEGORY_ID);
		}

		checkId(categoryId);

		Category category = (Category) categoriesById.get(categoryId);
		if (category == null) {
			category = new Category(categoryId);
			categoriesById.put(categoryId, category);
			category.addCategoryListener(this);
		}

		return category;
	}

	/**
	 * Gets the command with the given identifier. If no such command currently
	 * exists, then the command will be created (but will be undefined).
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code> and
	 *            must not be zero-length.
	 * @return The command with the given identifier; this value will never be
	 *         <code>null</code>, but it might be undefined.
	 * @see Command
	 */
	public final Command getCommand(final String commandId) {
		checkId(commandId);

		Command command = (Command) handleObjectsById.get(commandId);
		if (command == null) {
			command = new Command(commandId);
			handleObjectsById.put(commandId, command);
			command.addCommandListener(this);

			if (executionListener != null) {
				command.addExecutionListener(executionListener);
			}
		}

		return command;
	}

	/**
	 * Returns the those categories that are defined.
	 * 
	 * @return The defined categories; this value may be empty, but it is never
	 *         <code>null</code>.
	 * @since 3.2
	 */
	public final Category[] getDefinedCategories() {
		final Category[] categories = new Category[definedCategoryIds.size()];
		final Iterator categoryIdItr = definedCategoryIds.iterator();
		int i = 0;
		while (categoryIdItr.hasNext()) {
			String categoryId = (String) categoryIdItr.next();
			categories[i++] = getCategory(categoryId);
		}
		return categories;
	}

	/**
	 * Returns the set of identifiers for those category that are defined.
	 * 
	 * @return The set of defined category identifiers; this value may be empty,
	 *         but it is never <code>null</code>.
	 */
	public final Set getDefinedCategoryIds() {
		return Collections.unmodifiableSet(definedCategoryIds);
	}

	/**
	 * Returns the set of identifiers for those commands that are defined.
	 * 
	 * @return The set of defined command identifiers; this value may be empty,
	 *         but it is never <code>null</code>.
	 */
	public final Set getDefinedCommandIds() {
		return getDefinedHandleObjectIds();
	}

	/**
	 * Returns the those commands that are defined.
	 * 
	 * @return The defined commands; this value may be empty, but it is never
	 *         <code>null</code>.
	 * @since 3.2
	 */
	public final Command[] getDefinedCommands() {
		return (Command[]) definedHandleObjects
				.toArray(new Command[definedHandleObjects.size()]);
	}

	/**
	 * Removes a listener from this command manager.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removeCommandManagerListener(
			final ICommandManagerListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * Removes an execution listener from this command manager.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removeExecutionListener(final IExecutionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Cannot remove a null listener"); //$NON-NLS-1$
		}

		if (executionListeners == null) {
			return;
		}

		executionListeners.remove(listener);

		if (executionListeners.isEmpty()) {
			executionListeners = null;

			// Remove the execution listener to every command.
			final Iterator commandItr = handleObjectsById.values().iterator();
			while (commandItr.hasNext()) {
				final Command command = (Command) commandItr.next();
				command.removeExecutionListener(executionListener);
			}
			executionListener = null;

		}
	}

	/**
	 * Block updates all of the handlers for all of the commands. If the handler
	 * is <code>null</code> or the command id does not exist in the map, then
	 * the command becomes unhandled. Otherwise, the handler is set to the
	 * corresponding value in the map.
	 * 
	 * @param handlersByCommandId
	 *            A map of command identifiers (<code>String</code>) to
	 *            handlers (<code>IHandler</code>). This map may be
	 *            <code>null</code> if all handlers should be cleared.
	 *            Similarly, if the map is empty, then all commands will become
	 *            unhandled.
	 */
	public final void setHandlersByCommandId(final Map handlersByCommandId) {
		// Make that all the reference commands are created.
		final Iterator commandIdItr = handlersByCommandId.keySet().iterator();
		while (commandIdItr.hasNext()) {
			getCommand((String) commandIdItr.next());
		}

		// Now, set-up the handlers on all of the existing commands.
		final Iterator commandItr = handleObjectsById.values().iterator();
		while (commandItr.hasNext()) {
			final Command command = (Command) commandItr.next();
			final String commandId = command.getId();
			final Object value = handlersByCommandId.get(commandId);
			if (value instanceof IHandler) {
				command.setHandler((IHandler) value);
			} else {
				command.setHandler(null);
			}
		}
	}
}
