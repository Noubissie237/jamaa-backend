#!/bin/bash

# Vérification du paramètre
if [ -z "$1" ]; then
  echo "❌ Veuillez fournir le nom du service à consulter les logs."
  echo "👉 Exemple : ./logs.sh service-notifications"
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

# Vérification des logs
echo "🔍 Vérification des logs..."
kubectl logs -n "$NAMESPACE" -l app="$SERVICE_NAME" --tail=100

echo "✅ Service $SERVICE_NAME loggé avec succès !"
