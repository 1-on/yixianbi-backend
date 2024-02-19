package com.yixian.yixianbi.model.enums;

public enum ChartStatusEnum {
    WAIT("wait"),
    RUNNING("running"),
    SUCCEED("succeed"),
    FAILED("failed");

    private final String value;

    ChartStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
