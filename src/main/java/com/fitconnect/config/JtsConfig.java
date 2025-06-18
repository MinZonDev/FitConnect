package com.fitconnect.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JtsConfig {
    @Bean
    public GeometryFactory geometryFactory() {
        // SRID 4326 tương ứng với WGS 84, hệ tọa độ chuẩn cho GPS
        return new GeometryFactory(new PrecisionModel(), 4326);
    }
}
