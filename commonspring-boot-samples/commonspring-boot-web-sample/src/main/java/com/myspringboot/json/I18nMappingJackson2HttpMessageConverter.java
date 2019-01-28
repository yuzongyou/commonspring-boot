package com.myspringboot.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/11/14 15:38
 */
public class I18nMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public I18nMappingJackson2HttpMessageConverter() {
    }

    public I18nMappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        System.out.println("结果： " + object + ", type=" + type + ", outputmessage: " + outputMessage);

        super.writeInternal(object, type, outputMessage);
    }
}
