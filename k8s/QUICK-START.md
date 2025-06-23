# 🚀 Guide de Déploiement Rapide - Jamaa Backend

## ⚡ Déploiement en 5 minutes

### 1. Prérequis
```bash
# Vérifier kubectl
kubectl version --client

# Vérifier la connexion au cluster
kubectl cluster-info
```

### 2. Déploiement automatique
```bash
cd k8s
./deploy.sh
```

### 3. Vérification
```bash
./monitor.sh
```

### 4. Accès à l'application
```bash
# Voir l'IP externe de l'Ingress
kubectl get ingress -n jamaa

# Ou port-forward pour tester localement
kubectl port-forward service/service-proxy 8079:8079 -n jamaa
# Puis aller sur http://service-proxy:8079
```

## 🔧 Configuration rapide

### Domaine personnalisé
1. Éditer `services/service-proxy.yaml`
2. Remplacer `api.jamaa.com` par votre domaine
3. Redéployer : `kubectl apply -f services/service-proxy.yaml`

### Secrets AWS
1. Encoder vos clés en base64 :
   ```bash
   echo -n "YOUR_ACCESS_KEY" | base64
   echo -n "YOUR_SECRET_KEY" | base64
   ```
2. Mettre à jour `configs/aws-secrets.yaml`
3. Appliquer : `kubectl apply -f configs/aws-secrets.yaml`

## 📊 Monitoring en temps réel

### Tableau de bord
```bash
# État général
kubectl get all -n jamaa

# Auto-scaling
kubectl get hpa -n jamaa

# Logs en temps réel
kubectl logs -f deployment/service-proxy -n jamaa
```

### Interfaces Web
```bash
# RabbitMQ Management
kubectl port-forward service/rabbitmq-service 15672:15672 -n jamaa
# http://localhost:15672 (guest/guest)

# EventStore UI
kubectl port-forward service/eventstore-service 2113:2113 -n jamaa
# http://localhost:2113
```

## 🔄 Mise à jour

### Nouvelle version d'un service
```bash
# GitHub Actions pousse automatiquement les nouvelles images
# Redémarrer le déploiement pour utiliser la nouvelle image
kubectl rollout restart deployment/service-users -n jamaa
```

### Mise à jour complète
```bash
# Redémarrer tous les services
kubectl rollout restart deployment -n jamaa
```

## 🆘 Dépannage rapide

### Service ne démarre pas
```bash
kubectl describe pod <pod-name> -n jamaa
kubectl logs <pod-name> -n jamaa
```

### Base de données
```bash
kubectl exec -it deployment/mysql -n jamaa -- mysql -u root -pJamaaAdmin-123
```

### Supprimer et redéployer
```bash
./undeploy.sh
./deploy.sh
```

## 📈 Scalabilité

### Ajuster manuellement
```bash
kubectl scale deployment service-users --replicas=5 -n jamaa
```

### Auto-scaling (automatique)
- CPU > 70% → Scale up
- CPU < 70% → Scale down
- Min: 2 replicas, Max: 10 replicas (service-proxy)

## ✅ Checklist de production

- [ ] Domaine configuré dans l'Ingress
- [ ] Certificat SSL configuré (cert-manager)
- [ ] Secrets AWS mis à jour
- [ ] Mot de passe email configuré
- [ ] Metrics Server installé pour HPA
- [ ] Monitoring configuré
- [ ] Sauvegardes configurées pour MySQL/EventStore

## 🎯 URLs importantes

- **API Gateway** : https://votre-domaine.com
- **RabbitMQ** : Port-forward 15672
- **EventStore** : Port-forward 2113
- **MySQL** : Port-forward 3306
