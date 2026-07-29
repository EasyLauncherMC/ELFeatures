package elfeatures.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.nio.ByteBuffer
import java.util.zip.ZipFile

/**
 * Fails the build when the published JAR references Minecraft members under names the target runtime doesn't have.
 *
 * A mixin JAR left on development mappings still applies cleanly on an obfuscated runtime — the refmap rewrites
 * the `@At` targets — and only dies with a NoSuchMethodError on the first direct call inside a handler body.
 * That's why the shipped JAR itself has to be inspected, not just the presence of a reobfuscation task.
 */
abstract class VerifyMappingsTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val jarFile: RegularFileProperty

    /** how Minecraft members are named at runtime: `official`, `srg` or `intermediary` */
    @get:Input
    abstract val namingScheme: Property<String>

    @TaskAction
    fun verify() {
        val scheme = namingScheme.get()
        if (scheme != OFFICIAL && scheme !in SCHEMES)
            throw GradleException("unknown naming scheme '$scheme', expected $OFFICIAL or one of ${SCHEMES.keys}")

        val jar = jarFile.get().asFile
        val offenders = sortedMapOf<String, MutableSet<String>>()

        ZipFile(jar).use { zip ->
            zip.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { entry ->
                val bytecode = zip.getInputStream(entry).use { it.readBytes() }
                memberReferences(bytecode) { owner, name, descriptor ->
                    if (MINECRAFT_PACKAGES.any(owner::startsWith) && name !in INHERITED && !accepts(scheme, name)) {
                        offenders.getOrPut("$owner.$name$descriptor") { sortedSetOf() } += entry.name
                    }
                }
            }
        }

        if (offenders.isEmpty())
            return

        val report = offenders.entries.joinToString("\n") { (member, classes) ->
            "  $member\n" + classes.joinToString("\n") { "      referenced by $it" }
        }

        throw GradleException(
            "${jar.name} references ${offenders.size} Minecraft member(s) not named as '$scheme' at runtime:\n$report"
        )
    }

    private fun accepts(scheme: String, name: String): Boolean =
        if (scheme == OFFICIAL) {
            SCHEMES.values.none { it.matches(name) }
        } else {
            SCHEMES.getValue(scheme).matches(name)
        }

    /** feeds every field/method reference in the constant pool to [sink] as `(owner, name, descriptor)` */
    private fun memberReferences(bytecode: ByteArray, sink: (String, String, String) -> Unit) {
        val buffer = ByteBuffer.wrap(bytecode)
        buffer.position(8) // magic, minor and major version

        val constants = buffer.word()
        val strings = HashMap<Int, String>()
        val classNames = HashMap<Int, Int>()
        val nameAndTypes = HashMap<Int, Pair<Int, Int>>()
        val references = ArrayList<Pair<Int, Int>>()

        var index = 1
        while (index < constants) {
            when (val tag = buffer.get().toInt() and 0xFF) {
                1 -> strings[index] = String(ByteArray(buffer.word()).also(buffer::get), Charsets.UTF_8)
                7 -> classNames[index] = buffer.word()
                12 -> nameAndTypes[index] = buffer.word() to buffer.word()
                9, 10, 11 -> references += buffer.word() to buffer.word()
                8, 16, 19, 20 -> buffer.skip(2)
                15 -> buffer.skip(3)
                3, 4, 17, 18 -> buffer.skip(4)
                5, 6 -> { buffer.skip(8); index++ } // long and double take two constant pool slots
                else -> throw GradleException("unknown constant pool tag $tag")
            }

            index++
        }

        for ((classIndex, nameAndTypeIndex) in references) {
            val ownerIndex = classNames[classIndex] ?: continue
            val (nameIndex, descriptorIndex) = nameAndTypes[nameAndTypeIndex] ?: continue

            val owner = strings[ownerIndex] ?: continue
            val name = strings[nameIndex] ?: continue
            val descriptor = strings[descriptorIndex] ?: continue

            sink(owner, name, descriptor)
        }
    }

    private fun ByteBuffer.word(): Int = short.toInt() and 0xFFFF

    private fun ByteBuffer.skip(bytes: Int) = position(position() + bytes)

    private companion object {

        const val OFFICIAL = "official"

        val MINECRAFT_PACKAGES = listOf("net/minecraft/", "com/mojang/blaze3d/")

        val SCHEMES = mapOf(
            "srg" to Regex("m_\\d+_|f_\\d+_|func_\\d+_\\w+|field_\\d+_\\w+"),
            "intermediary" to Regex("method_\\d+|field_\\d+|comp_\\d+"),
        )

        // members Minecraft inherits or overrides from non-Minecraft types — those are never renamed
        val INHERITED = setOf(
            "<init>", "<clinit>", "toString", "equals", "hashCode", "close", "run", "call",
            "get", "accept", "apply", "test", "compare", "iterator", "clone",
        )

    }

}
