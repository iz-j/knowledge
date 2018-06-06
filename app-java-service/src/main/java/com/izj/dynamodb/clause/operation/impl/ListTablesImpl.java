package com.izj.dynamodb.clause.operation.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.izj.dynamodb.clause.operation.ListTables;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListTablesImpl implements ListTables {

    @Override
    public List<String> execute(AmazonDynamoDB db) {
        com.amazonaws.services.dynamodbv2.model.ListTablesResult result = db.listTables();

        List<String> tableNames = new ArrayList<>(result.getTableNames());
        String lastEval = result.getLastEvaluatedTableName();

        while (StringUtils.isNotEmpty(lastEval)) {
            log.trace("Try to get next tableNames ...");
            result = db.listTables(lastEval);
            tableNames.addAll(result.getTableNames());
            lastEval = result.getLastEvaluatedTableName();
        }

        log.trace("{} tables were listed.", tableNames.size());
        return tableNames;
    }

}
