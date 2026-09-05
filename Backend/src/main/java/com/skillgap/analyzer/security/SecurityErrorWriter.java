package com.skillgap.analyzer.security;

import com.skillgap.analyzer.dto.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SecurityErrorWriter {
    private final JsonMapper mapper;
    public SecurityErrorWriter(JsonMapper mapper) { this.mapper = mapper; }
    public void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), new ApiError(status, message, Map.of()));
    }
}
