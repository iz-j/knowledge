package com.izj.knowledge.service.common.report;

import java.util.Locale;

/**
 * This model is for create report
 *
 */
public interface ReportModel {

    String getCompanyId();

    String getKey();

    Locale getLocale();

    String getReportTypeKey();

}
