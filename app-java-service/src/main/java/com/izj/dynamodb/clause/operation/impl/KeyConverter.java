package com.izj.dynamodb.clause.operation.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.izj.knowledge.service.base.util.json.ObjectMapperFactory;

/**
 *
 * @author ~~~~
 *
 */
final class KeyConverter {
    private static final ObjectReader READER = ObjectMapperFactory
        .getDefault()
        .readerFor(new TypeReference<Map<String, AttributeValue>>() {
        });

    private KeyConverter() {
    }

    public static Map<String, AttributeValue> toMapKey(String key) {
        if (key == null) {
            return null;
        }
        byte[] byteArr = Base64.getDecoder().decode(key);
        StringBuilder result = new StringBuilder();
        byte[] buf = new byte[5];
        int rlen = -1;
        try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(byteArr))) {
            while ((rlen = iis.read(buf)) != -1) {
                result.append(new String(Arrays.copyOf(buf, rlen)));
            }
            return READER.readValue(result.toString());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String toStringKey(Map<String, AttributeValue> key) {
        if (key == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        try {
            String str = ObjectMapperFactory.getDefault().writeValueAsString(key);
            dos.write(str.getBytes());
            dos.flush();
            dos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
