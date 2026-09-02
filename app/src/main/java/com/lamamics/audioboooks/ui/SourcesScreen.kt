package com.lamamics.audioboooks.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class AudioSource(
    val name: String,
    val url: String,
    val description: String,
)

private val SOURCES = listOf(
    AudioSource(
        name = "Littérature audio",
        url = "https://www.litteratureaudio.com/",
        description = "La référence francophone : plus de 9 000 livres audio gratuits " +
            "lus par des bénévoles de l'association Des Livres à Lire et à Entendre.",
    ),
    AudioSource(
        name = "Audiocité",
        url = "https://www.audiocite.net/",
        description = "Des milliers d'œuvres en mp3 lues par des bénévoles : " +
            "romans, contes, nouvelles, poésie, histoire…",
    ),
    AudioSource(
        name = "LibriVox",
        url = "https://librivox.org/",
        description = "Livres audio du domaine public lus par des bénévoles du monde entier. " +
            "Plus de 1 000 titres en français : cherche « French » dans le catalogue.",
    ),
    AudioSource(
        name = "Bibliboom",
        url = "https://www.bibliboom.com/",
        description = "Site indépendant animé par des bénévoles : " +
            "300 livres audio gratuits en mp3, à télécharger librement.",
    ),
    AudioSource(
        name = "Atramenta",
        url = "https://www.atramenta.net/audiobooks",
        description = "Œuvres du domaine public à écouter en ligne ou télécharger " +
            "gratuitement, sans inscription.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trouver des livres", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "Ces sites proposent des livres audio gratuits, lus par des bénévoles " +
                        "(œuvres du domaine public). Télécharge les mp3 d'un livre dans un " +
                        "dossier de ton téléphone — 1 dossier = 1 livre — puis retrouve-le " +
                        "dans ta bibliothèque Audiobooocs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            items(SOURCES) { source ->
                Card(
                    onClick = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                            )
                        } catch (e: Exception) {
                            // pas de navigateur disponible : on ignore
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                source.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                source.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Ouvrir dans le navigateur",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
