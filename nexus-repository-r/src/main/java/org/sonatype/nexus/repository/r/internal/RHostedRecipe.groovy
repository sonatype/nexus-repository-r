/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.r.internal

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Route.Builder
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.ViewFacet
import org.sonatype.nexus.repository.view.handlers.BrowseUnsupportedHandler

import static org.sonatype.nexus.repository.r.internal.AssetKind.ARCHIVE
import static org.sonatype.nexus.repository.r.internal.AssetKind.PACKAGES

/**
 * R proxy repository recipe.
 */
@Named(RHostedRecipe.NAME)
@Singleton
class RHostedRecipe
    extends RRecipeSupport
{
  public static final String NAME = 'r-hosted'

  @Inject
  Provider<RHostedFacet> hostedFacet

  @Inject
  HostedHandlers hostedHandlers

  @Inject
  RHostedRecipe(@Named(HostedType.NAME) final Type type, @Named(RFormat.NAME) final Format format) {
    super(type, format)
  }

  @Override
  void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(securityFacet.get())
    repository.attach(configure(viewFacet.get()))
    repository.attach(httpClientFacet.get())
    repository.attach(componentMaintenanceFacet.get())
    repository.attach(hostedFacet.get())
    repository.attach(storageFacet.get())
    repository.attach(searchFacet.get())
    repository.attach(attributesFacet.get())
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder()

    builder.route(packagesGzMatcher()
        .handler(highAvailabilitySupportHandler)
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(PACKAGES))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandlers.getPackages)
        .create())

    builder.route(archiveMatcher()
        .handler(highAvailabilitySupportHandler)
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(ARCHIVE))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandlers.getArchive)
        .create())

    builder.route(uploadMatcher()
        .handler(highAvailabilitySupportHandler)
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(ARCHIVE))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandlers.putArchive)
        .create())

    builder.route(nonRFileUploadMatcher()
        .handler(hostedHandlers.nonRFileUpload)
        .create())

    builder.route(new Builder()
        .matcher(BrowseUnsupportedHandler.MATCHER)
        .handler(browseUnsupportedHandler)
        .create())

    builder.defaultHandlers(HttpHandlers.notFound())

    facet.configure(builder.create())

    return facet
  }
}
