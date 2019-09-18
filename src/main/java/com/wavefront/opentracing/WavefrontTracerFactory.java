package com.wavefront.opentracing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.wavefront.config.ApplicationTagsConfig;
import com.wavefront.config.WavefrontReportingConfig;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wavefront.config.ReportingUtils.constructWavefrontReportingConfig;
import static com.wavefront.config.ReportingUtils.constructWavefrontSender;
import static com.wavefront.opentracing.TracerParameters.APPLICATION;
import static com.wavefront.opentracing.TracerParameters.APP_TAGS_YAML_FILE;
import static com.wavefront.opentracing.TracerParameters.CLUSTER;
import static com.wavefront.opentracing.TracerParameters.PROXY_DISTRIBUTIONS_PORT;
import static com.wavefront.opentracing.TracerParameters.PROXY_HOST;
import static com.wavefront.opentracing.TracerParameters.PROXY_METRICS_PORT;
import static com.wavefront.opentracing.TracerParameters.PROXY_TRACING_PORT;
import static com.wavefront.opentracing.TracerParameters.REPORTING_MECHANISM;
import static com.wavefront.opentracing.TracerParameters.REPORTING_YAML_FILE;
import static com.wavefront.opentracing.TracerParameters.SERVER;
import static com.wavefront.opentracing.TracerParameters.SERVICE;
import static com.wavefront.opentracing.TracerParameters.SHARD;
import static com.wavefront.opentracing.TracerParameters.SOURCE;
import static com.wavefront.opentracing.TracerParameters.TOKEN;
import static com.wavefront.opentracing.TracerParameters.toCustomTags;
import static com.wavefront.opentracing.TracerParameters.toInteger;

/**
 * Implementation of {@link TracerFactory} that builds instances of {@link WavefrontTracer}.
 *
 * @author Han Zhang (zhanghan@vmware.com)
 */
public class WavefrontTracerFactory implements TracerFactory {

  private static final Logger logger = Logger.getLogger(WavefrontTracerFactory.class.getName());

  @Override
  public Tracer getTracer()
  {
    Map<String, String> params = TracerParameters.getParameters();

    // Step 1 - Create an ApplicationTags instance, which specifies metadata about your application.
    ApplicationTagsConfig applicationTagsConfig;
    if (params.containsKey(APP_TAGS_YAML_FILE)) {
      try {
        applicationTagsConfig = constructApplicationTagsConfig(params.get(APP_TAGS_YAML_FILE));
      } catch (Exception e) {
        logger.log(Level.WARNING, "Failed to parse application tags YAML file: " + e);
        return null;
      }
    } else {
      applicationTagsConfig = new ApplicationTagsConfig();
    }
    if (params.containsKey(APPLICATION)) {
      applicationTagsConfig.setApplication(params.get(APPLICATION));
    }
    if (params.containsKey(SERVICE)) {
      applicationTagsConfig.setService(params.get(SERVICE));
    }
    if (params.containsKey(CLUSTER)) {
      applicationTagsConfig.setCluster(params.get(CLUSTER));
    }
    if (params.containsKey(SHARD)) {
      applicationTagsConfig.setShard(params.get(SHARD));
    }
    Map<String, String> customTags = toCustomTags(params);
    if (customTags != null) {
      applicationTagsConfig.setCustomTags(customTags);
    }

    ApplicationTags applicationTags = constructApplicationTags(applicationTagsConfig);

    // Step 2 - Construct WavefrontReportingConfig.
    WavefrontReportingConfig wfReportingConfig;
    if (params.containsKey(REPORTING_YAML_FILE)) {
      try {
        wfReportingConfig = constructWavefrontReportingConfig(params.get(REPORTING_YAML_FILE));
      } catch (Exception e) {
        logger.log(Level.WARNING, "Failed to create a Wavefront reporting config: " + e);
        return null;
      }
    } else {
      wfReportingConfig = new WavefrontReportingConfig();
    }
    if (params.containsKey(REPORTING_MECHANISM)) {
      wfReportingConfig.setReportingMechanism(params.get(REPORTING_MECHANISM));
    }
    if (params.containsKey(SERVER)) {
      wfReportingConfig.setServer(params.get(SERVER));
    }
    if (params.containsKey(TOKEN)) {
      wfReportingConfig.setToken(params.get(TOKEN));
    }
    if (params.containsKey(PROXY_HOST)) {
      wfReportingConfig.setProxyHost(params.get(PROXY_HOST));
    }
    if (params.containsKey(PROXY_METRICS_PORT)) {
      Integer proxyMetricsPort = toInteger(params.get(PROXY_METRICS_PORT));
      if (proxyMetricsPort != null) {
        wfReportingConfig.setProxyMetricsPort(proxyMetricsPort);
      }
    }
    if (params.containsKey(PROXY_DISTRIBUTIONS_PORT)) {
      Integer proxyDistributionsPort = toInteger(params.get(PROXY_DISTRIBUTIONS_PORT));
      if (proxyDistributionsPort != null) {
        wfReportingConfig.setProxyDistributionsPort(proxyDistributionsPort);
      }
    }
    if (params.containsKey(PROXY_TRACING_PORT)) {
      Integer proxyTracingPort = toInteger(params.get(PROXY_TRACING_PORT));
      if (proxyTracingPort != null) {
        wfReportingConfig.setProxyTracingPort(proxyTracingPort);
      }
    }
    if (params.containsKey(SOURCE)) {
      wfReportingConfig.setSource(params.get(SOURCE));
    }

    String source = wfReportingConfig.getSource();

    // Step 3 - Create a WavefrontSender for sending data to Wavefront.
    WavefrontSender wavefrontSender;
    try {
      wavefrontSender = constructWavefrontSender(wfReportingConfig);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to create a Wavefront sender: " + e);
      return null;
    }

    // Step 4 - Create a WavefrontSpanReporter for reporting trace data.
    WavefrontSpanReporter wfSpanReporter;
    try {
      wfSpanReporter =
          new WavefrontSpanReporter.Builder().withSource(source).build(wavefrontSender);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to create a Wavefront span reporter: " + e);
      return null;
    }

    // Step 5 - Create and return a WavefrontTracer.
    try {
      return new WavefrontTracer.Builder(wfSpanReporter, applicationTags).build();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to create a Wavefront Tracer: " + e);
      return null;
    }
  }

  // TODO: move to internal reporter
  private static ApplicationTagsConfig constructApplicationTagsConfig(
      String applicationTagsYamlFile) {
    YAMLFactory factory = new YAMLFactory(new ObjectMapper());
    YAMLParser parser;
    try {
      parser = factory.createParser(new File(applicationTagsYamlFile));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try {
      return parser.readValueAs(ApplicationTagsConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO: move to internal reporter
  private static ApplicationTags constructApplicationTags(
      ApplicationTagsConfig applicationTagsConfig) {
    ApplicationTags.Builder applicationTagsBuilder = new ApplicationTags.Builder(
        applicationTagsConfig.getApplication(), applicationTagsConfig.getService());

    if (applicationTagsConfig.getCluster() != null) {
      applicationTagsBuilder.cluster(applicationTagsConfig.getCluster());
    }

    if (applicationTagsConfig.getShard() != null) {
      applicationTagsBuilder.shard(applicationTagsConfig.getShard());
    }

    if (applicationTagsConfig.getCustomTags() != null) {
      applicationTagsBuilder.customTags(applicationTagsConfig.getCustomTags());
    }

    return applicationTagsBuilder.build();
  }
}
