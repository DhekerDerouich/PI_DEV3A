# FarmVision – Plateforme de Gestion Intelligente pour l'Agriculture

## Overview
Ce projet a été développé dans le cadre du **PIDEV – 1ère Année enginner** à l’**École Supérieure Privée d’Ingénierie et de Technologies (Esprit)**  
(Année universitaire 2025–2026).

**FarmVision** est une application **desktop JavaFX** full-stack qui vise à moderniser la gestion agricole en Tunisie. Elle permet aux agriculteurs, responsables d’exploitation et administrateurs de gérer les utilisateurs, les équipements, le stock, les finances, les parcelles/cultures, ainsi que des fonctionnalités avancées comme les prédictions IA, les intégrations API externes (Google Merchant, InvenTree), la reconnaissance vocale (Vosk) et la détection de présence humaine.

## Tech Stack
- **Frontend** : JavaFX (FXML + CSS)
- **Backend** : Java (Spring-like architecture avec services/DAO)
- **Base de données** : MySQL (via JDBC)
- **Intégrations externes** :
  - Google OAuth (connexion sécurisée)
  - Google Merchant Center API
  - InvenTree API (gestion d’inventaire)
  - Vosk (reconnaissance vocale française)
  - Modèle de détection faciale (présence humaine)
- **Outils & Bibliothèques** :
  - Maven (gestion des dépendances)
  - JavaFX (interface graphique)
  - Vosk-API (speech-to-text)
  - Divers services externes (CO2Signal, SunriseSunset, etc.)

## Academic Context
Développé à **Esprit School of Engineering – Tunisia**
PIDEV – 3A | Année universitaire 2025–2026  
Équipe de 5 étudiants – Projet Intégré de Développement

## Fonctionnalités principales
- Gestion complète des utilisateurs (Agriculteur, Responsable d’exploitation, Administrateur)
- Module équipements & maintenance (avec QR Code, alertes, calendrier)
- Gestion financière (dépenses/revenus, prévisions, PDF export)
- Gestion de stock & marketplace agricole
- Gestion des parcelles et cultures
- Dashboard métier avancé avec prédictions IA
- Remarques administrateur internes (avec reconnaissance vocale)
- Connexion sécurisée Google OAuth
- Vérification humaine simple (détection visage / anti-bot)

