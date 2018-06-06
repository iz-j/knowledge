package com.izj.knowledge.service.base.util.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.izj.knowledge.service.base.util.json.ObjectMapperFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class can compress/decompress Object or bytes with {@link DeflaterOutputStream}.<br>
 * Also has convert method to/from Base64 String.
 * 
 * @author shirakawa_d
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZipUtils {
    public static byte[] compress(byte[] bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            throw new IllegalStateException("Can't compress bytes", e);
        }
        return baos.toByteArray();
    }

    public static byte[] compressAsJSON(Object object) {
        try {
            byte[] bytes = ObjectMapperFactory.getDefault().writeValueAsBytes(object);
            return compress(bytes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't convert object to bytes", e);
        }
    }

    public static String compressAsBase64(Object object) {
        byte[] bytes = compressAsJSON(object);
        return Base64Utils.encodeToString(bytes);
    }

    public static byte[] decompress(byte[] bytes) {
        try {
            InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Can't decompress bytes", e);
        }
    }

    public static <T> T decompressFromBase64(String base64, Class<?> valueType) {
        try {
            byte[] bytes = Base64Utils.decodeFromString(base64);
            bytes = decompress(bytes);

            @SuppressWarnings("unchecked")
            T readValue = (T)ObjectMapperFactory.getDefault().readValue(bytes, valueType);
            return readValue;
        } catch (IOException e) {
            throw new IllegalStateException("Can't convert bytes to " + valueType, e);
        }
    }
}