"""
Analyse en Composantes Principales (ACP/PCA) sur le jeu de données Iris
========================================================================

Ce script implémente l'ACP sur le célèbre dataset Iris pour réduire
la dimensionnalité des données de 4 dimensions à 2 dimensions.
"""

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.datasets import load_iris
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA

# Configuration du style des graphiques
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (12, 8)

def charger_donnees():
    """Charge le jeu de données Iris"""
    print("📊 Chargement du jeu de données Iris...")
    iris = load_iris()
    
    # Créer un DataFrame pour faciliter la manipulation
    df = pd.DataFrame(
        data=iris.data,
        columns=iris.feature_names
    )
    df['species'] = iris.target
    df['species_name'] = df['species'].map({
        0: 'Setosa',
        1: 'Versicolor',
        2: 'Virginica'
    })
    
    print(f"✅ Données chargées : {df.shape[0]} échantillons, {df.shape[1]-2} caractéristiques")
    print(f"\nCaractéristiques : {iris.feature_names}")
    print(f"Classes : {list(iris.target_names)}")
    
    return df, iris

def afficher_statistiques(df):
    """Affiche les statistiques descriptives"""
    print("\n" + "="*70)
    print("📈 STATISTIQUES DESCRIPTIVES")
    print("="*70)
    print(df.describe())
    
    print("\n📊 Répartition des classes :")
    print(df['species_name'].value_counts())

def standardiser_donnees(X):
    """Standardise les données (moyenne=0, écart-type=1)"""
    print("\n🔄 Standardisation des données...")
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    print("✅ Données standardisées")
    return X_scaled, scaler

def appliquer_acp(X_scaled, n_components=2):
    """Applique l'ACP sur les données standardisées"""
    print(f"\n🎯 Application de l'ACP (réduction à {n_components} composantes)...")
    
    pca = PCA(n_components=n_components)
    X_pca = pca.fit_transform(X_scaled)
    
    print("✅ ACP appliquée avec succès")
    print(f"\n📊 Variance expliquée par chaque composante :")
    for i, var in enumerate(pca.explained_variance_ratio_, 1):
        print(f"   PC{i}: {var*100:.2f}%")
    
    print(f"\n📈 Variance totale expliquée : {sum(pca.explained_variance_ratio_)*100:.2f}%")
    
    return X_pca, pca

def visualiser_resultats(X_pca, df, pca):
    """Crée des visualisations des résultats de l'ACP"""
    print("\n📊 Création des visualisations...")
    
    # Créer une figure avec plusieurs sous-graphiques
    fig = plt.figure(figsize=(16, 12))
    
    # 1. Projection sur les 2 premières composantes principales
    ax1 = plt.subplot(2, 2, 1)
    colors = {'Setosa': '#FF6B6B', 'Versicolor': '#4ECDC4', 'Virginica': '#45B7D1'}
    
    for species in df['species_name'].unique():
        mask = df['species_name'] == species
        ax1.scatter(
            X_pca[mask, 0],
            X_pca[mask, 1],
            c=colors[species],
            label=species,
            alpha=0.7,
            s=100,
            edgecolors='black',
            linewidth=0.5
        )
    
    ax1.set_xlabel(f'PC1 ({pca.explained_variance_ratio_[0]*100:.1f}%)', fontsize=12, fontweight='bold')
    ax1.set_ylabel(f'PC2 ({pca.explained_variance_ratio_[1]*100:.1f}%)', fontsize=12, fontweight='bold')
    ax1.set_title('Projection ACP - Iris Dataset', fontsize=14, fontweight='bold')
    ax1.legend(title='Espèces', fontsize=10)
    ax1.grid(True, alpha=0.3)
    
    # 2. Variance expliquée
    ax2 = plt.subplot(2, 2, 2)
    variance_ratio = pca.explained_variance_ratio_ * 100
    cumulative_variance = np.cumsum(variance_ratio)
    
    x_pos = np.arange(len(variance_ratio))
    bars = ax2.bar(x_pos, variance_ratio, alpha=0.7, color='#4ECDC4', edgecolor='black')
    ax2.plot(x_pos, cumulative_variance, 'ro-', linewidth=2, markersize=8, label='Cumulative')
    
    # Ajouter les valeurs sur les barres
    for i, (bar, val) in enumerate(zip(bars, variance_ratio)):
        ax2.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 1,
                f'{val:.1f}%', ha='center', va='bottom', fontweight='bold')
    
    ax2.set_xlabel('Composantes Principales', fontsize=12, fontweight='bold')
    ax2.set_ylabel('Variance Expliquée (%)', fontsize=12, fontweight='bold')
    ax2.set_title('Variance Expliquée par Composante', fontsize=14, fontweight='bold')
    ax2.set_xticks(x_pos)
    ax2.set_xticklabels([f'PC{i+1}' for i in x_pos])
    ax2.legend()
    ax2.grid(True, alpha=0.3, axis='y')
    
    # 3. Biplot (vecteurs des variables originales)
    ax3 = plt.subplot(2, 2, 3)
    
    # Projeter les données
    for species in df['species_name'].unique():
        mask = df['species_name'] == species
        ax3.scatter(
            X_pca[mask, 0],
            X_pca[mask, 1],
            c=colors[species],
            label=species,
            alpha=0.5,
            s=50
        )
    
    # Ajouter les vecteurs des variables
    feature_names = ['Sepal Length', 'Sepal Width', 'Petal Length', 'Petal Width']
    for i, feature in enumerate(feature_names):
        ax3.arrow(0, 0, pca.components_[0, i]*3, pca.components_[1, i]*3,
                 head_width=0.1, head_length=0.1, fc='red', ec='red', linewidth=2)
        ax3.text(pca.components_[0, i]*3.3, pca.components_[1, i]*3.3,
                feature, fontsize=10, fontweight='bold', color='red')
    
    ax3.set_xlabel(f'PC1 ({pca.explained_variance_ratio_[0]*100:.1f}%)', fontsize=12, fontweight='bold')
    ax3.set_ylabel(f'PC2 ({pca.explained_variance_ratio_[1]*100:.1f}%)', fontsize=12, fontweight='bold')
    ax3.set_title('Biplot - Variables Originales', fontsize=14, fontweight='bold')
    ax3.legend(title='Espèces', fontsize=9)
    ax3.grid(True, alpha=0.3)
    ax3.axhline(y=0, color='k', linestyle='--', alpha=0.3)
    ax3.axvline(x=0, color='k', linestyle='--', alpha=0.3)
    
    # 4. Matrice de corrélation des composantes
    ax4 = plt.subplot(2, 2, 4)
    
    # Créer un DataFrame avec les composantes principales
    pca_df = pd.DataFrame(
        data=X_pca,
        columns=[f'PC{i+1}' for i in range(X_pca.shape[1])]
    )
    
    # Matrice de corrélation
    corr_matrix = pca_df.corr()
    sns.heatmap(corr_matrix, annot=True, fmt='.3f', cmap='coolwarm',
                center=0, square=True, linewidths=1, cbar_kws={"shrink": 0.8},
                ax=ax4)
    ax4.set_title('Matrice de Corrélation des PC', fontsize=14, fontweight='bold')
    
    plt.tight_layout()
    plt.savefig('iris_pca_analysis.png', dpi=300, bbox_inches='tight')
    print("✅ Graphiques sauvegardés : iris_pca_analysis.png")
    plt.show()

def afficher_composantes(pca, feature_names):
    """Affiche les composantes principales"""
    print("\n" + "="*70)
    print("🔍 COMPOSANTES PRINCIPALES")
    print("="*70)
    
    components_df = pd.DataFrame(
        pca.components_,
        columns=feature_names,
        index=[f'PC{i+1}' for i in range(pca.n_components_)]
    )
    
    print("\n📊 Coefficients des composantes principales :")
    print(components_df.round(3))
    
    print("\n💡 Interprétation :")
    print("   - PC1 : Composante principale 1 (axe de plus grande variance)")
    print("   - PC2 : Composante principale 2 (axe de deuxième plus grande variance)")
    print("   - Les valeurs indiquent la contribution de chaque variable originale")

def main():
    """Fonction principale"""
    print("\n" + "="*70)
    print("🌸 ANALYSE EN COMPOSANTES PRINCIPALES (ACP) - IRIS DATASET 🌸")
    print("="*70)
    
    # 1. Charger les données
    df, iris = charger_donnees()
    
    # 2. Afficher les statistiques
    afficher_statistiques(df)
    
    # 3. Préparer les données (features uniquement)
    X = df[iris.feature_names].values
    
    # 4. Standardiser les données
    X_scaled, scaler = standardiser_donnees(X)
    
    # 5. Appliquer l'ACP
    X_pca, pca = appliquer_acp(X_scaled, n_components=2)
    
    # 6. Afficher les composantes
    afficher_composantes(pca, iris.feature_names)
    
    # 7. Visualiser les résultats
    visualiser_resultats(X_pca, df, pca)
    
    print("\n" + "="*70)
    print("✅ ANALYSE TERMINÉE AVEC SUCCÈS !")
    print("="*70)
    print("\n📁 Fichiers générés :")
    print("   - iris_pca_analysis.png : Visualisations complètes")
    print("\n💡 L'ACP a réduit les données de 4 dimensions à 2 dimensions")
    print(f"   tout en conservant {sum(pca.explained_variance_ratio_)*100:.2f}% de l'information !")

if __name__ == "__main__":
    main()
