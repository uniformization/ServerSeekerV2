package xyz.funtimes909.serverseekerv2.builders;
import java.util.List;

public record Masscan(String ip,  List<Port> ports) {}