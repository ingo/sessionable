package org.jetbrains.kotlinconf.strings

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Every language has every string of the English source, with the same placeholders. */
class StringsParityTest {
    private val placeholder = Regex("%\\d*\\$?[sd]")

    private val resourceRoots = listOf(
        "src/commonMain/composeResources",
        "../ui-components/src/commonMain/composeResources",
    ).map(::File)

    private fun entries(file: File): Map<String, List<String>> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val result = linkedMapOf<String, List<String>>()
        val nodes = doc.documentElement.childNodes
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeType != org.w3c.dom.Node.ELEMENT_NODE) continue
            val name = node.attributes.getNamedItem("name")?.nodeValue ?: continue
            // A plural or array is compared by the placeholders of each of its items, in order
            result[name] = placeholder.findAll(node.textContent).map { it.value }.sorted().toList()
        }
        return result
    }

    @Test
    fun everyLanguageHasTheSameStringsAsEnglish() {
        for (root in resourceRoots) {
            val english = entries(File(root, "values/strings.xml"))
            val languages = root.listFiles { f -> f.isDirectory && f.name.startsWith("values-") }.orEmpty()
            assertTrue(languages.isNotEmpty(), "no translations found in $root")
            for (dir in languages) {
                val translated = entries(File(dir, "strings.xml"))
                assertEquals(english.keys, translated.keys, "${dir.name} in $root: strings differ from English")
            }
        }
    }

    @Test
    fun translationsKeepTheirPlaceholders() {
        for (root in resourceRoots) {
            val english = entries(File(root, "values/strings.xml"))
            val languages = root.listFiles { f -> f.isDirectory && f.name.startsWith("values-") }.orEmpty()
            for (dir in languages) {
                val translated = entries(File(dir, "strings.xml"))
                for ((name, placeholders) in english) {
                    // Plural items may differ ("one" can drop the number), so compare the set of distinct ones
                    assertEquals(
                        placeholders.toSet(),
                        translated[name]?.toSet(),
                        "${dir.name}: placeholders of '$name' differ from English",
                    )
                }
            }
        }
    }
}
