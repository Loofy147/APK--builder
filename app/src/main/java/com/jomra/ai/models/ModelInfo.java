package com.jomra.ai.models;

import java.util.List;
import java.util.Map;

public class ModelInfo {
    public String id;
    public String name;
    public String description;
    public ModelCategory category;
    public String version;
    public long sizeBytes;
    public String downloadUrl;
    public String sha256;
    public String signature;
    public boolean compressed;
    public int priority;
    public boolean featured;
    public float rating;
    public int downloadCount;
    public String author;
    public String license;
    public List<String> capabilities;
    public int minAndroidVersion;
    public int minRamMB;
    public String filename;
    public Map<String, Object> metadata;
}
