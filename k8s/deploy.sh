#!/bin/bash

# Script de déploiement Kubernetes pour Jamaa Backend
echo "🚀 Déploiement de Jamaa Backend sur Kubernetes"

# Vérifier que kubectl est configuré
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Erreur: kubectl n'est pas configuré ou le cluster n'est pas accessible"
    exit 1
fi

# Créer le namespace
echo "📁 Création du namespace..."
kubectl apply -f infrastructure/namespace.yaml

# Attendre que le namespace soit créé
kubectl wait --for=condition=Ready namespace/jamaa --timeout=30s

# Déployer l'infrastructure
echo "🏗️ Déploiement de l'infrastructure..."
kubectl apply -f infrastructure/mysql-deployment.yaml
kubectl apply -f infrastructure/rabbitmq-deployment.yaml
kubectl apply -f infrastructure/eventstore-deployment.yaml

# Attendre que MySQL, RabbitMQ et EventStore soient prêts
echo "⏳ Attente de MySQL, RabbitMQ et EventStore..."
kubectl wait --for=condition=available --timeout=300s deployment/mysql -n jamaa
kubectl wait --for=condition=available --timeout=300s deployment/rabbitmq -n jamaa
kubectl wait --for=condition=available --timeout=300s deployment/eventstore -n jamaa

# Déployer les configurations
echo "⚙️ Déploiement des configurations..."
kubectl apply -f configs/aws-secrets.yaml

# Déployer les services de base (Config Server et Eureka)
echo "🔧 Déploiement des services de base..."
kubectl apply -f services/service-config.yaml
kubectl apply -f services/service-register.yaml

# Attendre que les services de base soient prêts
echo "⏳ Attente des services de base..."
kubectl wait --for=condition=available --timeout=300s deployment/service-config -n jamaa
kubectl wait --for=condition=available --timeout=300s deployment/service-register -n jamaa

# Déployer les microservices
echo "🚀 Déploiement des microservices..."
kubectl apply -f services/service-auth.yaml
kubectl apply -f services/service-users.yaml
kubectl apply -f services/service-account.yaml
kubectl apply -f services/service-banks.yaml
kubectl apply -f services/service-card.yaml
kubectl apply -f services/service-transfert.yaml
kubectl apply -f services/service-transactions.yaml
kubectl apply -f services/service-notifications.yaml
kubectl apply -f services/service-banks-account.yaml
kubectl apply -f services/service-recharge-retrait.yaml

# Déployer les HPA
echo "📈 Déploiement des HPA..."
kubectl apply -f configs/hpa-config.yaml

# Attendre que les microservices soient prêts
echo "⏳ Attente des microservices..."
sleep 30

# Déployer le proxy (Gateway)
echo "🌐 Déploiement du service proxy..."
kubectl apply -f services/service-proxy.yaml

# Attendre que le proxy soit prêt
kubectl wait --for=condition=available --timeout=300s deployment/service-proxy -n jamaa

echo "✅ Déploiement terminé!"
echo ""
echo "📊 État des déploiements:"
kubectl get deployments -n jamaa
echo ""
echo "🔗 Services:"
kubectl get services -n jamaa
echo ""
echo "🌐 Ingress:"
kubectl get ingress -n jamaa
echo ""
echo "🎉 Jamaa Backend est maintenant déployé!"
echo "📝 N'oubliez pas de:"
echo "   1. Configurer votre domaine DNS"
echo "   2. Installer cert-manager pour HTTPS"
echo "   3. Mettre à jour les secrets AWS avec vos vraies clés"
