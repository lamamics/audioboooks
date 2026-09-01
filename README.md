# Audioboooks — by Lamamics

Application Android simple, sobre et intuitive pour écouter et suivre ses audiobooks mp3,
aux teintes beige clair et rouge bordeaux.

## Le principe

Tu télécharges tes audiobooks (mp3) et tu les ranges dans un dossier de ton téléphone :
**1 dossier = 1 livre** (contenant un ou plusieurs mp3, triés naturellement : `chapitre 2` avant `chapitre 10`).

L'app lit ce dossier et te présente ta bibliothèque.

## Fonctionnalités

- 📂 Choix du dossier racine des audiobooks (la sélection est mémorisée)
- ▶️ Lecture / pause, avec **reprise X secondes en arrière** après une pause
  (5 s par défaut, réglable de 0 à 30 s dans les paramètres)
- 💾 Position mémorisée en permanence : quitte l'app, reviens dans 3 jours,
  tu reprends exactement où tu en étais
- ⏩ Avance de 10 s ou 30 s, recul de 10 s ou 30 s
- 🚀 Vitesses de lecture : 0.8x, 0.9x, 1x, 1.2x, 1.4x, 1.6x, 1.8x, 2x
- 📑 Liste des chapitres avec accès direct à chacun
- ⭐ Favoris (étoile), notation 1 à 5 étoiles, marquage « Terminé »
  (automatique en fin de livre, ou manuel)
- 🔊 Lecture en arrière-plan avec notification (écran éteint, autres apps…)

## Installation sur ton téléphone

1. Va dans l'onglet **[Releases](https://github.com/lamamics/audioboooks/releases)** du repo.
2. Télécharge le fichier `Audioboooks-vX.Y.Z.apk` de la dernière release **depuis ton téléphone**.
3. Ouvre le fichier téléchargé et accepte l'installation
   (autorise « l'installation d'applications inconnues » si Android le demande).
4. Ouvre l'app, choisis ton dossier d'audiobooks, et c'est parti.

Les mises à jour s'installent par-dessus l'ancienne version (même clé de signature).

## Compilation

Chaque push sur `main` déclenche le workflow GitHub Actions **Build APK** qui compile,
signe et publie l'APK dans une nouvelle release.

En local (avec Android Studio ou le SDK Android installé) :

```bash
./gradlew assembleRelease
# APK produite dans app/build/outputs/apk/release/
```

## Technique

- Kotlin + Jetpack Compose (Material 3)
- Media3 / ExoPlayer avec `MediaSessionService` (lecture en arrière-plan)
- Storage Access Framework pour l'accès au dossier (aucune permission de stockage globale)
- minSdk 26 (Android 8.0), targetSdk 35

## Note pour une publication sur le Play Store

La clé de signature `keystore/audioboooks.jks` est committée dans ce repo public pour
simplifier le build automatique : elle convient pour un usage personnel, mais **avant une
publication sur le Play Store, génère une nouvelle clé privée** (garde-la hors du repo)
et active Play App Signing.

## Licence

MIT — voir [LICENSE](LICENSE).
