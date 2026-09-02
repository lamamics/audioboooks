package com.lamamics.audioboooks.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
private fun LegalSection(title: String, body: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(20.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mentions légales", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            LegalSection(
                "Éditeur de l'application",
                "L'application Audiobooocs est éditée par LAMAMICS, " +
                    "Société par Actions Simplifiée (SAS).\n\n" +
                    "Siège social : 3 rue de la Basse Houssais, " +
                    "44360 Vigneux-de-Bretagne, France\n" +
                    "SIREN : 982 300 717 — RCS Nantes\n" +
                    "N° TVA intracommunautaire : FR65982300717\n\n" +
                    "Directeur de la publication : Mathias Lamamy\n" +
                    "Contact : contact@lamamics.fr\n" +
                    "Site web : https://lamamics.fr"
            )
            LegalSection(
                "Données personnelles",
                "Audiobooocs est une application 100 % locale : elle ne collecte, " +
                    "ne stocke ni ne transmet aucune donnée personnelle. Aucun compte " +
                    "n'est requis, aucune statistique d'usage n'est envoyée.\n\n" +
                    "Les préférences (dossier choisi, positions de lecture, notes, " +
                    "favoris) sont enregistrées uniquement sur votre appareil et " +
                    "supprimées avec l'application. L'application ne s'appuyant sur " +
                    "aucun serveur, aucun hébergeur ne traite vos données."
            )
            LegalSection(
                "Contenus écoutés",
                "Les livres audio lus par Audiobooocs proviennent exclusivement des " +
                    "fichiers présents sur votre appareil. Vous êtes responsable de la " +
                    "licéité des contenus que vous téléchargez. Les sites suggérés dans " +
                    "l'application proposent des œuvres du domaine public lues par des " +
                    "bénévoles ; ces sites sont édités par des tiers indépendants de " +
                    "LAMAMICS."
            )
            LegalSection(
                "Propriété intellectuelle",
                "Le nom Audiobooocs, l'application et son identité visuelle sont la " +
                    "propriété de LAMAMICS. Le code source de l'application est publié " +
                    "sur GitHub (github.com/lamamics/audioboooks) sous licence MIT."
            )
            LegalSection(
                "Droit applicable",
                "Les présentes mentions légales sont régies par le droit français. " +
                    "Tout litige relatif à leur interprétation ou à leur exécution " +
                    "relève de la compétence des tribunaux français."
            )
        }
    }
}
