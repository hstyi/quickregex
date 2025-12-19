#!/usr/bin/env kotlin

import java.io.File

val isNextGeneration = args.isNotEmpty() && args.first() == "--ng"

val dir = File("quickregex")
if (dir.exists()) {
    dir.deleteRecursively()
}
if (dir.mkdirs().not()) {
    throw IllegalStateException("Failed to create directory ${dir.absolutePath}")
}

fun readLicense(): String {
    return File("LICENSE").readText()
}

fun insertLicense(sb: StringBuilder): StringBuilder {
    sb.appendLine("/*").append(readLicense()).append("*/").appendLine()
    return sb
}

fun createHeader() {
    val sb = insertLicense(StringBuilder())
    sb.appendLine("#include \"libregexp.h\"")
    File(dir, "quickregex.h").writeText(sb.toString())
}

fun createSource() {
    val sb = insertLicense(StringBuilder())
    sb.appendLine("#include \"dtoa.c\"")
    sb.appendLine("#include \"cutils.c\"")
    sb.appendLine("#include \"libunicode.c\"")
    sb.appendLine("#include \"libregexp.c\"")

    sb.appendLine().appendLine(
        """
        #ifndef QUICKREGEX_OPAQUE
        #define QUICKREGEX_OPAQUE
        ${if (isNextGeneration) "#include <stdbool.h>" else String()}
        

        ${if (isNextGeneration) "bool" else "int"} lre_check_stack_overflow(void *opaque, size_t alloca_size) {
            return ${if (isNextGeneration) "false" else "0"};
        }

        void *lre_realloc(void *opaque, void *ptr, size_t size) {
            return realloc(ptr, size);
        }

        int lre_check_timeout(void *opaque) {
            return 0;
        }

        #endif /* QUICKREGEX_OPAQUE */
    """.trimIndent()
    )

    File(dir, "quickregex.c").writeText(sb.toString())
}

fun copyFiles() {
    for (filename in setOf(
        "cutils.c", "cutils.h", "dtoa.c", "dtoa.h",
        "libregexp.c", "libregexp-opcode.h", "libunicode.c",
        "libunicode.h", "libunicode-table.h", "libregexp.h"
    )) {
        var text = File("quickjs/${filename}").readText()
        if (isNextGeneration.not()) {
            if ("libunicode.c" == filename) {
                text = text.replace("char_range_s", "char_range_s_rename")
            } else if ("dtoa.c" == filename) {
                text = text.replace("#include <sys/time.h>", "// #include <sys/time.h>")
                    .replace("#include <setjmp.h>", "// #include <setjmp.h>")
            }
        }
        File(dir, filename).writeText(text)

    }
}

copyFiles()
createHeader()
createSource()