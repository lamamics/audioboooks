package com.lamamics.audioboooks.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lamamics.audioboooks.Book
import com.lamamics.audioboooks.Store
import com.lamamics.audioboooks.listBooks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer

private fun normalize(s: String): String =
    Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenBook: (Book) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSources: () -> Unit,
) {
    val context = LocalContext.current
    var rootUri by remember { mutableStateOf(Store.rootUri(context)) }
    var books by remember { mutableStateOf<List<Book>?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }

    val pickFolder = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // permission non persistable : on garde quand même l'uri pour la session
            }
            Store.setRootUri(context, uri.toString())
            rootUri = uri.toString()
            refreshKey++
        }
    }

    LaunchedEffect(rootUri, refreshKey) {
        val r = rootUri
        books = if (r == null) {
            emptyList()
        } else {
            withContext(Dispatchers.IO) { listBooks(context, Uri.parse(r)) }
        }
    }

    Scaffold(
        topBar = {
            if (searchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Rechercher un livre…") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocus),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            searchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Fermer la recherche")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
                LaunchedEffect(Unit) { searchFocus.requestFocus() }
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text("Audiobooocs", fontWeight = FontWeight.Bold)
                            Text(
                                "by Lamamics",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Rechercher dans la bibliothèque")
                        }
                        IconButton(onClick = onOpenSources) {
                            Icon(Icons.Filled.Public, contentDescription = "Trouver de nouveaux livres")
                        }
                        IconButton(onClick = { pickFolder.launch(null) }) {
                            Icon(Icons.Filled.FolderOpen, contentDescription = "Choisir le dossier")
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "Paramètres")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                rootUri == null -> EmptyLibrary(
                    message = "Choisis le dossier de ton téléphone qui contient tes audiobooks.\n\n1 dossier = 1 livre.",
                    buttonLabel = "Choisir le dossier",
                    onClick = { pickFolder.launch(null) },
                )

                books == null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                books!!.isEmpty() -> EmptyLibrary(
                    message = "Aucun audiobook trouvé dans ce dossier.\n\nPlace chaque livre dans son propre sous-dossier (fichiers mp3).",
                    buttonLabel = "Changer de dossier",
                    onClick = { pickFolder.launch(null) },
                )

                else -> {
                    val sorted = books!!.sortedByDescending { book ->
                        Store.getBookState(context, book.id).favorite
                    }
                    val query = normalize(searchQuery.trim())
                    val filtered = if (query.isEmpty()) sorted
                    else sorted.filter { normalize(it.name).contains(query) }

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Aucun livre ne correspond à « ${searchQuery.trim()} »",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(filtered, key = { it.id }) { book ->
                                BookRow(book = book, onClick = { onOpenBook(book) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibrary(message: String, buttonLabel: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onClick) {
            Text(buttonLabel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookRow(book: Book, onClick: () -> Unit) {
    val context = LocalContext.current
    var state by remember(book.id) { mutableStateOf(Store.getBookState(context, book.id)) }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    book.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.finished) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Terminé",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Text(
                            "Chapitre ${state.chapterIndex + 1}/${book.chapters.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.rating > 0) {
                        Spacer(Modifier.width(10.dp))
                        Row {
                            repeat(state.rating) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
            IconButton(onClick = {
                val newState = state.copy(favorite = !state.favorite)
                state = newState
                Store.saveBookState(context, book.id, newState)
            }) {
                Icon(
                    if (state.favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favori",
                    tint = if (state.favorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
