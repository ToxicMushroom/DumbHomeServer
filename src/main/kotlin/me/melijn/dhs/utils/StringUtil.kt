package me.melijn.dhs.utils

fun String.splitIETEL(delimiter: String): List<String> {
    val res = this.split(delimiter)
    return if (res.first().isEmpty() && res.size == 1) {
        emptyList()
    } else {
        res
    }
}

fun String.remove(vararg strings: String, ignoreCase: Boolean = false): String {
    var newString = this
    for (string in strings) {
        newString = newString.replace(string, "", ignoreCase)
    }
    return newString
}