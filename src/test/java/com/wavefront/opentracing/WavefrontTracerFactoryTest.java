package com.wavefront.opentracing;

import com.wavefront.config.ApplicationTagsConfig;
import com.wavefront.config.WavefrontReportingConfig;
import io.opentracing.Tracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.wavefront.config.WavefrontReportingConfig.directReporting;
import static com.wavefront.config.WavefrontReportingConfig.proxyReporting;
import static com.wavefront.opentracing.TracerParameters.APP_TAGS_YAML_FILE;
import static com.wavefront.opentracing.TracerParameters.REPORTING_YAML_FILE;
import static com.wavefront.opentracing.Utils.savePropertiesToTempFile;
import static com.wavefront.opentracing.Utils.saveToTempYamlFile;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link WavefrontTracerFactory}.
 *
 * @author Han Zhang (zhanghan@vmware.com)
 */
public class WavefrontTracerFactoryTest {
  private Tracer tracer;
  private File configurationFile;
  private File applicationTagsYamlFile;
  private File wfReportingConfigYamlFile;

  @Before
  public void beforeTest() {
    // Clear all the parameters.
    System.clearProperty(Configuration.CONFIGURATION_FILE_KEY);
    for (String paramName : TracerParameters.ALL) {
      System.clearProperty(paramName);
    }
  }

  @After
  public void afterTest() {
    if (tracer != null) {
      tracer.close();
      tracer = null;
    }
    if (configurationFile != null) {
      configurationFile.delete();
      configurationFile = null;
    }
    if (applicationTagsYamlFile != null) {
      applicationTagsYamlFile.delete();
      applicationTagsYamlFile = null;
    }
    if (wfReportingConfigYamlFile != null) {
      wfReportingConfigYamlFile.delete();
      wfReportingConfigYamlFile = null;
    }
  }

  @Test
  public void getTracer_withProxySender() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsYamlFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE, applicationTagsYamlFile.getAbsolutePath());

    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfig.setReportingMechanism(proxyReporting);
    wfReportingConfig.setProxyHost("test-host");
    wfReportingConfig.setProxyMetricsPort(0);
    wfReportingConfigYamlFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE, wfReportingConfigYamlFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertTrue(tracer instanceof WavefrontTracer);
  }

  @Test
  public void getTracer_withDirectSender() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsYamlFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE, applicationTagsYamlFile.getAbsolutePath());

    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfig.setReportingMechanism(directReporting);
    wfReportingConfig.setServer("test-server");
    wfReportingConfig.setToken("test-token");
    wfReportingConfigYamlFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE, wfReportingConfigYamlFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertTrue(tracer instanceof WavefrontTracer);
  }

  @Test
  public void getTracer_withNoReportingMechanism() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsYamlFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE, applicationTagsYamlFile.getAbsolutePath());

    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfigYamlFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE, wfReportingConfigYamlFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertNull(tracer);
  }

  @Test
  public void getTracer_withInvalidReportingMechanism() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsYamlFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE, applicationTagsYamlFile.getAbsolutePath());

    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfig.setReportingMechanism("invalid");
    wfReportingConfigYamlFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE, wfReportingConfigYamlFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertNull(tracer);
  }

  @Test
  public void getTracer_withInvalidApplicationTagsYamlPath() throws IOException {
    System.setProperty(APP_TAGS_YAML_FILE, "invalidPath");

    WavefrontReportingConfig wfReportingConfig = new WavefrontReportingConfig();
    wfReportingConfig.setReportingMechanism("direct");
    wfReportingConfig.setServer("test-server");
    wfReportingConfig.setToken("test-token");
    wfReportingConfigYamlFile = saveToTempYamlFile(wfReportingConfig);
    System.setProperty(REPORTING_YAML_FILE, wfReportingConfigYamlFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertNull(tracer);
  }

  @Test
  public void getTracer_withInvalidReportingConfigYamlPath() throws IOException {
    ApplicationTagsConfig applicationTagsConfig = new ApplicationTagsConfig();
    applicationTagsConfig.setApplication("test-app");
    applicationTagsConfig.setService("test-service");
    applicationTagsYamlFile = saveToTempYamlFile(applicationTagsConfig);
    System.setProperty(APP_TAGS_YAML_FILE, applicationTagsYamlFile.getAbsolutePath());

    System.setProperty(REPORTING_YAML_FILE, "invalidPath");

    tracer = new WavefrontTracerFactory().getTracer();
    assertNull(tracer);
  }

  @Test
  public void getTracer_fromConfigurationFile() throws IOException {
    Properties props = new Properties();
    props.setProperty(TracerParameters.APPLICATION, "test-app");
    props.setProperty(TracerParameters.SERVICE, "test-service");
    props.setProperty(TracerParameters.REPORTING_MECHANISM, "direct");
    props.setProperty(TracerParameters.SERVER, "test-server");
    props.setProperty(TracerParameters.TOKEN, "test-token");
    configurationFile = savePropertiesToTempFile(props);
    System.setProperty(Configuration.CONFIGURATION_FILE_KEY, configurationFile.getAbsolutePath());

    tracer = new WavefrontTracerFactory().getTracer();
    assertTrue(tracer instanceof WavefrontTracer);
  }

  @Test
  public void getTracer_fromSystemProperties() {
    System.setProperty(TracerParameters.APPLICATION, "test-app");
    System.setProperty(TracerParameters.SERVICE, "test-service");
    System.setProperty(TracerParameters.REPORTING_MECHANISM, "direct");
    System.setProperty(TracerParameters.SERVER, "test-server");
    System.setProperty(TracerParameters.TOKEN, "test-token");

    tracer = new WavefrontTracerFactory().getTracer();
    assertTrue(tracer instanceof WavefrontTracer);
  }
}
