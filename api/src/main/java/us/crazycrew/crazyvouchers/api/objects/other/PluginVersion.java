package us.crazycrew.crazyvouchers.api.objects.other;

public class PluginVersion {

    public record VersionInfo(String version, String apiVersion, GitInfo git) {}

    public record GitInfo(String current, String previous) {}

}