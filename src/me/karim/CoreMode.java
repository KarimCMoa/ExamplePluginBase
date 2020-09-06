package me.karim;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CoreMode {

    SETUP("Setup"),
    BETA("Beta"),
    DEV("Dev"),
    DISABLED("Disabled"),
    NORMAL("Normal");

    private String name;
}
