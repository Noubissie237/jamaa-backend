#!/bin/bash
# export KUBECONFIG=/var/lib/jenkins/.kube/config

echo "🔄 Mise à jour des images vers Docker Hub..."

SERVICES_DIR="services"

# Fonction pour mettre à jour une image et la puller
update_image() {
    local service=$1
    local file="$SERVICES_DIR/service-$service.yaml"
    local new_image="noubissie237/jamaa-project-service-$service:latest"
    
    if [ -f "$file" ]; then
        echo "🔄 Mise à jour de $service..."
        sed -i "s|image: jamaa/service-$service:latest|image: $new_image|g" "$file"
        echo "✅ Fichier YAML de $service mis à jour ✅"

        echo "📥 Pull de l'image $new_image..."
        docker pull "$new_image"

        echo "✅ Image $new_image pullée avec succès"
    else
        echo "⚠️ Fichier $file non trouvé"
    fi
}

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
    "transactions"
)

for service in "${services[@]}"; do
    update_image "$service"
done

echo ""
echo "✅ Toutes les images ont été mises à jour et pullées avec succès !"
echo "📋 Images utilisées et pullées :"
echo "   - noubissie237/jamaa-project-service-config:latest"
echo "   - noubissie237/jamaa-project-service-register:latest"
echo "   - noubissie237/jamaa-project-service-proxy:latest"
for service in "${services[@]}"; do
    echo "   - noubissie237/jamaa-project-service-$service:latest"
done
