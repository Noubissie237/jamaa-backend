#!/bin/bash
# export KUBECONFIG=/var/lib/jenkins/.kube/config

# Script de monitoring pour Jamaa Backend
echo "📊 Monitoring Jamaa Backend sur Kubernetes"
echo "=========================================="

# Vérifier que kubectl est configuré
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Erreur: kubectl n'est pas configuré ou le cluster n'est pas accessible"
    exit 1
fi

# Fonction pour afficher une section
print_section() {
    echo ""
    echo "🔍 $1"
    echo "----------------------------------------"
}

# État général du namespace
print_section "État du namespace jamaa"
kubectl get all -n jamaa

# État des déploiements
print_section "Déploiements"
kubectl get deployments -n jamaa -o wide

# État des services
print_section "Services"
kubectl get services -n jamaa

# État des pods
print_section "Pods"
kubectl get pods -n jamaa -o wide

# État des HPA (Auto-scaling)
print_section "Auto-scaling (HPA)"
if kubectl get hpa -n jamaa &> /dev/null; then
    kubectl get hpa -n jamaa
else
    echo "⚠️ HPA non configuré ou Metrics Server non installé"
fi

# État des PVC (Stockage)
print_section "Volumes persistants"
kubectl get pvc -n jamaa

# État de l'Ingress
print_section "Ingress"
kubectl get ingress -n jamaa

# Vérification des services critiques
print_section "Vérification des services critiques"

critical_services=("mysql" "rabbitmq" "eventstore" "service-config" "service-register" "service-proxy")

for service in "${critical_services[@]}"; do
    if kubectl get deployment "$service" -n jamaa &> /dev/null; then
        replicas=$(kubectl get deployment "$service" -n jamaa -o jsonpath='{.status.readyReplicas}')
        desired=$(kubectl get deployment "$service" -n jamaa -o jsonpath='{.spec.replicas}')
        if [ "$replicas" = "$desired" ]; then
            echo "✅ $service: $replicas/$desired replicas prêts"
        else
            echo "⚠️ $service: $replicas/$desired replicas prêts"
        fi
    else
        echo "❌ $service: Déploiement non trouvé"
    fi
done

# Utilisation des ressources
print_section "Utilisation des ressources"
if kubectl top nodes &> /dev/null; then
    echo "Nœuds:"
    kubectl top nodes
    echo ""
    echo "Pods (Top 10):"
    kubectl top pods -n jamaa --sort-by=cpu | head -11
else
    echo "⚠️ Metrics Server non installé - impossible d'afficher l'utilisation des ressources"
fi

# Événements récents
print_section "Événements récents (dernières 10 minutes)"
kubectl get events -n jamaa --sort-by='.lastTimestamp' | tail -10

echo ""
echo "🎉 Monitoring terminé!"
echo ""
echo "💡 Commandes utiles:"
echo "   kubectl logs -f deployment/service-proxy -n jamaa    # Logs du proxy"
echo "   kubectl describe pod <pod-name> -n jamaa            # Détails d'un pod"
echo "   kubectl port-forward service/rabbitmq-service 15672:15672 -n jamaa  # RabbitMQ UI"
echo "   kubectl port-forward service/eventstore-service 2113:2113 -n jamaa  # EventStore UI"
