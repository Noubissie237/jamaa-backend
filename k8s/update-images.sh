#!/bin/bash

# Script pour mettre à jour toutes les images vers Docker Hub
echo "🔄 Mise à jour des images vers Docker Hub..."

# Répertoire des services
SERVICES_DIR="services"

# Fonction pour mettre à jour une image
update_image() {
    local service=$1
    local file="$SERVICES_DIR/service-$service.yaml"
    
    if [ -f "$file" ]; then
        echo "🔄 Mise à jour de $service..."
        sed -i "s|image: jamaa/service-$service:latest|image: noubissie237/jamaa-project-service-$service:latest|g" "$file"
        echo "✅ $service mis à jour"
    else
        echo "⚠️ Fichier $file non trouvé"
    fi
}

# Liste des services à mettre à jour
services=(
    "auth"
    "users"
    "account"
    "banks"
    "transfert"
    "notifications"
    "card"
    "banks-account"
    "recharge-retrait"
)

# Mettre à jour chaque service
for service in "${services[@]}"; do
    update_image "$service"
done

echo ""
echo "✅ Toutes les images ont été mises à jour vers Docker Hub!"
echo "📋 Images utilisées:"
echo "   - noubissie237/jamaa-project-service-config:latest"
echo "   - noubissie237/jamaa-project-service-register:latest"
echo "   - noubissie237/jamaa-project-service-proxy:latest"
for service in "${services[@]}"; do
    echo "   - noubissie237/jamaa-project-service-$service:latest"
done
