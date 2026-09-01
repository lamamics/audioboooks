package com.lamamics.audioboooks

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

data class Chapter(val uri: Uri, val name: String)

data class Book(val id: String, val name: String, val chapters: List<Chapter>)

private val AUDIO_EXTENSIONS = setOf("mp3", "m4a", "m4b", "aac", "ogg", "opus", "flac", "wav")

private fun isAudio(name: String): Boolean =
    name.substringAfterLast('.', "").lowercase() in AUDIO_EXTENSIONS

/** Tri naturel : "chapitre 2" avant "chapitre 10". */
fun naturalCompare(a: String, b: String): Int {
    var i = 0
    var j = 0
    while (i < a.length && j < b.length) {
        val ca = a[i]
        val cb = b[j]
        if (ca.isDigit() && cb.isDigit()) {
            var i2 = i
            while (i2 < a.length && a[i2].isDigit()) i2++
            var j2 = j
            while (j2 < b.length && b[j2].isDigit()) j2++
            val na = a.substring(i, i2).trimStart('0')
            val nb = b.substring(j, j2).trimStart('0')
            val cmp = if (na.length != nb.length) na.length - nb.length else na.compareTo(nb)
            if (cmp != 0) return cmp
            i = i2
            j = j2
        } else {
            if (ca != cb) return ca.compareTo(cb)
            i++
            j++
        }
    }
    return (a.length - i) - (b.length - j)
}

private val naturalOrder = Comparator<String> { a, b -> naturalCompare(a.lowercase(), b.lowercase()) }

private fun chaptersOf(files: List<DocumentFile>): List<Chapter> =
    files.sortedWith(compareBy(naturalOrder) { it.name ?: "" })
        .map { Chapter(it.uri, (it.name ?: "?").substringBeforeLast('.')) }

/** Liste les livres : chaque sous-dossier contenant de l'audio = un livre. */
fun listBooks(ctx: Context, rootUri: Uri): List<Book> {
    val root = DocumentFile.fromTreeUri(ctx, rootUri) ?: return emptyList()
    val children = root.listFiles().toList()
    val books = mutableListOf<Book>()

    for (dir in children.filter { it.isDirectory }) {
        val audio = dir.listFiles().filter { it.isFile && isAudio(it.name ?: "") }
        if (audio.isEmpty()) continue
        books.add(Book(dir.uri.toString(), dir.name ?: "?", chaptersOf(audio)))
    }

    // Des fichiers audio directement à la racine = un livre portant le nom du dossier racine.
    val rootAudio = children.filter { it.isFile && isAudio(it.name ?: "") }
    if (rootAudio.isNotEmpty()) {
        books.add(Book(root.uri.toString(), root.name ?: "Audiobook", chaptersOf(rootAudio)))
    }

    return books.sortedWith(compareBy(naturalOrder) { it.name })
}
