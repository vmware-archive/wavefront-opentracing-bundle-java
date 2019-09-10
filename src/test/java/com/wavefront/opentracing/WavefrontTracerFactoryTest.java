package com.wavefront.opentracing;

import com.wavefront.config.ApplicationTagsConfig;
import com.wavefront.config.WavefrontReportingConfig;
import io.opentracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.wavefront.config.WavefrontReportingConfig.directReporting;
import static com.wavefront.config.WavefrontReportingConfig.proxyReporting;
import static com.wavefront.opentracing.TracerParameters.APP_TAGS_YAML_FILE_KEY;
import static com.wavefront.opentracing.TracerParameters.REPORTING_YAML_FILE_KEY;
import static com.wavefront.opentracing.Utils.saveToTempYamlFile;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link WavefrontTracerFactory}.
 *
 * @author Han Zhang (zhanghan@vmware.com)
 */
public class WavefrontTracerFactoryTest {
  private Tracer tracer;
  private File applicationTagsFile;
  private File wfReportingConfigFile;

  @BeforeEach
  public void beforeTest() {
    // Clear all the parameters.
    System.clearProperty(Configuration.CONFIGURATION_FILE_KEY);
    for (String paramName : TracerParameters.ALL) {
      System.clearProperty(paramName);
    }
  }

  @AfterEach
  public void afterTest() {
    if (tracer != null) {
      ((WavefrontTracer) tracer).close();
      tracer = null;
    }
    if (applicationTagsFile != null) {
      applicationTagsFile.delete();
      applicationTagsFile = null;
    }
    if (wfReportingConfigFile != null) {
      wfReportingConfigFile.delete();
      wfReportingConfigFile = null;
    }
  }

  @Test
  public void getTracer_withProxySender() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE_KEY, applicationTagsFile.getAbsolutePath());

    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfig.setReportingMechanism(proxyReporting);
    wfReportingConfig.setProxyHost("test-host");
    wfReportingConfig.setProxyMetricsPort(0);
    wfReportingConfigFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE_KEY, wfReportingConfigFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertTrue(tracer instanceof WavefrontTracer);
  }

  @Test
  public void getTracer_withDirectSender() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE_KEY, applicationTagsFile.getAbsolutePath());

    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfig.setReportingMechanism(directReporting);
    wfReportingConfig.setServer("test-server");
    wfReportingConfig.setToken("test-token");
    wfReportingConfigFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE_KEY, wfReportingConfigFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertTrue(tracer instanceof WavefrontTracer);
  }

  @Test
  public void getTracer_withInvalidReportingConfig() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE_KEY, applicationTagsFile.getAbsolutePath());

    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfig.setReportingMechanism("invalid");
    wfReportingConfigFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE_KEY, wfReportingConfigFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertNull(tracer);
  }

  @Test
  public void getTracer_withNoApplicationTags() throws IOException {
    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfig.setReportingMechanism("direct");
    wfReportingConfig.setServer("test-server");
    wfReportingConfig.setToken("test-token");
    wfReportingConfigFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE_KEY, wfReportingConfigFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertNull(tracer);
  }

  @Test
  public void getTracer_withNoReportingConfig() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE_KEY, applicationTagsFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertNull(tracer);
  }
}
