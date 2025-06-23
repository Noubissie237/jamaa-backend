# Service Recharge-Retrait

## Description
Le service recharge-retrait est un microservice Spring Boot qui gère les opérations de recharge et de retrait entre les comptes (service-account) et les cartes (service-card).

## Fonctionnalités

### Recharge
- **Opération** : Décrémenter la balance d'un Account (service-account) et incrémenter currentBalance d'une Card (service-card)
- **Validation** : Vérification du solde suffisant sur le compte
- **Transaction** : Opération atomique avec rollback en cas d'erreur

### Retrait
- **Opération** : Décrémenter currentBalance d'une Card (service-card) et incrémenter la balance d'un Account (service-account)
- **Validation** : Vérification du solde suffisant sur la carte
- **Transaction** : Opération atomique avec rollback en cas d'erreur

## Architecture

### Technologies utilisées
- **Spring Boot 3.4.7**
- **Spring Data JPA** pour la persistance
- **Spring GraphQL** pour l'API
- **Spring AMQP** pour RabbitMQ
- **MySQL** pour la base de données
- **Eureka Client** pour la découverte de services
- **Spring Cloud Config** pour la configuration externalisée

### Structure du projet
```
src/
├── main/java/com/jamaa_bank/service_recharge_retrait/
│   ├── dto/                    # Data Transfer Objects
│   ├── exception/              # Exceptions personnalisées
│   ├── model/                  # Entités JPA
│   ├── repository/             # Repositories JPA
│   ├── service/                # Services métier
│   ├── resolver/               # Resolvers GraphQL
│   ├── utils/                  # Utilitaires pour communication inter-services
│   ├── config/                 # Configuration RabbitMQ
│   └── events/                 # Événements RabbitMQ
└── test/                       # Tests unitaires
```

## API GraphQL

### Mutations
```graphql
# Recharge d'une carte depuis un compte
recharge(accountId: ID!, cardId: ID!, amount: BigDecimal!): RechargeRetrait!

# Recharge avec objet request
rechargeWithRequest(request: RechargeRequest!): RechargeRetrait!

# Retrait d'une carte vers un compte
retrait(cardId: ID!, accountId: ID!, amount: BigDecimal!): RechargeRetrait!

# Retrait avec objet request
retraitWithRequest(request: RetraitRequest!): RechargeRetrait!
```

### Queries
```graphql
# Récupérer toutes les opérations
getAllOperations: [RechargeRetrait!]!

# Récupérer les opérations par compte
getOperationsByAccount(accountId: ID!): [RechargeRetrait!]!

# Récupérer les opérations par carte
getOperationsByCard(cardId: ID!): [RechargeRetrait!]!

# Récupérer les opérations par type
getOperationsByType(operationType: OperationType!): [RechargeRetrait!]!

# Récupérer uniquement les recharges
getRecharges: [RechargeRetrait!]!

# Récupérer uniquement les retraits
getRetraits: [RechargeRetrait!]!
```

## Configuration

### Base de données
- **URL** : `jdbc:mysql://localhost:3306/jamaa_recharge_retrait_db`
- **Port** : 8091
- **Eureka** : http://127.0.0.1:8761/eureka/

### Services externes
- **Service Account** : http://localhost:8080/service-account/graphql
- **Service Card** : http://localhost:8081/service-card/graphql

### RabbitMQ
- **Host** : 127.0.0.1:5672
- **Exchanges** : AccountExchange
- **Routing Keys** :
  - `notification.recharge.done` pour les recharges
  - `notification.retrait.done` pour les retraits

## Modèle de données

### RechargeRetrait
```java
{
    "id": Long,
    "accountId": Long,
    "cardId": Long,
    "amount": BigDecimal,
    "operationType": "RECHARGE" | "RETRAIT",
    "status": "SUCCESS" | "FAILED" | "PENDING",
    "createdAt": LocalDateTime,
    "updatedAt": LocalDateTime
}
```

## Gestion des erreurs

### Exceptions
- **InsufficientBalanceException** : Solde insuffisant
- **RechargeRetraitException** : Erreur générale du service
- **AccountNotFoundException** : Compte ou carte introuvable
- **AccountServiceException** : Erreur de communication avec les services externes

### Validation
- Paramètres non null
- Montant supérieur à zéro
- Solde suffisant avant opération

## Événements

### Publication RabbitMQ
Chaque opération (réussie ou échouée) publie un événement contenant :
- ID du compte et de la carte
- Montant de l'opération
- Type d'opération (RECHARGE/RETRAIT)
- Statut (SUCCESS/FAILED)
- Timestamp

## Tests
Des tests unitaires sont disponibles pour valider :
- Les opérations de recharge et retrait
- La validation des paramètres
- La gestion des erreurs
- Les interactions avec les services externes

## Démarrage
1. Configurer la base de données MySQL
2. Démarrer Eureka Server
3. Démarrer RabbitMQ
4. Configurer le service dans Spring Cloud Config
5. Lancer l'application : `mvn spring-boot:run`

## GraphiQL
Interface GraphQL disponible sur : http://localhost:8091/graphiql
