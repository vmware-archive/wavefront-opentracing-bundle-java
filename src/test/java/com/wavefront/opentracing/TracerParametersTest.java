package com.wavefront.opentracing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import static com.wavefront.opentracing.TracerParameters.APP_TAGS_YAML_FILE_KEY;
import static com.wavefront.opentracing.TracerParameters.DEFAULT_APP_TAGS_YAML_FILE_PATH;
import static com.wavefront.opentracing.TracerParameters.DEFAULT_REPORTING_YAML_FILE_PATH;
import static com.wavefront.opentracing.TracerParameters.REPORTING_YAML_FILE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link TracerParameters}.
 *
 * @author Han Zhang (zhanghan@vmware.com)
 */
public class TracerParametersTest {
  private final static String APP_TAGS_YAML_FILE_PATH = "/etc/application-tags.yaml";
  private final static String REPORTING_YAML_FILE_PATH = "/etc/wf-reporting-config.yaml";

  @BeforeEach
  public void beforeTest() {
    // Clear all the parameters.
    System.clearProperty(Configuration.CONFIGURATION_FILE_KEY);
    for (String paramName : TracerParameters.ALL) {
      System.clearProperty(paramName);
    }
  }

  @Test
  public void getParameters_fromSystemProperties() {
    System.setProperty(APP_TAGS_YAML_FILE_KEY, APP_TAGS_YAML_FILE_PATH);
    System.setProperty(REPORTING_YAML_FILE_KEY, REPORTING_YAML_FILE_PATH);

    Map<String, String> params = TracerParameters.getParameters();
    assertNotNull(params);
    assertEquals(APP_TAGS_YAML_FILE_PATH, params.get(APP_TAGS_YAML_FILE_KEY));
    assertEquals(REPORTING_YAML_FILE_PATH, params.get(REPORTING_YAML_FILE_KEY));
  }

  @Test
  public void getParameters_fromConfigurationFile() throws Exception {
    Properties props = new Properties();
    props.setProperty(APP_TAGS_YAML_FILE_KEY, APP_TAGS_YAML_FILE_PATH);
    props.setProperty(REPORTING_YAML_FILE_KEY, REPORTING_YAML_FILE_PATH);

    File file = null;
    try {
      file = Utils.savePropertiesToTempFile(props);
      System.setProperty(Configuration.CONFIGURATION_FILE_KEY, file.getAbsolutePath());

      Map<String, String> params = TracerParameters.getParameters();
      assertNotNull(params);
      assertEquals(APP_TAGS_YAML_FILE_PATH, params.get(APP_TAGS_YAML_FILE_KEY));
      assertEquals(REPORTING_YAML_FILE_PATH, params.get(REPORTING_YAML_FILE_KEY));

    } finally {
      if (file != null) {
        file.delete();
      }
    }
  }

  @Test
  public void getParameters_fromDefaults() {
    Map<String, String> params = TracerParameters.getParameters();
    assertNotNull(params);
    assertEquals(DEFAULT_APP_TAGS_YAML_FILE_PATH, params.get(APP_TAGS_YAML_FILE_KEY));
    assertEquals(DEFAULT_REPORTING_YAML_FILE_PATH, params.get(REPORTING_YAML_FILE_KEY));
  }
}
