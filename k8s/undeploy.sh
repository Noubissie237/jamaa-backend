#!/bin/bash

# Script de suppression du déploiement Kubernetes pour Jamaa Backend
echo "🗑️ Suppression du déploiement Jamaa Backend"

# Supprimer les services
echo "🔥 Suppression des services..."
kubectl delete -f services/ --ignore-not-found=true

# Supprimer les configurations
echo "🔥 Suppression des configurations..."
kubectl delete -f configs/ --ignore-not-found=true

# Supprimer l'infrastructure
echo "🔥 Suppression de l'infrastructure..."
kubectl delete -f infrastructure/mysql-deployment.yaml --ignore-not-found=true
kubectl delete -f infrastructure/rabbitmq-deployment.yaml --ignore-not-found=true

# Supprimer le namespace (cela supprimera tout ce qui reste)
echo "🔥 Suppression du namespace..."
kubectl delete namespace jamaa --ignore-not-found=true

echo "✅ Suppression terminée!"
