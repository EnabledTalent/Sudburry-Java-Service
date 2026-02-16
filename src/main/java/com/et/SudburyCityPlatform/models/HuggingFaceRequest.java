package com.et.SudburyCityPlatform.models;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class HuggingFaceRequest {
    public String model;
    public List<Map<String, String>> messages;
    public int max_tokens;
    public double temperature;
}

