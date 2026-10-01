package com.darkblade12.itemslotmachine.plugin.command;

public class CommandRegistrationException extends Exception {
    private static final long serialVersionUID = 1L;

    public CommandRegistrationException(String message, String name) {
        super(String.format(message, name));
    }

    public CommandRegistrationException(String message, String name, Throwable cause) {
        super(String.format(message, name), cause);
    }
}
