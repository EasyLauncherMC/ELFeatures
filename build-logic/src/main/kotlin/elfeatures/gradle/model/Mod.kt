package elfeatures.gradle.model

import java.util.*

data class Mod(
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
        properties.getProperty("mod_authors"),
    )

    fun toReplaceProperties() = buildMap {
        put("mod_id", id)
        put("mod_name", name)
        put("mod_version", version)
        put("mod_license", license)
        put("mod_description", description)
        put("mod_full_description", fullDescription)
        put("mod_summary", summary)
        put("mod_website", website)
        put("mod_sources", sources)
        put("mod_issues", issues)
        put("mod_authors", authors)
    }

}