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
package org.sonatype.nexus.repository.r.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;

import org.sonatype.nexus.repository.r.api.RHostedRepositoryApiRequest;
import org.sonatype.nexus.repository.rest.api.AbstractRepositoryApiRequestToConfigurationConverter;
import org.sonatype.nexus.repository.rest.api.AuthorizingRepositoryManager;
import org.sonatype.nexus.repository.rest.api.RepositoriesApiResourceV1;

@Named
@Singleton
@Path(RepositoriesApiResourceV1.RESOURCE_URI + "/r/hosted")
public class RHostedRepositoriesApiResourceV1
    extends RHostedRepositoriesApiResource
{
  @Inject
  public RHostedRepositoriesApiResourceV1(final AuthorizingRepositoryManager authorizingRepositoryManager,
                                          final AbstractRepositoryApiRequestToConfigurationConverter<RHostedRepositoryApiRequest> configurationAdapter)
  {
    super(authorizingRepositoryManager, configurationAdapter);
  }
}
