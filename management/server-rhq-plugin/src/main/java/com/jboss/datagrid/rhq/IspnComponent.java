package com.jboss.datagrid.rhq;

import java.util.Set;

import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

/**
 * Component class for Infinispan
 * @author Heiko W. Rupp
 */
public class IspnComponent extends MetricsRemappingComponent<IspnComponent> implements MeasurementFacet{
   @Override
   public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> reqs) throws Exception {
      super.getValues(report, reqs);
   }

}
