package com.izj.knowledge.service.common.report;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Slf4j
public class ReportServiceImpl {

    private static String RESOURCE_FOLDER = "report";

    public void createReport(ReportModel reportModel) {

        Map<String, Object> param = new HashMap<>();
        param.put("logo", "path/to/image");

    }

    public void createReport(ReportModel reportModel,
            Map<String, Object> parameters) {
        ClassPathResource resource = new ClassPathResource(new StringBuilder()
            .append(RESOURCE_FOLDER)
            .toString());

        try {
            log.debug("Print report - start: Target#{}, resource#{}", reportModel.getKey(), resource.getPath());

            JasperReport jasperReport = JasperCompileManager.compileReport(resource.getInputStream());
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Arrays.asList(reportModel));

            JasperPrint pdf = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] report = JasperExportManager.exportReportToPdf(pdf);

            log.debug("Print report - end: Used memory#{}, Target#{}, resource#{}",
                    (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()),
                    reportModel.getKey(), resource.getPath());
        } catch (JRException e) {
            throw new RuntimeException(
                    "Failed to create PDF. Resource#" + resource.getPath() + " Target#" + reportModel.getKey(), e);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to access jrxml. Resource#" + resource.getPath() + " Target#" + reportModel.getKey(), e);
        }
    }
}
