package me.melijn.dhs.utils

fun String.splitIETEL(delimiter: String): List<String> {
    val res = this.split(delimiter)
    return if (res.first().isEmpty() && res.size == 1) {
        emptyList()
    } else {
        res
    }
}