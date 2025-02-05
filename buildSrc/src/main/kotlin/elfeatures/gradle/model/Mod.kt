package elfeatures.gradle.model

import java.util.*

class Mod(
    val id: String,
    val name: String,
    val version: String,
    val license: String,
    val description: String,
    val fullDescription: String,
    val summary: String,
    val website: String,
    val sources: String,
    val issues: String,
    val authors: String,
) {

    constructor(properties: Properties) : this(
        properties.getProperty("mod_id"),
        properties.getProperty("mod_name"),
        properties.getProperty("mod_version"),
        properties.getProperty("mod_license"),
        properties.getProperty("mod_description"),
        properties.getProperty("mod_full_description"),
        properties.getProperty("mod_summary"),
        properties.getProperty("mod_website"),
        properties.getProperty("mod_sources"),
        properties.getProperty("mod_issues"),
        properties.getProperty("mod_authors")
    )

    fun toReplaceProperties(): MutableMap<String?, Any?> {
        val replacements: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        replacements.put("mod_id", id)
        replacements.put("mod_name", name)
        replacements.put("mod_version", version)
        replacements.put("mod_license", license)
        replacements.put("mod_description", description)
        replacements.put("mod_full_description", fullDescription)
        replacements.put("mod_summary", summary)
        replacements.put("mod_website", website)
        replacements.put("mod_sources", sources)
        replacements.put("mod_issues", issues)
        replacements.put("mod_authors", authors)
        return replacements
    }

    override fun toString(): String {
        return "Mod{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", license='" + license + '\'' +
                ", description='" + description + '\'' +
                ", fullDescription='" + fullDescription + '\'' +
                ", summary='" + summary + '\'' +
                ", website='" + website + '\'' +
                ", sources='" + sources + '\'' +
                ", issues='" + issues + '\'' +
                ", authors='" + authors + '\'' +
                '}'
    }

}