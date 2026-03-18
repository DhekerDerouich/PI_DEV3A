<div align="center">

<img src="https://img.shields.io/badge/ESPRIT-PIDEV_1A-2E7D32?style=for-the-badge&logo=leaf&logoColor=white" />

# 🌿 FarmVision
### Plateforme de Gestion Intelligente pour l'Agriculture

> **Projet Intégré de Développement (PIDEV)** · Équipe de **5 étudiants** · ESPRIT, Tunisie 2025–2026

[![JavaFX](https://img.shields.io/badge/Frontend-JavaFX-007396?style=flat-square&logo=java&logoColor=white)](https://openjfx.io/)
[![Java](https://img.shields.io/badge/Backend-Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.java.com/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Google OAuth](https://img.shields.io/badge/Auth-Google_OAuth-4285F4?style=flat-square&logo=google&logoColor=white)](https://developers.google.com/identity)
[![AI](https://img.shields.io/badge/AI-Prédictions_IA-FF6F00?style=flat-square&logo=tensorflow&logoColor=white)]()
[![Status](https://img.shields.io/badge/Status-Terminé-28a745?style=flat-square)]()

</div>

---

## 📋 Table des matières

- [À propos du projet](#-à-propos-du-projet)
- [Contexte académique](#-contexte-académique)
- [Fonctionnalités principales](#-fonctionnalités-principales)
- [Technologies utilisées](#-technologies-utilisées)
- [Intégrations externes](#-intégrations-externes)
- [Structure du projet](#-structure-du-projet)
- [Installation](#-installation)
- [Équipe](#-équipe)

---

## 🌱 À propos du projet

**FarmVision** est une application desktop **JavaFX full-stack** qui vise à moderniser la gestion agricole en Tunisie. Elle permet aux agriculteurs, responsables d'exploitation et administrateurs de gérer l'ensemble de leurs opérations via une interface intelligente et centralisée.

Elle intègre des fonctionnalités avancées telles que les **prédictions IA**, les **intégrations API externes**, la **reconnaissance vocale (Vosk)** et la **détection de présence humaine**.

---

## 🎓 Contexte académique

| | |
|---|---|
| 🏫 **École** | ESPRIT – École Supérieure Privée d'Ingénierie et de Technologies |
| 📚 **Module** | PIDEV – Projet Intégré de Développement |
| 📅 **Année universitaire** | 2025–2026 |
| 👥 **Équipe** | 5 étudiants |
| 🎯 **Niveau** | 1ère Année Ingénieur |
| 📍 **Lieu** | Tunis, Tunisie |

---

## ✨ Fonctionnalités principales

### 👥 Gestion des Utilisateurs
- Rôles : **Agriculteur**, **Responsable d'exploitation**, **Administrateur**
- Connexion sécurisée via **Google OAuth**
- Vérification anti-bot par détection de visage

### 🚜 Module Équipements & Maintenance
- CRUD complet des équipements
- Génération de **QR Codes**
- Alertes de maintenance & calendrier

### 💰 Gestion Financière
- Suivi des dépenses & revenus
- Prévisions financières intelligentes
- Export **PDF** des rapports

### 📦 Gestion de Stock & Marketplace
- Suivi en temps réel du stock agricole
- Intégration **InvenTree API**
- Marketplace agricole connectée à **Google Merchant Center**

### 🌾 Gestion des Parcelles & Cultures
- Cartographie des parcelles
- Suivi des cycles de cultures

### 🤖 Dashboard IA & Avancé
- **Prédictions IA** sur les cultures et rendements
- Remarques administrateur avec **reconnaissance vocale (Vosk)**
- Données environnementales (CO2Signal, SunriseSunset)

---

## 🛠️ Technologies utilisées

| Couche | Technologie |
|--------|-------------|
| **Frontend** | JavaFX (FXML + CSS) |
| **Backend** | Java (architecture services/DAO) |
| **Base de données** | MySQL (via JDBC) |
| **Build** | Maven |
| **Reconnaissance vocale** | Vosk (speech-to-text français) |
| **Détection faciale** | Modèle de détection de présence humaine |
| **Versioning** | Git · GitHub |

---

## 🔗 Intégrations externes

| Service | Usage |
|--------|--------|
| 🔐 **Google OAuth** | Authentification sécurisée |
| 🛒 **Google Merchant Center API** | Marketplace agricole |
| 📦 **InvenTree API** | Gestion d'inventaire avancée |
| 🎙️ **Vosk API** | Reconnaissance vocale française |
| 🌿 **CO2Signal API** | Données environnementales CO2 |
| ☀️ **SunriseSunset API** | Données solaires pour planification |

---

## 📁 Structure du projet

```
FarmVision/
│
├── 📂 src/main/java/
│   ├── 📂 controllers/        # Contrôleurs JavaFX
│   ├── 📂 services/           # Logique métier
│   ├── 📂 dao/                # Accès base de données (JDBC)
│   ├── 📂 models/             # Entités / POJOs
│   └── 📂 utils/              # Outils (QR Code, PDF, OAuth...)
│
├── 📂 src/main/resources/
│   ├── 📂 fxml/               # Interfaces JavaFX (FXML)
│   ├── 📂 css/                # Styles des vues
│   └── 📂 assets/             # Images, icônes
│
│
├── 📄 pom.xml                 # Configuration Maven
└── 📄 README.md
```

---

## 🚀 Installation

### Prérequis
- Java JDK >= 17
- JavaFX SDK >= 17
- Maven >= 3.8
- MySQL Server
- IDE : IntelliJ IDEA ou Eclipse

### Étapes

```bash
# 1. Cloner le dépôt
git clone https://github.com/votre-username/FarmVision.git
cd FarmVision

# 2. Configurer la base de données
# Importer le fichier SQL fourni dans phpMyAdmin ou MySQL Workbench
# Modifier les credentials dans src/main/java/utils/DBConnection.java

# 3. Installer les dépendances Maven
mvn clean install

# 4. Lancer l'application
mvn javafx:run
```

---

## 👥 Équipe

<div align="center">

Développé par une équipe de **5 étudiants** dans le cadre du **PIDEV**

_Projet Intégré de Développement · ESPRIT School of Engineering_

---

<img src="https://img.shields.io/badge/Made%20with-❤️%20in%20Tunisia-red?style=flat-square" />
<img src="https://img.shields.io/badge/ESPRIT-PIDEV_2025--2026-2E7D32?style=flat-square" />

</div>

---

> 💡 *FarmVision a été développé dans le cadre du Projet Intégré de Développement (PIDEV) à ESPRIT, Tunisie — 2025–2026.*
