package com.skillgap.analyzer.dto;

import java.util.Map;

public record ApiError(int status, String message, Map<String, String> errors) {}
