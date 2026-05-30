package com.technokratos.agona.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return sanitize(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        return Arrays.stream(values)
                .map(this::sanitize)
                .toArray(String[]::new);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> original = super.getParameterMap();
        Map<String, String[]> sanitized = new LinkedHashMap<>();
        original.forEach((key, values) ->
                sanitized.put(key, Arrays.stream(values)
                        .map(this::sanitize)
                        .toArray(String[]::new))
        );
        return java.util.Collections.unmodifiableMap(sanitized);
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        value = value.replace("\0", "");
        value = value.replaceAll("(?is)<script[^>]*>.*?</script>", "");
        value = value.replaceAll("(?i)<script[^>]*>", "");
        value = value.replaceAll("(?i)</script>", "");
        value = value.replaceAll("(?i)javascript\\s*:", "");
        value = value.replaceAll("(?i)vbscript\\s*:", "");
        value = value.replaceAll("(?i)data\\s*:[^,]*javascript", "data:removed");
        value = value.replaceAll("(?i)\\s+on[a-z]{1,20}\\s*=\\s*[\"'][^\"']*[\"']", "");
        value = value.replaceAll("(?i)\\s+on[a-z]{1,20}\\s*=\\s*\\S+", "");
        value = value.replaceAll("(?i)expression\\s*\\(", "");

        return value;
    }
}
