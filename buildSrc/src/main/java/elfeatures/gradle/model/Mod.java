package elfeatures.gradle.model;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@AllArgsConstructor
public class Mod {

    private final String id;
    private final String name;
    private final String version;
    private final String license;
    private final String description;
    private final String fullDescription;
    private final String summary;
    private final String website;
    private final String sources;
    private final String issues;
    private final String authors;

    public Mod(Properties properties) {
        this(
                String.valueOf(properties.get("mod_id")),
                String.valueOf(properties.get("mod_name")),
                String.valueOf(properties.get("mod_version")),
                String.valueOf(properties.get("mod_license")),
                String.valueOf(properties.get("mod_description")),
                String.valueOf(properties.get("mod_full_description")),
                String.valueOf(properties.get("mod_summary")),
                String.valueOf(properties.get("mod_website")),
                String.valueOf(properties.get("mod_sources")),
                String.valueOf(properties.get("mod_issues")),
                String.valueOf(properties.get("mod_authors"))
        );
    }

    public Map<String, Object> toReplaceProperties() {
        Map<String, Object> replacements = new HashMap<>();
        replacements.put("mod_id", id);
        replacements.put("mod_name", name);
        replacements.put("mod_version", version);
        replacements.put("mod_license", license);
        replacements.put("mod_description", description);
        replacements.put("mod_full_description", fullDescription);
        replacements.put("mod_summary", summary);
        replacements.put("mod_website", website);
        replacements.put("mod_sources", sources);
        replacements.put("mod_issues", issues);
        replacements.put("mod_authors", authors);
        return replacements;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getLicense() {
        return license;
    }

    public String getDescription() {
        return description;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getSummary() {
        return summary;
    }

    public String getWebsite() {
        return website;
    }

    public String getSources() {
        return sources;
    }

    public String getIssues() {
        return issues;
    }

    public String getAuthors() {
        return authors;
    }

}