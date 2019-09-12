package com.wavefront.opentracing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.wavefront.config.ApplicationTagsConfig;
import com.wavefront.config.WavefrontReportingConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Util methods used by test classes.
 *
 * @author Han Zhang (zhanghan@vmware.com)
 */
public class Utils {
  private static ObjectMapper mapper =
      new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

  public static File savePropertiesToTempFile(Properties props) throws IOException {
    File file = null;
    try {
      file = File.createTempFile("myconfig", "properties");
      try (FileOutputStream stream = new FileOutputStream(file)) {
        props.store(stream, "");
      }
    } catch (Exception e) {
      if (file != null) {
        file.delete();
      }
      throw e;
    }
    return file;
  }

  public static File saveToTempYamlFile(ApplicationTagsConfig config) throws IOException {
    File file = null;
    try {
      file = File.createTempFile("test-application-tags", "yaml");
      mapper.writeValue(file, config);
    } catch (Exception e) {
      if (file != null) {
        file.delete();
      }
      throw e;
    }
    return file;
  }

  public static File saveToTempYamlFile(WavefrontReportingConfig config) throws IOException {
    File file = null;
    try {
      file = File.createTempFile("test-wf-reporting-config", "yaml");
      mapper.writeValue(file, config);
    } catch (Exception e) {
      if (file != null) {
        file.delete();
      }
      throw e;
    }
    return file;
  }
}
