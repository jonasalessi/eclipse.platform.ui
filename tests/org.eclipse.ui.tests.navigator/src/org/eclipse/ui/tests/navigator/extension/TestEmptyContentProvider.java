/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fair Isaac Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TestEmptyContentProvider implements ITreeContentProvider {

	private static final Object[] NO_CHILDREN = new Object[0];

	public static boolean _throw;

	public static void resetTest() {
		_throw = false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return NO_CHILDREN;
	}

	@Override
	public Object getParent(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return NO_CHILDREN;
	}

	@Override
	public void dispose() {}
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

}
