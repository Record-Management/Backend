package com.recordmanagement.habitlog.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;

/**
 * LocalDate를 [year, month, day] 배열 형태로 직렬화하는 커스텀 시리얼라이저
 */
public class LocalDateArraySerializer extends JsonSerializer<LocalDate> {
    
    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        
        gen.writeStartArray();
        gen.writeNumber(value.getYear());
        gen.writeNumber(value.getMonthValue());
        gen.writeNumber(value.getDayOfMonth());
        gen.writeEndArray();
    }
}