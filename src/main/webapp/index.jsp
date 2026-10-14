<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Gestion Commerciale - Hibernate & JEE</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f4f6f9;
            color: #333;
            margin: 0;
            padding: 40px;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
            background: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        }
        h1 { color: #1e3a8a; }
        .badge {
            background: #2563eb;
            color: white;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.85em;
        }
        ul { line-height: 1.8; }
    </style>
</head>
<body>
<div class="container">
    <h1>Gestion Commerciale <span class="badge">JEE / Hibernate 5.6</span></h1>
    <p>Application prête pour déploiement sur serveur d'application compatible JEE (WildFly, Payara, TomEE) ou exécution standalone.</p>
    <h3>Fonctionnalités implémentées :</h3>
    <ul>
        <li><strong>GenericDAO & CrudService :</strong> Opérations CRUD complètes avec gestion transactionnelle et gestion propre de l'EntityManager.</li>
        <li><strong>Relations 1:N / N:1 :</strong> Entités Categorie & Produit avec navigation bidirectionnelle et cascade.</li>
        <li><strong>Héritage JOINED :</strong> Hiérarchie User (abstrait), Client et Fournisseur.</li>
        <li><strong>Relation N:N & Clé composée :</strong> Commande & CommandeProduit (avec CommandeProduitPK et @MapsId).</li>
        <li><strong>Requêtes avancées :</strong> Named Queries, Criteria API dynamique, et pagination JPQL.</li>
    </ul>
</div>
</body>
</html>
