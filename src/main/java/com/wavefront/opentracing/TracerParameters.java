package com.wavefront.opentracing;

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

  public final static String DEFAULT_APP_TAGS_YAML_FILE_PATH = "application-tags.yaml";
  public final static String DEFAULT_REPORTING_YAML_FILE_PATH = "wf-reporting-config.yaml";

  public final static String APP_TAGS_YAML_FILE_KEY = "wf.applicationTagsYamlFile";
  public final static String REPORTING_YAML_FILE_KEY = "wf.reportingConfigYamlFile";

  public final static String [] ALL = {
      APP_TAGS_YAML_FILE_KEY,
      REPORTING_YAML_FILE_KEY
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

    if (!props.containsKey(APP_TAGS_YAML_FILE_KEY)) {
      props.setProperty(APP_TAGS_YAML_FILE_KEY, DEFAULT_APP_TAGS_YAML_FILE_PATH);
    }
    if (!props.containsKey(REPORTING_YAML_FILE_KEY)) {
      props.setProperty(REPORTING_YAML_FILE_KEY, DEFAULT_REPORTING_YAML_FILE_PATH);
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
}
