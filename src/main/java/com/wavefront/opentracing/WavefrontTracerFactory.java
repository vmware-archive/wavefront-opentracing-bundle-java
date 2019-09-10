package com.wavefront.opentracing;

import com.wavefront.config.WavefrontReportingConfig;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerFactory;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wavefront.config.ReportingUtils.constructApplicationTags;
import static com.wavefront.config.ReportingUtils.constructWavefrontReportingConfig;
import static com.wavefront.config.ReportingUtils.constructWavefrontSender;
import static com.wavefront.opentracing.TracerParameters.APP_TAGS_YAML_FILE_KEY;
import static com.wavefront.opentracing.TracerParameters.REPORTING_YAML_FILE_KEY;

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
    ApplicationTags applicationTags;
    try {
      applicationTags = constructApplicationTags(params.get(APP_TAGS_YAML_FILE_KEY));
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to create application tags: " + e);
      return null;
    }

    // Step 2 - Construct WavefrontReportingConfig.
    WavefrontReportingConfig wfReportingConfig;
    try {
      wfReportingConfig = constructWavefrontReportingConfig(params.get(REPORTING_YAML_FILE_KEY));
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to create a Wavefront reporting config: " + e);
      return null;
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
}
