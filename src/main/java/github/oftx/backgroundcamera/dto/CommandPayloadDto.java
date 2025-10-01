package github.oftx.backgroundcamera.dto;

import java.util.Map;
import java.util.Objects;

public class CommandPayloadDto {
    private String command;
    private Map<String, Object> details;

    // Getters and Setters
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandPayloadDto that = (CommandPayloadDto) o;
        return Objects.equals(command, that.command) && Objects.equals(details, that.details);
    }
    @Override
    public int hashCode() { return Objects.hash(command, details); }
    @Override
    public String toString() { return "CommandPayloadDto{" + "command='" + command + '\'' + ", details=" + details + '}'; }
}