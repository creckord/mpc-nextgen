/*******************************************************************************
 * Copyright (c) 2010, 2018 The Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.internal.mpc.core.service;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.epp.internal.mpc.core.service.messages"; //$NON-NLS-1$


	public static String DefaultMarketplaceService_cannotCompleteRequest_reason;

	public static String DefaultMarketplaceService_categoryNotFound;

	public static String DefaultMarketplaceService_FavoritesErrorRetrieving;


	public static String DefaultMarketplaceService_FavoritesRetrieve;


	public static String DefaultMarketplaceService_FavoritesUpdate;


	public static String DefaultMarketplaceService_getNodesProgress;


	public static String DefaultMarketplaceService_invalidLocation;


	public static String DefaultMarketplaceService_invalidNode;

	public static String DefaultMarketplaceService_marketNotFound;


	public static String DefaultMarketplaceService_missingNodeId;

	public static String DefaultMarketplaceService_mustConfigureBaseUrl;

	public static String DefaultMarketplaceService_nodeNotFound;


	public static String DefaultMarketplaceService_nullResultNodes;

	public static String DefaultMarketplaceService_parseError;

	public static String DefaultMarketplaceService_retrievingDataFrom;

	public static String DefaultMarketplaceService_unexpectedResponse;

	public static String DefaultMarketplaceService_unexpectedResponseContent;


	public static String DefaultMarketplaceService_UnsupportedSearchString;


	public static String MarketplaceStorageService_defaultStorageServiceName;


	public static String MarketplaceUnmarshaller_errorNullStream;


	public static String MarketplaceUnmarshaller_invalidResponseContent;


	public static String MarketplaceUnmarshaller_unexpectedResponseContentNullResult;


	public static String UserFavoritesService_SettingUserFavorites;


	public static String UserFavoritesService_uriMissingHost;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
