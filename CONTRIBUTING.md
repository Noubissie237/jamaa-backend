# Guide de Contribution – Jamaa Backend

Merci de votre intérêt pour contribuer à Jamaa Backend ! Suivez ces étapes et bonnes pratiques pour garantir la qualité et la cohérence du projet.

## 🚀 Comment contribuer ?

1. **Forkez** ce dépôt et clonez-le sur votre machine.
2. **Créez une branche** dédiée à votre fonctionnalité ou correction :
   ```bash
   git checkout -b feat/ma-fonctionnalite
   # ou
   git checkout -b fix/mon-bug
   ```
3. **Développez** votre fonctionnalité/correctif en respectant les conventions de code.
4. **Testez** localement (unitaires, intégration, etc.).
5. **Documentez** vos changements (README, commentaires, etc.).
6. **Effectuez un commit** clair et descriptif :
   ```bash
   git commit -m "feat: ajoute la gestion des notifications de transfert"
   ```
7. **Poussez** votre branche sur votre fork :
   ```bash
   git push origin feat/ma-fonctionnalite
   ```
8. **Créez une Pull Request** sur le dépôt principal en détaillant :
   - Le problème résolu ou la fonctionnalité ajoutée
   - Les impacts potentiels
   - Les étapes de test

## 🧑‍💻 Bonnes pratiques
- Respectez le style de code de chaque service (Java, Python, JS...)
- Privilégiez les petites Pull Requests atomiques
- Ajoutez des tests pour toute nouvelle fonctionnalité
- Vérifiez la non-régression des tests existants
- Utilisez des messages de commit explicites (convention [Conventional Commits](https://www.conventionalcommits.org/fr/v1.0.0/))
- Documentez toute API ou modification majeure

## 📦 Dépendances & scripts
- Utilisez les scripts fournis (`deploy.sh`, `update-images.sh`, etc.) pour tester vos changements dans un environnement réaliste
- Consultez le `README.md` et `QUICK-START.md` pour le setup local

## 🤝 Code de conduite
- Soyez respectueux et bienveillant dans les échanges
- Privilégiez la discussion sur les issues avant les grosses modifications

## 📧 Besoin d’aide ?
Ouvrez une issue ou contactez l’équipe via GitHub !