package com.jboss.datagrid.rhq;

import java.util.Set;

import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

public class JDGComponent extends MetricsRemappingComponent<JDGComponent> implements MeasurementFacet {

   @Override
   public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> reqs) throws Exception {
      super.getValues(report, reqs);
   }

}
