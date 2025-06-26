<p align="center">
  <img src="https://upload.wikimedia.org/wikipedia/fr/2/2a/Blason_univ_Yaound%C3%A9_1.png" width="150" style="margin-right: 40px;">
  <img src="service-notifications/src/main/resources/img/img.jpg" width="190" alt="Logo Jamaa" style="margin-left: 40px; border-radius: 10px; box-shadow: 0 2px 8px #aaa;">
</p>

<p align="center">
  <a href="https://github.com/Noubissie237/jamaa-backend/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/Noubissie237/jamaa-backend/build.yml?branch=main&label=Build&logo=github" alt="Build Status"></a>
  <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT"></a>
  <a href="https://github.com/Noubissie237/jamaa-backend"><img src="https://img.shields.io/github/repo-size/Noubissie237/jamaa-backend?label=Repo%20size&color=informational" alt="Repo Size"></a>
  <a href="https://github.com/Noubissie237/jamaa-backend/graphs/contributors"><img src="https://img.shields.io/github/contributors/Noubissie237/jamaa-backend?color=success" alt="Contributors"></a>
  <a href="https://github.com/Noubissie237/jamaa-backend/issues"><img src="https://img.shields.io/github/issues/Noubissie237/jamaa-backend?color=yellow" alt="Issues"></a>
</p>

# PROJET : Mise sur pieds d'une Système Multi-Banque

## Application de Gestion d'un Système Multi-Banque en utilisant les langages de programmation Java, Python, javaScript, et les framework Spring boot, Django Rest et React js



## Equipes

* Superviseur: Dr KIMBI XAVERIA
* Chef de groupe: NOUBISSIE KAMGA WILFRIED  [lien vers son portfolio](https://noubissie.propentatech.com/)
 
* Développeurs
  * NDEUNA NGANA OUSMAN SINCLAIR: [lien vers son github](https://github.com/Nnos5)
  * TCHAMI SORELLE: [lien vers son github](https://github.com/Tchamisorelle)
  * NANA ORNELLA : [lien vers son github](https://github.com/ornelnana4)
  * DIGOU SENOU PRISNEL NICHA : [lien vers son github](https://github.com/Nich18)
  * NOUBISSIE KAMGA WILFRIED: [lien vers son github](https://github.com/Noubissie237/) 

## PRÉSENTATION DE L'APPLICATION

**Jamaa Backend** est une plateforme complète de gestion multi-bancaire, conçue autour d'une architecture microservices moderne et scalable. Le projet vise à offrir un écosystème sécurisé et performant pour la gestion des comptes, des cartes, des transferts (inter-comptes et inter-banques), des recharges/retraits, et des notifications, adapté aux besoins des institutions financières et fintechs.

### 🏗️ **Architecture et Technologies**
- **Microservices** : 13 services indépendants (Java Spring Boot)
- **Front-end** : React.js (gestion et visualisation)
- **Communication** : RabbitMQ (asynchrone), GraphQL (requêtes inter-services)
- **Base de données** : MySQL (relationnel), EventStoreDB (événements)
- **Déploiement** : Kubernetes (scalabilité automatique, scripts automatisés)
- **Monitoring** : Scripts de surveillance, métriques, alertes

### ⚙️ **Fonctionnalités principales**
- **Gestion des comptes** : Création, consultation, gestion multi-utilisateurs
- **Gestion des cartes** : Attribution, gestion de soldes, opérations bancaires
- **Transferts** :
   - Entre comptes (app-to-app)
   - Entre banques (bank-to-bank) avec vérifications, sécurité et rollback transactionnel
- **Recharges & Retraits** : Mouvement d'argent entre comptes et cartes, avec traçabilité complète
- **Notifications** : Système intelligent de notifications (création de compte, transferts, etc.)
- **Sécurité** : Isolation des services, validation forte, gestion des erreurs

### 🚀 **Points forts**
- **Scalabilité avancée** : Autoscaling Kubernetes (CPU/Mémoire), HPA, gestion fine des ressources
- **Automatisation** : Scripts `deploy.sh`, `undeploy.sh`, `update-images.sh`, `monitor.sh` pour faciliter le cycle de vie
- **Interopérabilité** : Communication fluide entre services via RabbitMQ et GraphQL
- **Prêt pour la production** : Monitoring, haute disponibilité, sécurité, documentation rapide (`QUICK-START.md`)

### 📦 **Démarrage rapide**
Consultez le fichier `QUICK-START.md` à la racine pour un guide pas-à-pas du déploiement sur Kubernetes ou VPS.

---
Pour toute contribution, suggestion ou bug, veuillez contacter l'équipe via GitHub ou consulter les portfolios et profils listés ci-dessus.


## 🗺️ Schéma d’architecture

L’architecture Jamaa repose sur des microservices spécialisés, orchestrés par Kubernetes :

```
[Client Web/React/flutter]  <--->  [Service-Proxy/Ingress]  <--->  [Services Métiers]
                                              |---> Service-Account
                                              |---> Service-Card
                                              |---> Service-Transfert
                                              |---> Service-Recharge-Retrait
                                              |---> Service-Notification
                                              ...

[Services Métiers] <--> [RabbitMQ] <--> [EventStoreDB, MySQL]
```
- **Ingress/Proxy** : point d’entrée unique sécurisé (HTTPS)
- **RabbitMQ** : bus d’événements pour la communication asynchrone
- **EventStoreDB** : stockage des événements métier
- **MySQL** : stockage transactionnel relationnel


## 🗂️ Structure du projet

```
jamaa-backend/
├── account-service/
├── card-service/
├── transfert-service/
├── recharge-retrait-service/
├── notification-service/
├── ...
├── k8s/                # Fichiers de déploiement Kubernetes
├── README.md
```


## 🔌 Exemples d’API

### Exemple de requête GraphQL (création de compte)
```graphql
mutation {
  createAccount(input: {name: "Noubissie Wilfried", email: "wilfried.noubissie@facsciences-uy1.cm"}) {
    id
    name
    balance
  }
}
```
### Exemple REST (notification de transfert)
```http
POST /notification/transfer
Content-Type: application/json
{
  "senderUserId": 1,
  "receiverUserId": 2,
  "amount": 5000,
  "type": "APP_ACCOUNTS"
}
```


## 🛠️ Guide de développement local

1. **Prérequis** :
   - Docker & Docker Compose
   - Java 17+, Python 3.10+, Node.js 18+
   - Kubernetes (minikube ou cluster)

2. **Installation** :
   ```bash
   # Cloner le projet
   git clone https://github.com/Noubissie237/jamaa-backend.git
   cd jamaa-backend
   ```

3. **Lancement local (exemple pour un service)** :
   ```bash
   cd account-service
   ./mvnw spring-boot:run
   ```

4. **Déploiement complet Kubernetes** :
   ```bash
   ./k8s/deploy.sh
   # Pour supprimer :
   ./k8s/undeploy.sh
   ```


## 🤝 Contribution

Les contributions sont les bienvenues ! Merci de :
- Créer une branche dédiée
- Documenter vos changements
- Vérifier les tests
- Proposer une Pull Request détaillée

Voir le fichier `CONTRIBUTING.md` s’il existe, ou contacter l’équipe.


## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus d’informations.
