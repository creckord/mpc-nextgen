/*******************************************************************************
 * Copyright (c) 2009, 2018 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Yatta Solutions - bug 413871: performance
 *******************************************************************************/

package org.eclipse.epp.internal.mpc.ui.wizards;

import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epp.internal.mpc.core.util.TextUtil;
import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalogSource;
import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceDiscoveryStrategy;
import org.eclipse.epp.internal.mpc.ui.catalog.ResourceProvider;
import org.eclipse.epp.internal.mpc.ui.catalog.ResourceProvider.ResourceReceiver;
import org.eclipse.epp.internal.mpc.ui.util.Util;
import org.eclipse.equinox.internal.p2.discovery.model.Overview;
import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * @author David Green
 * @author Carsten Reckord
 */
class OverviewToolTip extends ToolTip {

	final int SCREENSHOT_HEIGHT = 240;

	final int SCREENSHOT_WIDTH = 320;

	private final Overview overview;

	private final MarketplaceCatalogSource source;

	private final Control parent;

	private final Image leftImage;

	private final IMarketplaceWebBrowser browser;

	public OverviewToolTip(Control control, IMarketplaceWebBrowser browser, MarketplaceCatalogSource source,
			Overview overview, Image leftImage) {
		super(control, ToolTip.RECREATE, true);
		Assert.isNotNull(source);
		Assert.isNotNull(overview);
		this.parent = control;
		this.source = source;
		this.overview = overview;
		this.leftImage = leftImage;
		this.browser = browser;
		setHideOnMouseDown(false); // required for links to work
	}

	@Override
	protected Composite createToolTipContentArea(Event event, final Composite parent) {
		Shell shell = parent.getShell();
		setData(Shell.class.getName(), shell);
		AbstractMarketplaceDiscoveryItem.setWidgetId(shell, DiscoveryItem.WIDGET_ID_OVERVIEW);
		GridLayoutFactory.fillDefaults().applyTo(parent);

		Color backgroundColor = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		final Composite container = new Composite(parent, SWT.NULL);
		container.setBackground(backgroundColor);

		boolean hasImage = false;
		if (overview.getScreenshot() != null) {
			hasImage = true;
		}
		final boolean addLearnMoreLink = browser != null && overview.getUrl() != null && overview.getUrl().length() > 0;

		final int borderWidth = 1;
		final int heightHint = SCREENSHOT_HEIGHT + (borderWidth * 2);
		final int widthHint = SCREENSHOT_WIDTH;

		final int containerWidthHintWithImage = 650;
		final int containerWidthHintWithoutImage = 500;

		GridDataFactory.fillDefaults().grab(true, true).hint(
				hasImage ? containerWidthHintWithImage : containerWidthHintWithoutImage, SWT.DEFAULT)
		.applyTo(
				container);

		GridLayoutFactory.fillDefaults().numColumns((leftImage != null) ? 3 : 2).margins(5, 5).spacing(3, 0).applyTo(
				container);

		if (leftImage != null) {
			Label imageLabel = new Label(container, SWT.NONE);
			imageLabel.setImage(leftImage);
			imageLabel.setBackground(backgroundColor);
			int imageWidthHint = leftImage.getBounds().width + 5;
			GridDataFactory.fillDefaults()
			.align(SWT.BEGINNING, SWT.BEGINNING)
			.hint(imageWidthHint, SWT.DEFAULT)
			.applyTo(imageLabel);
		}

		String summary = overview.getSummary();

		Composite summaryContainer = new Composite(container, SWT.NULL);
		summaryContainer.setBackground(backgroundColor);
		GridLayoutFactory.fillDefaults().applyTo(summaryContainer);

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults()
				.grab(true, true)
				.span(hasImage ? 1 : 2, 1);
		if (hasImage) {
			gridDataFactory.hint(widthHint, heightHint);
		}
		gridDataFactory.applyTo(summaryContainer);

		Browser summaryLabel = new Browser(summaryContainer, SWT.NULL);

		Font dialogFont = JFaceResources.getDialogFont();
		FontData[] fontData = dialogFont.getFontData();
		String attr = ""; //$NON-NLS-1$
		String fontSizeUnitOfMeasure = "pt"; //$NON-NLS-1$
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			fontSizeUnitOfMeasure = "px"; //$NON-NLS-1$
		} else if (Platform.OS_WIN32.equals(Platform.getOS())) {
			attr = "overflow: auto; "; //$NON-NLS-1$
		}
		String cssStyle = "body, p, div, *  {" + attr + "font-family:\"" + fontData[0].getName() //$NON-NLS-1$ //$NON-NLS-2$
				+ "\",Arial,sans-serif !important;font-size:" + fontData[0].getHeight() + fontSizeUnitOfMeasure + " !important;" //$NON-NLS-1$ //$NON-NLS-2$
				+ "} body { margin: 0px; background-color: white;}"; //$NON-NLS-1$
		summaryLabel.setFont(dialogFont);
		String html = "<html><style>" + cssStyle + "</style><body>" + TextUtil.cleanInformalHtmlMarkup(summary) //$NON-NLS-1$//$NON-NLS-2$
		+ "</body></html>"; //$NON-NLS-1$
		summaryLabel.setText(html);
		summaryLabel.setBackground(backgroundColor);
		// instead of opening links in the tooltip, open a new browser window
		summaryLabel.addLocationListener(new LocationListener() {
			@Override
			public void changing(LocationEvent event) {
				if (event.location.equals("about:blank")) { //$NON-NLS-1$
					return;
				}
				event.doit = false;
				OverviewToolTip.this.hide();
				WorkbenchUtil.openUrl(event.location, IWorkbenchBrowserSupport.AS_EXTERNAL);
			}

			@Override
			public void changed(LocationEvent event) {
			}
		});

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(SWT.DEFAULT,
				hasImage ? SWT.DEFAULT : SCREENSHOT_HEIGHT)
		.applyTo(summaryLabel);

		if (hasImage) {
			final Composite imageContainer = new Composite(container, SWT.BORDER);
			GridLayoutFactory.fillDefaults().applyTo(imageContainer);

			GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.BEGINNING).hint(
					widthHint + (borderWidth * 2), heightHint).applyTo(imageContainer);

			Label imageLabel = new Label(imageContainer, SWT.NULL);
			GridDataFactory.fillDefaults().hint(widthHint, SCREENSHOT_HEIGHT).indent(borderWidth, borderWidth).applyTo(
					imageLabel);
			imageLabel.setBackground(backgroundColor);
			imageLabel.setSize(widthHint, SCREENSHOT_HEIGHT);

			provideImage(imageLabel, source, overview.getScreenshot());

			final Cursor handCursor = new Cursor(imageLabel.getDisplay(), SWT.CURSOR_HAND);
			imageLabel.setCursor(handCursor);
			imageLabel.addDisposeListener(e -> handCursor.dispose());
			imageLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					OverviewToolTip.this.hide();
					WorkbenchUtil.openUrl(overview.getScreenshot(), IWorkbenchBrowserSupport.AS_EXTERNAL);
				}
			});

			// creates a border
			imageContainer.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		}
		if (addLearnMoreLink) {
			Link link = new Link(summaryContainer, SWT.NULL);
			AbstractMarketplaceDiscoveryItem.setWidgetId(link, DiscoveryItem.WIDGET_ID_LEARNMORE);
			GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(link);
			link.setText(Messages.OverviewToolTip_learnMoreLink);
			link.setBackground(backgroundColor);
			link.setToolTipText(NLS.bind(Messages.OverviewToolTip_openUrlInBrowser, overview.getUrl()));
			link.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					OverviewToolTip.this.hide();
					browser.openUrl(overview.getUrl());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
		}
		if (!hasImage) {
			// prevent overviews with no image from providing unlimited text.
			Point optimalSize = summaryContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			if (optimalSize.y > (heightHint + 10)) {
				((GridData) summaryContainer.getLayoutData()).heightHint = heightHint;
				container.layout(true);
			}
		}
		// hack: cause the tooltip to gain focus so that we can capture the escape key
		//       this must be done async since the tooltip is not yet visible.
		Display.getCurrent().asyncExec(() -> {
			if (!parent.isDisposed()) {
				parent.setFocus();
			}
		});
		return container;
	}

	@Override
	protected void afterHideToolTip(Event event) {
		setData(Shell.class.getName(), null);
	}

	private void provideImage(final Label imageLabel, MarketplaceCatalogSource discoverySource, final String imagePath) {
		ResourceProvider resourceProvider = discoverySource.getResourceProvider();
		MarketplaceDiscoveryStrategy.cacheResource(resourceProvider, overview.getItem(), imagePath);
		resourceProvider.provideResource(new ResourceReceiver<ImageDescriptor>() {

			@Override
			public ImageDescriptor processResource(URL resource) {
				return ImageDescriptor.createFromURL(resource);
			}

			@Override
			public void setResource(final ImageDescriptor resource) {
				if (resource != null && imageLabel != null && !imageLabel.isDisposed()) {
					imageLabel.getDisplay().asyncExec(() -> {
						if (!imageLabel.isDisposed()) {
							try {
								Image image = resource.createImage();
								if (image != null) {
									Rectangle imageBounds = image.getBounds();
									if (imageBounds.width > SCREENSHOT_WIDTH
											|| imageBounds.height > SCREENSHOT_HEIGHT) {
										final Image scaledImage = Util.scaleImage(image, SCREENSHOT_WIDTH,
												SCREENSHOT_HEIGHT);
										Image originalImage = image;
										image = scaledImage;
										originalImage.dispose();
									}
									final Image fimage = image;
									imageLabel.addDisposeListener(e -> fimage.dispose());
									imageLabel.setImage(image);
								}
							} catch (SWTException e) {
								// ignore, probably a bad image format
								MarketplaceClientUi.error(NLS.bind(Messages.OverviewToolTip_cannotRenderImage_reason,
										imagePath, e.getMessage()), e);
							}
						}
					});
				}
			}
		}, imagePath, null);
	}

	public void show(Control titleControl) {
		Point titleAbsLocation = titleControl.getParent().toDisplay(titleControl.getLocation());
		Point containerAbsLocation = parent.getParent().toDisplay(parent.getLocation());
		Rectangle bounds = titleControl.getBounds();
		int relativeX = titleAbsLocation.x - containerAbsLocation.x;
		int relativeY = titleAbsLocation.y - containerAbsLocation.y;

		if (org.eclipse.jface.util.Util.isGtk()) {
			//GTK sends MOUSE_EXIT on entering the tooltip shell, closing it (bug 423908)
			//Workaround: open tooltip under cursor
			GC gc = new GC(titleControl);
			try {
				gc.setFont(titleControl.getFont());
				int height = gc.getFontMetrics().getHeight();
				relativeY += bounds.height - height;
			} finally {
				gc.dispose();
			}
		} else {
			relativeY += bounds.height + 3;
		}
		show(new Point(relativeX, relativeY));
	}
}
