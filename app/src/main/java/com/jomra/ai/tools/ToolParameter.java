package com.jomra.ai.tools;

public class ToolParameter {
    public final String name;
    public final String description;
    public final Class<?> type;
    public final boolean required;
    public final Object defaultValue;

    public ToolParameter(String name, String desc, Class<?> type,
                        boolean required, Object defaultValue) {
        this.name = name;
        this.description = desc;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
    }
}
