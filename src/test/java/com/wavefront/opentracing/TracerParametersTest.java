package com.wavefront.opentracing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import static com.wavefront.opentracing.TracerParameters.getParameters;
import static com.wavefront.opentracing.TracerParameters.toCustomTags;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

/**
 * Tests for {@link TracerParameters}.
 *
 * @author Han Zhang (zhanghan@vmware.com)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TracerParameters.class)
public class TracerParametersTest {
  private final static String APP_TAGS_YAML_FILE = "/etc/application-tags.yaml";
  private final static String REPORTING_YAML_FILE = "/etc/wf-reporting-config.yaml";

  public final static String APPLICATION = "application";
  public final static String SERVICE = "service";
  public final static String CLUSTER = "cluster";
  public final static String SHARD = "shard";
  public final static String CUSTOM_TAGS = "env|prod";
  public final static String CUSTOM_TAGS_FROM_ENV = "location|loc";
  public final static String CUSTOM_TAGS_DELIMITER = "|";

  public final static String REPORTING_MECHANISM = "direct";
  public final static String SERVER = "server";
  public final static String TOKEN = "token";
  public final static String PROXY_HOST = "host";
  public final static String PROXY_METRICS_PORT = "2878";
  public final static String PROXY_DISTRIBUTIONS_PORT = "40000";
  public final static String PROXY_TRACING_PORT = "30000";
  public final static String SOURCE = "source";

  @Before
  public void beforeTest() {
    // Clear all the parameters.
    System.clearProperty(Configuration.CONFIGURATION_FILE_KEY);
    for (String paramName : TracerParameters.ALL) {
      System.clearProperty(paramName);
    }
  }

  @Test
  public void getParameters_fromSystemProperties() {
    System.setProperty(TracerParameters.APP_TAGS_YAML_FILE, APP_TAGS_YAML_FILE);
    System.setProperty(TracerParameters.REPORTING_YAML_FILE, REPORTING_YAML_FILE);

    System.setProperty(TracerParameters.APPLICATION, APPLICATION);
    System.setProperty(TracerParameters.SERVICE, SERVICE);
    System.setProperty(TracerParameters.CLUSTER, CLUSTER);
    System.setProperty(TracerParameters.SHARD, SHARD);
    System.setProperty(TracerParameters.CUSTOM_TAGS, CUSTOM_TAGS);
    System.setProperty(TracerParameters.CUSTOM_TAGS_FROM_ENV, CUSTOM_TAGS_FROM_ENV);
    System.setProperty(TracerParameters.CUSTOM_TAGS_DELIMITER, CUSTOM_TAGS_DELIMITER);

    System.setProperty(TracerParameters.REPORTING_MECHANISM, REPORTING_MECHANISM);
    System.setProperty(TracerParameters.SERVER, SERVER);
    System.setProperty(TracerParameters.TOKEN, TOKEN);
    System.setProperty(TracerParameters.PROXY_HOST, PROXY_HOST);
    System.setProperty(TracerParameters.PROXY_METRICS_PORT, PROXY_METRICS_PORT);
    System.setProperty(TracerParameters.PROXY_DISTRIBUTIONS_PORT, PROXY_DISTRIBUTIONS_PORT);
    System.setProperty(TracerParameters.PROXY_TRACING_PORT, PROXY_TRACING_PORT);
    System.setProperty(TracerParameters.SOURCE, SOURCE);

    assertValidParameters(getParameters());
  }

  @Test
  public void getParameters_fromConfigurationFile() throws Exception {
    Properties props = new Properties();
    props.setProperty(TracerParameters.APP_TAGS_YAML_FILE, APP_TAGS_YAML_FILE);
    props.setProperty(TracerParameters.REPORTING_YAML_FILE, REPORTING_YAML_FILE);

    props.setProperty(TracerParameters.APPLICATION, APPLICATION);
    props.setProperty(TracerParameters.SERVICE, SERVICE);
    props.setProperty(TracerParameters.CLUSTER, CLUSTER);
    props.setProperty(TracerParameters.SHARD, SHARD);
    props.setProperty(TracerParameters.CUSTOM_TAGS, CUSTOM_TAGS);
    props.setProperty(TracerParameters.CUSTOM_TAGS_FROM_ENV, CUSTOM_TAGS_FROM_ENV);
    props.setProperty(TracerParameters.CUSTOM_TAGS_DELIMITER, CUSTOM_TAGS_DELIMITER);

    props.setProperty(TracerParameters.REPORTING_MECHANISM, REPORTING_MECHANISM);
    props.setProperty(TracerParameters.SERVER, SERVER);
    props.setProperty(TracerParameters.TOKEN, TOKEN);
    props.setProperty(TracerParameters.PROXY_HOST, PROXY_HOST);
    props.setProperty(TracerParameters.PROXY_METRICS_PORT, PROXY_METRICS_PORT);
    props.setProperty(TracerParameters.PROXY_DISTRIBUTIONS_PORT, PROXY_DISTRIBUTIONS_PORT);
    props.setProperty(TracerParameters.PROXY_TRACING_PORT, PROXY_TRACING_PORT);
    props.setProperty(TracerParameters.SOURCE, SOURCE);

    File file = null;
    try {
      file = Utils.savePropertiesToTempFile(props);
      System.setProperty(Configuration.CONFIGURATION_FILE_KEY, file.getAbsolutePath());

      assertValidParameters(getParameters());
    } finally {
      if (file != null) {
        file.delete();
      }
    }
  }

  @Test
  public void testToCustomTags() {
    System.setProperty(TracerParameters.CUSTOM_TAGS, "env,prod,id,,location,SF");
    System.setProperty(TracerParameters.CUSTOM_TAGS_DELIMITER, ",");
    Map<String, String> customTags = toCustomTags(getParameters());
    assertNotNull(customTags);
    assertEquals(2, customTags.size());
    assertEquals("prod", customTags.get("env"));
    assertEquals("SF", customTags.get("location"));
  }

  @Test
  public void testToCustomTags_NoProperty() {
    Map<String, String> customTags = toCustomTags(getParameters());
    assertNull(customTags);
  }

  @Test
  public void testToCustomTags_InvalidValue() {
    System.setProperty(TracerParameters.CUSTOM_TAGS, "env,prod,id");
    System.setProperty(TracerParameters.CUSTOM_TAGS_DELIMITER, ",");
    Map<String, String> customTags = toCustomTags(getParameters());
    assertNotNull(customTags);
    assertEquals(0, customTags.size());
  }

  @Test
  public void testToCustomTags_FromEnv() {
    System.setProperty(TracerParameters.CUSTOM_TAGS_FROM_ENV, "env,env,id,id,location,loc");
    mockStaticPartial(TracerParameters.class, "getEnvVariable");
    expect(TracerParameters.getEnvVariable("env")).andReturn("dev");
    expect(TracerParameters.getEnvVariable("id")).andReturn(null);
    expect(TracerParameters.getEnvVariable("location")).andReturn("PA");
    replay(TracerParameters.class);
    Map<String, String> customTags = toCustomTags(getParameters());
    verify(TracerParameters.class);
    assertNotNull(customTags);
    assertEquals(2, customTags.size());
    assertEquals("dev", customTags.get("env"));
    assertEquals("PA", customTags.get("loc"));
  }

  private static void assertValidParameters(Map<String, String> params) {
    assertNotNull(params);

    assertEquals(APP_TAGS_YAML_FILE, params.get(TracerParameters.APP_TAGS_YAML_FILE));
    assertEquals(REPORTING_YAML_FILE, params.get(TracerParameters.REPORTING_YAML_FILE));

    assertEquals(APPLICATION, params.get(TracerParameters.APPLICATION));
    assertEquals(SERVICE, params.get(TracerParameters.SERVICE));
    assertEquals(CLUSTER, params.get(TracerParameters.CLUSTER));
    assertEquals(SHARD, params.get(TracerParameters.SHARD));
    assertEquals(CUSTOM_TAGS, params.get(TracerParameters.CUSTOM_TAGS));
    assertEquals(CUSTOM_TAGS_FROM_ENV, params.get(TracerParameters.CUSTOM_TAGS_FROM_ENV));
    assertEquals(CUSTOM_TAGS_DELIMITER, params.get(TracerParameters.CUSTOM_TAGS_DELIMITER));

    assertEquals(REPORTING_MECHANISM, params.get(TracerParameters.REPORTING_MECHANISM));
    assertEquals(SERVER, params.get(TracerParameters.SERVER));
    assertEquals(TOKEN, params.get(TracerParameters.TOKEN));
    assertEquals(PROXY_HOST, params.get(TracerParameters.PROXY_HOST));
    assertEquals(PROXY_METRICS_PORT, params.get(TracerParameters.PROXY_METRICS_PORT));
    assertEquals(PROXY_DISTRIBUTIONS_PORT, params.get(TracerParameters.PROXY_DISTRIBUTIONS_PORT));
    assertEquals(PROXY_TRACING_PORT, params.get(TracerParameters.PROXY_TRACING_PORT));
    assertEquals(SOURCE, params.get(TracerParameters.SOURCE));
  }
}
