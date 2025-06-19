# Déploiement Kubernetes - Jamaa Backend

Ce répertoire contient tous les fichiers nécessaires pour déployer l'application Jamaa Backend sur Kubernetes.

## 🏗️ Architecture

### Infrastructure
- **MySQL** : Base de données (une DB par service)
- **RabbitMQ** : Message broker pour la communication asynchrone
- **EventStore DB** : Base de données événementielle pour le service transactions
- **Namespace** : `jamaa` pour isoler les ressources

### Services
- **service-register** (Eureka) : Service de découverte - Port 8761
- **service-config** : Configuration centralisée - Port 2370
- **service-proxy** : Gateway API (EXPOSÉ) - Port 8079
- **11 microservices métier** : Ports 8081-8091

## 📁 Structure des fichiers

```
k8s/
├── infrastructure/          # Infrastructure de base
│   ├── namespace.yaml      # Namespace jamaa
│   ├── mysql-deployment.yaml
│   ├── rabbitmq-deployment.yaml
│   └── eventstore-deployment.yaml  # EventStore DB
├── services/               # Tous les microservices
│   ├── service-register.yaml
│   ├── service-config.yaml
│   ├── service-proxy.yaml  # Gateway avec Ingress
│   └── service-*.yaml      # Autres microservices
├── configs/                # Configurations
│   ├── aws-secrets.yaml    # Secrets AWS S3
│   └── hpa-config.yaml     # Auto-scaling horizontal
├── deploy.sh              # Script de déploiement
├── undeploy.sh            # Script de suppression
├── update-images.sh       # Script de mise à jour des images
└── README.md              # Ce fichier
```

## 🚀 Déploiement

### Prérequis
- Cluster Kubernetes configuré
- `kubectl` installé et configuré
- Ingress Controller (nginx recommandé)
- cert-manager pour HTTPS (optionnel)
- Metrics Server pour HPA (auto-scaling)

### Images Docker
Les images sont automatiquement construites et poussées sur Docker Hub via GitHub Actions :
- `noubissie237/jamaa-project-service-*:latest`

### Déploiement automatique
```bash
cd k8s
./deploy.sh
```

### Déploiement manuel
```bash
# 1. Créer le namespace
kubectl apply -f infrastructure/namespace.yaml

# 2. Déployer l'infrastructure
kubectl apply -f infrastructure/

# 3. Déployer les configurations
kubectl apply -f configs/

# 4. Déployer les services de base
kubectl apply -f services/service-config.yaml
kubectl apply -f services/service-register.yaml

# 5. Attendre que les services de base soient prêts
kubectl wait --for=condition=available --timeout=300s deployment/service-config -n jamaa
kubectl wait --for=condition=available --timeout=300s deployment/service-register -n jamaa

# 6. Déployer les microservices
kubectl apply -f services/

# 7. Vérifier le déploiement
kubectl get all -n jamaa
```

## 🔧 Configuration

### Variables d'environnement importantes
- **SPRING_PROFILES_ACTIVE** : `k8s` pour tous les services
- **Bases de données** : Chaque service a sa propre DB
- **RabbitMQ** : Instance partagée
- **Eureka** : `http://service-register:8761/eureka/`
- **Config Server** : `http://service-config:2370`

### Secrets à configurer
1. **AWS S3** : Mettre à jour `configs/aws-secrets.yaml` avec vos vraies clés
2. **Email** : Mot de passe Gmail dans `service-notifications.yaml`
3. **GitHub** : Token pour le Config Server dans `service-config.yaml`

## 🌐 Exposition

### Ingress
- **Domaine** : `api.jamaa.com` (à modifier dans `service-proxy.yaml`)
- **HTTPS** : Configuré avec cert-manager
- **Point d'entrée unique** : Toutes les requêtes passent par le service-proxy

### Ports des services
| Service | Port | Exposition |
|---------|------|------------|
| service-proxy | 8079 | ✅ Via Ingress |
| service-register | 8761 | ❌ Interne |
| service-config | 2370 | ❌ Interne |
| service-auth | 8081 | ❌ Interne |
| service-users | 8082 | ❌ Interne |
| service-account | 8083 | ❌ Interne |
| service-banks | 8084 | ❌ Interne |
| service-transfert | 8085 | ❌ Interne |
| service-transactions | 8086 | ❌ Interne |
| service-notifications | 8087 | ❌ Interne |
| service-card | 8088 | ❌ Interne |
| service-banks-account | 8090 | ❌ Interne |
| service-recharge-retrait | 8091 | ❌ Interne |

## 📊 Monitoring

### Health Checks
Tous les services ont des probes configurées :
- **Readiness Probe** : `/actuator/health`
- **Liveness Probe** : `/actuator/health`

### Commandes utiles
```bash
# Voir l'état des pods
kubectl get pods -n jamaa

# Voir les logs d'un service
kubectl logs -f deployment/service-proxy -n jamaa

# Voir les services
kubectl get services -n jamaa

# Voir l'ingress
kubectl get ingress -n jamaa

# Accéder à RabbitMQ Management
kubectl port-forward service/rabbitmq-service 15672:15672 -n jamaa
# Puis aller sur http://localhost:15672 (guest/guest)
```

## 🔄 Mise à jour

### Mettre à jour une image
```bash
kubectl set image deployment/service-users service-users=jamaa/service-users:v2.0 -n jamaa
```

### Redémarrer un service
```bash
kubectl rollout restart deployment/service-users -n jamaa
```

## 🗑️ Suppression

### Suppression automatique
```bash
./undeploy.sh
```

### Suppression manuelle
```bash
kubectl delete namespace jamaa
```

## ⚠️ Notes importantes

1. **Domaine** : Changez `api.jamaa.com` par votre domaine dans `service-proxy.yaml`
2. **Secrets** : Mettez à jour tous les secrets avec vos vraies valeurs
3. **Ressources** : Ajustez les limites CPU/mémoire selon votre cluster
4. **Stockage** : Les PVC utilisent le storage class par défaut
5. **Sécurité** : En production, utilisez des secrets Kubernetes pour toutes les données sensibles

## 🆘 Dépannage

### Service ne démarre pas
```bash
kubectl describe pod <pod-name> -n jamaa
kubectl logs <pod-name> -n jamaa
```

### Base de données
```bash
kubectl exec -it deployment/mysql -n jamaa -- mysql -u root -p
```

### RabbitMQ
```bash
kubectl port-forward service/rabbitmq-service 15672:15672 -n jamaa
```

## 📈 Scalabilité

### Auto-scaling horizontal
- **Metrics Server** : Pour collecter les métriques de CPU et de mémoire
- **HPA (Horizontal Pod Autoscaler)** : Pour ajuster le nombre de replicas en fonction des métriques

### Configurer l'auto-scaling
```bash
kubectl apply -f configs/hpa-config.yaml
```

### Voir les métriques
```bash
kubectl get hpa -n jamaa
```

### Voir les replicas
```bash
kubectl get deployments -n jamaa
```
