# Lab JEE / Hibernate : Gestion Commerciale

Ce projet constitue une implémentation complète et rigoureuse du laboratoire **Hibernate & JEE**. Il couvre l'ensemble des concepts de persistance avancée avec l'API JPA 2.2 et Hibernate 5.6, du mapping objet-relationnel complexe aux requêtes dynamiques et à la gestion transactionnelle.

---

## 🎯 Objectifs d'apprentissage couverts

1. **Configuration d'Hibernate & JPA** : Fichiers `hibernate.cfg.xml` et `persistence.xml`, utilitaires `HibernateUtil` et `GenericDAO`.
2. **Modélisation d'entités complexes** :
   - Relation bidirectionnelle One-to-Many / Many-to-One (`Categorie` ↔ `Produit`) avec synchronisation par helper methods (`addProduit` / `removeProduit`) et `orphanRemoval = true`.
   - Stratégie d'héritage `JOINED` (`User` abstrait → `Client`, `Fournisseur`).
   - Relation Many-to-Many avec entité associative et clé primaire composée (`Commande` ↔ `CommandeProduit` ↔ `Produit`, avec `CommandeProduitPK`, `@EmbeddedId` et `@MapsId`).
3. **GenericDAO & CrudService** : DAO générique typé (`<T, ID extends Serializable>`), gestion de cycle de vie (`persist`, `merge`, `remove`, `find`, `findAll`) et isolation propre des transactions.
4. **Requêtes avancées** :
   - **Named Queries** déclaratives dans `Produit` (`Produit.findAll`, `Produit.findByCategorie`, `Produit.searchByLibelle`).
   - **Criteria API dynamique** et type-safe avec filtres conditionnels (`term`, `minPrix`, `maxPrix`).
   - **Pagination JPQL** avec `setFirstResult` et `setMaxResults`.
5. **Gestion transactionnelle & Contexte de persistance** : Cycle de vie des entités (transient, managed, detached, removed), gestion explicite des rollbacks en cas d'exception métier.

---

## 📁 Architecture du projet

```text
gestion-commerciale/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── App.java                      # Point d'entrée exécutable (démo complète de toutes les étapes)
│   │   │   ├── dao/
│   │   │   │   ├── GenericDAO.java           # Implémentation générique JPA du DAO
│   │   │   │   └── ProduitDAO.java           # DAO spécifique Produit (Named Queries, Criteria API, Pagination)
│   │   │   ├── entities/
│   │   │   │   ├── Categorie.java            # Entité Catégorie (1:N bidirectionnel)
│   │   │   │   ├── Produit.java              # Entité Produit (N:1, Named Queries)
│   │   │   │   ├── User.java                 # Entité abstraite parente (@Inheritance(JOINED))
│   │   │   │   ├── Client.java               # Entité fille Client
│   │   │   │   ├── Fournisseur.java          # Entité fille Fournisseur
│   │   │   │   ├── Commande.java             # Entité Commande (N:N via table associative)
│   │   │   │   ├── CommandeProduit.java      # Entité associative avec clé composite
│   │   │   │   └── CommandeProduitPK.java    # Clé primaire composée (@Embeddable)
│   │   │   ├── service/
│   │   │   │   ├── CrudService.java          # Service métier CRUD générique
│   │   │   │   ├── ProduitService.java       # Service métier spécialisé Produit avec validations
│   │   │   │   ├── CommandeService.java      # Service de gestion des commandes
│   │   │   │   └── UserService.java          # Service de gestion des utilisateurs
│   │   │   └── util/
│   │   │       └── HibernateUtil.java        # Gestionnaire de SessionFactory Hibernate
│   │   ├── resources/
│   │   │   ├── hibernate.cfg.xml             # Configuration Hibernate native (MySQL 8.0 / MariaDB)
│   │   │   └── META-INF/
│   │   │       └── persistence.xml           # Unité de persistance JPA standard ("defaultPU")
│   │   └── webapp/
│   │       ├── index.jsp                     # Page d'accueil web
│   │       └── WEB-INF/
│   │           └── web.xml                   # Descripteur de déploiement JEE
│   └── test/
│       ├── java/org/example/
│       │   ├── HibernateLabTest.java         # Suite de tests JUnit validant l'ensemble du TP
│       │   └── TransactionTest.java          # Test de rollback transactionnel
│       └── resources/
│           └── META-INF/
│               └── persistence.xml           # Persistence unit de test (H2 in-memory, zero-config)
```

---

## ⚙️ Configuration de la Base de Données

Le projet supporte **deux modes** transparents :

### 1. Mode Production / Développement local (MySQL 8.0 / MariaDB)
- **Hôte** : `localhost:3306`
- **Base** : `gestion_commerciale`
- **Identifiants** : `root` / `1234` (modifiable dans `hibernate.cfg.xml` et `src/main/resources/META-INF/persistence.xml`).

Pour créer la base manuellement si nécessaire :
```sql
CREATE DATABASE IF NOT EXISTS gestion_commerciale CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Mode Test unitaire automatisé (H2 Database)
Pour garantir une exécution instantanée et portable sans nécessiter de serveur MySQL actif, `src/test/resources/META-INF/persistence.xml` utilise une base mémoire `jdbc:h2:mem:gestion_commerciale_test;MODE=MySQL`.

---

## 🚀 Exécution & Compilation

### 1. Lancer la suite complète de tests JUnit
```bash
mvn clean test
```

### 2. Lancer la démonstration complète (App.java)
```bash
mvn exec:java
```

### 3. Packager le livrable WAR (pour WildFly, Payara, TomEE)
```bash
mvn package
```
Le fichier WAR sera généré dans `target/gestion-commerciale-1.0-SNAPSHOT.war`.

---

## 🔍 Détails techniques clés

### 1. Relation N:N et Clé composée (`@MapsId` et `@EmbeddedId`)
L'entité `CommandeProduit` possède une clé primaire composite `CommandeProduitPK` contenant `commandeId` et `produitId`. L'annotation `@MapsId` permet d'associer directement les clés étrangères aux clés primaires sans duplication d'identifiants.

### 2. Requêtes dynamiques (Criteria API)
La méthode `ProduitDAO.search(String term, BigDecimal minPrix, BigDecimal maxPrix)` construit dynamiquement la clause `WHERE` en ajoutant uniquement les prédicats non nuls, protégeant ainsi contre les erreurs SQL et les injections.

### 3. Pagination JPQL
`setFirstResult((pageNumber - 1) * pageSize)` et `setMaxResults(pageSize)` permettent de paginer efficacement de volumineux jeux de données côté serveur de base de données.
