#!/bin/bash

# Vérification du paramètre
if [ -z "$1" ]; then
  echo "❌ Veuillez fournir le nom du service à redémarrer."
  echo "👉 Exemple : ./restart-service.sh service-notifications"
  exit 1
fi

SERVICE_NAME=$1
NAMESPACE="jamaa"
DEPLOYMENT_FILE="services/${SERVICE_NAME}.yaml"

# Vérification de l'existence du fichier YAML
if [ ! -f "$DEPLOYMENT_FILE" ]; then
  echo "❌ Le fichier $DEPLOYMENT_FILE n'existe pas."
  exit 1
fi

echo "🔁 Redémarrage du service : $SERVICE_NAME"

# Supprimer le déploiement
echo "🗑️ Suppression du déploiement..."
kubectl delete deployment "$SERVICE_NAME" -n "$NAMESPACE"

# Attendre que le déploiement soit complètement supprimé
echo "⏳ Attente de la suppression du déploiement..."
while kubectl get deployment "$SERVICE_NAME" -n "$NAMESPACE" &> /dev/null; do
  echo "⌛ En attente..."
  sleep 2
done

# Redéployer
echo "🚀 Redéploiement du service..."
kubectl apply -f "$DEPLOYMENT_FILE"

# Attendre qu'il soit prêt
echo "⏳ Attente que le service soit prêt..."
kubectl wait --for=condition=available --timeout=300s deployment/"$SERVICE_NAME" -n "$NAMESPACE"

echo "✅ Service $SERVICE_NAME redémarré avec succès !"
