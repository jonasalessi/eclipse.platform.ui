/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.ArrayList;
import java.util.List;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class CSSValueListImpl extends AbstractCSSNode implements CSSValueList {

	List<CSSValue> values;

	public CSSValueListImpl(LexicalUnit parsePropertyValue) {
		values = new ArrayList<CSSValue>();

		LexicalUnit unit = parsePropertyValue;
		while(unit != null) {
			values.add(CSSValueFactory.newPrimitiveValue(unit));
			unit = unit.getNextLexicalUnit();
		}
	}

	@Override
	public int getLength() {
		return values.size();
	}

	@Override
	public CSSValue item(int index) {
		return values.get(index);
	}

	@Override
	public String getCssText() {
		StringBuilder buffer = new StringBuilder();
		for (CSSValue value : values) {
			buffer.append(value.getCssText());
			buffer.append(" ");
		}
		return buffer.toString().trim();
	}

	@Override
	public short getCssValueType() {
		return CSS_VALUE_LIST;
	}

	@Override
	public void setCssText(String arg0) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

}
