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
 *     Yatta Solutions - initial API and implementation, public API (bug 432803)
 *******************************************************************************/
package org.eclipse.epp.internal.mpc.ui.wizards;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCategory;
import org.eclipse.epp.mpc.core.model.ICategory;
import org.eclipse.epp.mpc.core.model.IMarket;
import org.eclipse.epp.mpc.core.model.INode;
import org.eclipse.epp.mpc.ui.CatalogDescriptor;
import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;

/**
 * @author Carsten Reckord
 */
public class NewsUrlHandler extends MarketplaceUrlHandler implements LocationListener, ProgressListener {

	private Set<String> documentLinks = null;

	private final NewsViewer viewer;

	public NewsUrlHandler(NewsViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void changed(LocationEvent event) {
		updatePageLinks();
	}

	private void updatePageLinks() {
		viewer.getControl().getDisplay().asyncExec(() -> {
			// Links should open in external browser.
			// Since explicit HREF targets interfere with that,
			// we'll just remove them.
			Object[] links = null;
			try {
				links = (Object[]) viewer.getBrowser()
						.evaluate( //
								"return (function() {" + //$NON-NLS-1$
								"   var links = document.links;" + //$NON-NLS-1$
								"   var hrefs = Array();" + //$NON-NLS-1$
								"   for (var i=0; i<links.length; i++) {" + //$NON-NLS-1$
								"      links[i].target='_self';" + //$NON-NLS-1$
								"      hrefs[i]=links[i].href;" + //$NON-NLS-1$
								"   };" + //$NON-NLS-1$
								"   return hrefs;" + //$NON-NLS-1$
								"})();"); //$NON-NLS-1$
			} catch (SWTException ex) {
				MarketplaceClientUi.log(IStatus.WARNING,
						"Failed to process link targets on news page. Some links might not open in external browser.", //$NON-NLS-1$
						ex);
				NewsUrlHandler.this.documentLinks = null;
			}
			// Remember document links for navigation handling since we
			// don't want to deal with URLs from dynamic loading events
			if (links != null) {
				Set<String> documentLinks = new HashSet<>();
				for (Object link : links) {
					documentLinks.add(link.toString());
				}
				NewsUrlHandler.this.documentLinks = documentLinks;
			}
		});
	}

	@Override
	public void changing(LocationEvent event) {
		if (!event.doit) {
			return;
		}
		String newLocation = event.location;
		boolean handled = handleUri(newLocation);
		if (handled) {
			event.doit = false;
		} else {
			String currentLocation = viewer.getBrowser().getUrl();
			if (isNavigation(currentLocation, newLocation)) {
				event.doit = false;
				viewer.getWizard().openUrl(newLocation);
			}
		}
	}

	private boolean isNavigation(String currentLocation, String newLocation) {
		if (eq(currentLocation, newLocation) || newLocation.startsWith("javascript:") || //$NON-NLS-1$
				"about:blank".equals(newLocation) || "about:blank".equals(currentLocation)) { //$NON-NLS-1$//$NON-NLS-2$
			return false;
		}
		if (documentLinks == null || !documentLinks.contains(newLocation)) {
			return false;
		}
		return !isSameLocation(currentLocation, newLocation);
	}

	static boolean isSameLocation(String currentLocation, String newLocation) {
		try {
			URI currentUri = new URI(currentLocation);
			URI newUri = new URI(newLocation);
			return equalsHostAndPath(currentUri, newUri);
		} catch (URISyntaxException e) {
			return false;
		}
	}

	static boolean equalsHostAndPath(URI currentLocation, URI newLocation) {
		return eq(currentLocation.getHost(), newLocation.getHost())
				&& equalsIgnoreTrailingSlash(currentLocation.getPath(), newLocation.getPath());
	}

	static boolean equalsIgnoreTrailingSlash(String path1, String path2) {
		if (path1.endsWith("/") && !path2.endsWith("/")) { //$NON-NLS-1$//$NON-NLS-2$
			path1 = path1.substring(0, path1.length() - 1);
		} else if (!path1.endsWith("/") && path2.endsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
			path2 = path2.substring(0, path2.length() - 1);
		}
		return eq(path1, path2);
	}

	static boolean eq(String s1, String s2) {
		return s1 == s2 || (s1 != null && s1.equals(s2));
	}

	@Override
	protected boolean handleSearch(CatalogDescriptor descriptor, String url, String searchString,
			Map<String, String> params) {
		MarketplaceWizard marketplaceWizard = viewer.getWizard();

		String filterParam = params.get("filter"); //$NON-NLS-1$
		String[] filters = filterParam.split(" "); //$NON-NLS-1$
		ICategory searchCategory = null;
		IMarket searchMarket = null;
		for (String filter : filters) {
			if (filter.startsWith("tid:")) { //$NON-NLS-1$
				String id = filter.substring("tid:".length()); //$NON-NLS-1$
				List<CatalogCategory> catalogCategories = marketplaceWizard.getCatalog().getCategories();
				for (CatalogCategory catalogCategory : catalogCategories) {
					if (catalogCategory instanceof MarketplaceCategory) {
						MarketplaceCategory marketplaceCategory = (MarketplaceCategory) catalogCategory;
						List<? extends IMarket> markets = marketplaceCategory.getMarkets();
						for (IMarket market : markets) {
							if (id.equals(market.getId())) {
								searchMarket = market;
							} else {
								final List<? extends ICategory> categories = market.getCategory();
								for (ICategory category : categories) {
									if (id.equals(category.getId())) {
										searchCategory = category;
									}
								}
							}
						}
					}
				}
			}
		}

		marketplaceWizard.getCatalogPage().search(descriptor, searchMarket, searchCategory, searchString);
		return true;
	}

	@Override
	protected boolean handleRecent(CatalogDescriptor descriptor, String url) {
		viewer.getWizard().getCatalogPage().show(descriptor, MarketplaceViewer.ContentType.RECENT);
		return true;
	}

	@Override
	protected boolean handlePopular(CatalogDescriptor descriptor, String url) {
		viewer.getWizard().getCatalogPage().show(descriptor, MarketplaceViewer.ContentType.POPULAR);
		return true;
	}

	@Override
	protected boolean handleNode(CatalogDescriptor descriptor, String url, INode node) {
		viewer.getWizard().getCatalogPage().show(descriptor, Collections.singleton(node));
		return true;
	}

	@Override
	protected boolean handleInstallRequest(final SolutionInstallationInfo installInfo, String url) {
		if (installInfo.getInstallId() == null) {
			return false;
		}
		final MarketplaceWizard wizard = viewer.getWizard();
		return wizard.handleInstallRequest(installInfo, url);
	}

	@Override
	public void completed(ProgressEvent event) {
		updatePageLinks();
	}

	@Override
	public void changed(ProgressEvent event) {
		// ignore
	}
}
