/*******************************************************************************
 * Copyright (c) 2018 The Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.mpc.rest.client.compatibility.mapping;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractMapper {

	public <S, T> List<T> mapAll(List<S> sources, Function<S, T> mappingFunction) {
		return sources.stream().map(mappingFunction).collect(Collectors.toList());
	}

	public <S, T> T map(S source, Function<S, T> mappingFunction) {
		return mappingFunction.apply(source);
	}
}
