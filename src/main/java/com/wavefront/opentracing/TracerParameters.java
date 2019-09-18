package com.wavefront.opentracing;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines tracer parameters used by {@link WavefrontTracerFactory} to build instances of
 * {@link WavefrontTracer}, and associated methods that load parameter values from system properties
 * or a configuration file.
 *
 * @author Han Zhang (zhanghan@vmware.com)
 */
public final class TracerParameters {
  private TracerParameters() {}

  private final static Logger logger = Logger.getLogger(TracerParameters.class.getName());

  public final static String DEFAULT_CUSTOM_TAGS_DELIMITER = ",";

  // YAML file parameters
  public final static String APP_TAGS_YAML_FILE = "wf.applicationTagsYamlFile";
  public final static String REPORTING_YAML_FILE = "wf.reportingConfigYamlFile";

  // Application Tag parameters
  public final static String APPLICATION = "wf.application";
  public final static String SERVICE = "wf.service";
  public final static String CLUSTER = "wf.cluster";
  public final static String SHARD = "wf.shard";
  public final static String CUSTOM_TAGS = "wf.customTags";
  public final static String CUSTOM_TAGS_FROM_ENV = "wf.customTagsFromEnv";
  public final static String CUSTOM_TAGS_DELIMITER = "wf.customTagsDelimiter";

  // Reporting parameters
  public final static String REPORTING_MECHANISM = "wf.reportingMechanism";
  public final static String SERVER = "wf.server";
  public final static String TOKEN = "wf.token";
  public final static String PROXY_HOST = "wf.proxyHost";
  public final static String PROXY_METRICS_PORT = "wf.proxyMetricsPort";
  public final static String PROXY_DISTRIBUTIONS_PORT = "wf.proxyDistributionsPort";
  public final static String PROXY_TRACING_PORT = "wf.proxyTracingPort";
  public final static String SOURCE = "wf.source";

  public final static String [] ALL = {
      APP_TAGS_YAML_FILE,
      REPORTING_YAML_FILE,
      APPLICATION,
      SERVICE,
      CLUSTER,
      SHARD,
      CUSTOM_TAGS,
      CUSTOM_TAGS_FROM_ENV,
      CUSTOM_TAGS_DELIMITER,
      REPORTING_MECHANISM,
      SERVER,
      TOKEN,
      PROXY_HOST,
      PROXY_METRICS_PORT,
      PROXY_DISTRIBUTIONS_PORT,
      PROXY_TRACING_PORT,
      SOURCE
  };

  /**
   * Loads parameters from (1) system properties, (2) a configuration file, and (3) defaults.
   *
   * @return The parameters as a map of key-value pairs.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> getParameters() {
    Properties props = Configuration.loadConfigurationFile();
    loadSystemProperties(props);

    if (!props.containsKey(CUSTOM_TAGS_DELIMITER)) {
      props.setProperty(CUSTOM_TAGS_DELIMITER, DEFAULT_CUSTOM_TAGS_DELIMITER);
    }

    for (String propName: props.stringPropertyNames()) {
      logger.log(Level.INFO,
          "Retrieved Tracer parameter " + propName + "=" + props.getProperty(propName));
    }

    // A Properties object is expected to only contain String keys/values.
    return (Map)props;
  }

  static void loadSystemProperties(Properties props) {
    for (String paramName : ALL) {
      String paramValue = System.getProperty(paramName);
      if (paramValue != null) {
        props.setProperty(paramName, paramValue);
      }
    }
  }

  static Integer toInteger(String value) {
    Integer integer = null;
    try {
      integer = Integer.valueOf(value);
    } catch (NumberFormatException e) {
      logger.log(Level.WARNING, "Failed to convert Tracer parameter value '" + value + "' to int");
    }

    return integer;
  }

  @Nullable
  static Map<String, String> toCustomTags(Map<String, String> params) {
    if (!params.containsKey(CUSTOM_TAGS) && !params.containsKey(CUSTOM_TAGS_FROM_ENV)) {
      return null;
    }

    Map<String, String> customTags = new HashMap<>();
    String delimiter = params.get(CUSTOM_TAGS_DELIMITER);
    if (params.containsKey(CUSTOM_TAGS)) {
      // ex: "tagKey1,tagVal1,tagKey2,tagVal2"
      String value = params.get(CUSTOM_TAGS);
      String[] components = value.split(delimiter);
      if (components.length % 2 == 0) {
        for (int i = 0; i < components.length; i += 2) {
          String tagKey = components[i];
          String tagVal = components[i + 1];
          if (!tagKey.isEmpty() && !tagVal.isEmpty()) {
            customTags.put(tagKey, tagVal);
          }
        }
      } else {
        logger.log(Level.WARNING, "Failed to convert Tracer parameter value '" + value +
            "' to custom tags -- key exists without value");
      }
    }
    if (params.containsKey(CUSTOM_TAGS_FROM_ENV)) {
      // ex: "envVarName1,tagKey1,envVarName2,tagKey2"
      String value = params.get(CUSTOM_TAGS_FROM_ENV);
      String[] components = value.split(delimiter);
      if (components.length % 2 == 0) {
        for (int i = 0; i < components.length; i += 2) {
          String tagKey = components[i + 1];
          String tagVal = System.getenv(components[i]);
          if (!tagKey.isEmpty() && tagVal != null && !tagVal.isEmpty()) {
            customTags.put(tagKey, tagVal);
          }
        }
      } else {
        logger.log(Level.WARNING, "Failed to convert Tracer parameter value '" + value +
            "' to custom tags from environment -- key exists without value");
      }
    }
    return customTags;
  }
}
