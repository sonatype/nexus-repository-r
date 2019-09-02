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
package org.sonatype.nexus.plugins.r.internal;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.plugins.r.internal.fixtures.RepositoryRuleR;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.tika.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.apache.commons.compress.compressors.CompressorStreamFactory.GZIP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;

public class RHostedIT
    extends RITSupport
{
  private RClient client;

  private Repository repository;

  @Rule
  public RepositoryRuleR repos = new RepositoryRuleR(() -> repositoryManager);

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-r")
    );
  }

  @Before
  public void setUp() throws Exception {
    BaseUrlHolder.set(this.nexusUrl.toString());
    repository = repos.createRHosted("r-hosted-test");
    client = createRHostedClient(repository);
  }

  @Test
  public void testPullingPackages() throws Exception
  {
    assertThat(getAllComponents(repository), hasSize(0));
    final File file = testData.resolveFile(AGRICOLAE_PKG_FILE_NAME_131_TGZ);
    client.put(AGRICOLAE_PATH_FULL_131_TGZ, new ByteArrayEntity(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));

    Component component = findComponent(repository, AGRICOLAE_PKG_NAME);
    assertThat(component.name(), is(equalTo(AGRICOLAE_PKG_NAME)));
    assertThat(component.version(), is(equalTo(AGRICOLAE_PKG_VERSION_131)));
  }

  @Test
  public void testPullingMetadata() throws Exception
  {
    assertThat(getAllComponents(repository), hasSize(0));

    final File file = testData.resolveFile(AGRICOLAE_PKG_FILE_NAME_131_TGZ);
    client.put(AGRICOLAE_PATH_FULL_131_TGZ, new ByteArrayEntity(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
    assertThat(getAllComponents(repository), hasSize(1));
    HttpResponse resp = client.fetch(PACKAGES_PATH_FULL);
    assertThat(resp.getEntity().getContentLength(), notNullValue());
    assertThat(resp.getEntity().getContentType().getValue(), equalTo(CONTENT_TYPE_GZIP));
  }

  @Test
  public void testGeneratedMetadataIsCorrect() throws Exception
  {
    final File expectedPackaFile = testData.resolveFile(PACKAGES_AGRICOLAE_131_NAME);
    final String expectedPackageData =
        Files.readAllLines(expectedPackaFile.toPath())
            .stream().collect(Collectors.joining(System.lineSeparator()));

    assertThat(getAllComponents(repository), hasSize(0));

    final File file = testData.resolveFile(AGRICOLAE_PKG_FILE_NAME_131_TGZ);
    client.put(AGRICOLAE_PATH_FULL_131_TGZ, new ByteArrayEntity(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
    assertThat(getAllComponents(repository), hasSize(1));

    final InputStream content = client.fetch(PACKAGES_PATH_FULL).getEntity().getContent();
    try (InputStream cin = new CompressorStreamFactory().createCompressorInputStream(GZIP, content)) {
      final String downloadedPackageData = IOUtils.toString(cin);
      assertThat(downloadedPackageData, is(equalTo(expectedPackageData)));
    }
  }
}