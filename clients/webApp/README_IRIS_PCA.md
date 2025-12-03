# 🌸 Analyse en Composantes Principales (ACP) - Dataset Iris

## 📋 Description

Ce projet implémente l'**Analyse en Composantes Principales (ACP/PCA)** sur le célèbre jeu de données **Iris** pour réduire la dimensionnalité des données de 4 dimensions à 2 dimensions tout en conservant un maximum d'information.

## 🎯 Objectifs

- Charger le dataset Iris depuis `sklearn.datasets`
- Standardiser les données
- Appliquer l'ACP pour réduire les dimensions
- Visualiser les résultats
- Interpréter les composantes principales

## 📊 Dataset Iris

Le dataset Iris contient 150 échantillons de fleurs d'iris avec :
- **4 caractéristiques** :
  - Longueur du sépale (sepal length)
  - Largeur du sépale (sepal width)
  - Longueur du pétale (petal length)
  - Largeur du pétale (petal width)
- **3 classes** :
  - Iris Setosa
  - Iris Versicolor
  - Iris Virginica

## 🚀 Installation

### 1. Créer un environnement virtuel (recommandé)

```bash
python -m venv venv

# Windows
venv\Scripts\activate

# Linux/Mac
source venv/bin/activate
```

### 2. Installer les dépendances

```bash
pip install -r requirements.txt
```

## ▶️ Exécution

```bash
python iris_pca.py
```

## 📈 Résultats

Le script génère :

### 1. Statistiques descriptives
- Moyenne, écart-type, min, max de chaque caractéristique
- Répartition des classes

### 2. Variance expliquée
- Pourcentage de variance expliquée par chaque composante principale
- Variance cumulative

### 3. Visualisations (iris_pca_analysis.png)
- **Projection ACP** : Données projetées sur les 2 premières composantes
- **Variance expliquée** : Graphique en barres avec variance cumulative
- **Biplot** : Vecteurs des variables originales
- **Matrice de corrélation** : Corrélation entre les composantes

### 4. Composantes principales
- Coefficients de chaque variable dans les composantes
- Interprétation des axes

## 🔍 Interprétation

### PC1 (Première Composante Principale)
- Capture la plus grande variance des données (~73%)
- Représente principalement la taille globale de la fleur
- Forte contribution des longueurs de pétale et sépale

### PC2 (Deuxième Composante Principale)
- Capture la deuxième plus grande variance (~23%)
- Représente la forme de la fleur
- Forte contribution des largeurs

### Variance totale
- Les 2 premières composantes expliquent ~95-97% de la variance totale
- Réduction de 4D à 2D avec perte minimale d'information

## 📚 Bibliothèques utilisées

- **numpy** : Calculs numériques
- **pandas** : Manipulation de données
- **matplotlib** : Visualisations
- **seaborn** : Visualisations statistiques
- **scikit-learn** : 
  - `load_iris()` : Chargement du dataset
  - `StandardScaler` : Standardisation
  - `PCA` : Analyse en Composantes Principales

## 🎓 Concepts clés

### Standardisation
```python
X_scaled = (X - mean) / std
```
Nécessaire car les variables ont des échelles différentes.

### ACP
```python
pca = PCA(n_components=2)
X_pca = pca.fit_transform(X_scaled)
```
Trouve les axes de plus grande variance dans les données.

### Variance expliquée
```python
explained_variance_ratio = pca.explained_variance_ratio_
```
Indique combien d'information est conservée.

## 📊 Structure du code

```
iris_pca.py
├── charger_donnees()          # Charge le dataset Iris
├── afficher_statistiques()    # Affiche les stats descriptives
├── standardiser_donnees()     # Standardise les features
├── appliquer_acp()            # Applique la PCA
├── visualiser_resultats()     # Crée les graphiques
├── afficher_composantes()     # Affiche les coefficients
└── main()                     # Fonction principale
```

## 🎨 Visualisations

Le script génère 4 graphiques :

1. **Scatter plot** : Projection des données sur PC1 et PC2
   - Couleurs différentes pour chaque espèce
   - Permet de voir la séparation des classes

2. **Bar plot** : Variance expliquée par composante
   - Barres pour chaque composante
   - Ligne cumulative

3. **Biplot** : Variables originales projetées
   - Flèches rouges montrant la contribution de chaque variable
   - Permet d'interpréter les composantes

4. **Heatmap** : Corrélation entre composantes
   - Vérifie l'orthogonalité des composantes

## 💡 Utilisation pédagogique

Ce projet est idéal pour :
- Comprendre l'ACP/PCA
- Apprendre la réduction de dimensionnalité
- Visualiser des données multidimensionnelles
- Pratiquer avec scikit-learn
- Créer des visualisations avec matplotlib/seaborn

## 🔧 Personnalisation

### Changer le nombre de composantes
```python
X_pca, pca = appliquer_acp(X_scaled, n_components=3)
```

### Utiliser un autre dataset
```python
from sklearn.datasets import load_wine, load_breast_cancer
# Remplacer load_iris() par load_wine() ou load_breast_cancer()
```

### Modifier les couleurs
```python
colors = {'Setosa': '#FF0000', 'Versicolor': '#00FF00', 'Virginica': '#0000FF'}
```

## 📝 Exemple de sortie

```
🌸 ANALYSE EN COMPOSANTES PRINCIPALES (ACP) - IRIS DATASET 🌸
======================================================================
📊 Chargement du jeu de données Iris...
✅ Données chargées : 150 échantillons, 4 caractéristiques

Caractéristiques : ['sepal length (cm)', 'sepal width (cm)', 
                    'petal length (cm)', 'petal width (cm)']
Classes : ['setosa', 'versicolor', 'virginica']

🔄 Standardisation des données...
✅ Données standardisées

🎯 Application de l'ACP (réduction à 2 composantes)...
✅ ACP appliquée avec succès

📊 Variance expliquée par chaque composante :
   PC1: 72.96%
   PC2: 22.85%

📈 Variance totale expliquée : 95.81%

✅ ANALYSE TERMINÉE AVEC SUCCÈS !
```

## 🎯 Résultats attendus

- **Séparation des classes** : Les 3 espèces d'iris sont bien séparées dans l'espace 2D
- **Setosa** : Bien séparée des autres
- **Versicolor et Virginica** : Légèrement chevauchantes
- **Conservation de l'information** : ~96% de la variance conservée

## 📖 Références

- [Scikit-learn PCA Documentation](https://scikit-learn.org/stable/modules/generated/sklearn.decomposition.PCA.html)
- [Iris Dataset](https://scikit-learn.org/stable/auto_examples/datasets/plot_iris_dataset.html)
- [PCA Tutorial](https://scikit-learn.org/stable/modules/decomposition.html#pca)

## 👨‍💻 Auteur

Projet d'analyse de données - ACP sur Iris Dataset

## 📄 Licence

Ce projet est à des fins éducatives.

---

**Note** : Le dataset Iris est disponible directement dans `sklearn.datasets`, aucun téléchargement externe n'est nécessaire ! 🎉
