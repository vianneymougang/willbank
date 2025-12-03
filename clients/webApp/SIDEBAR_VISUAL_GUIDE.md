# 📱 Guide Visuel de la Sidebar

## Structure de la Sidebar

```
┌─────────────────────────────────────┐
│  ╔═══════════════════════════════╗  │
│  ║   [🏢]  WillBank              ║  │ ← Header avec dégradé bleu
│  ║         CRM Platform      [×] ║  │
│  ╚═══════════════════════════════╝  │
├─────────────────────────────────────┤
│  ┌───────────────────────────────┐  │
│  │  [JD]  Jean Dupont            │  │ ← Profil utilisateur
│  │        ● Administrateur       │  │   (fond gris clair)
│  └───────────────────────────────┘  │
├─────────────────────────────────────┤
│                                     │
│  NAVIGATION                         │ ← Section titre
│  ┌───────────────────────────────┐  │
│  │ 🏠  Tableau de bord           │  │ ← Item normal
│  └───────────────────────────────┘  │
│  ╔═══════════════════════════════╗  │
│  ║ 👥  Clients              [5]  ║  │ ← Item actif (dégradé)
│  ╚═══════════════════════════════╝  │
│  ┌───────────────────────────────┐  │
│  │ 📄  Contrats             [3]  │  │ ← Item avec badge
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │ 📊  Rapports                  │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │ ⚙️  Paramètres                │  │
│  └───────────────────────────────┘  │
│                                     │
│  SUPPORT                            │ ← Section titre
│  ┌───────────────────────────────┐  │
│  │ ❓  Aide & Support            │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │ 📚  Documentation             │  │
│  └───────────────────────────────┘  │
│                                     │
├─────────────────────────────────────┤
│  ╔═══════════════════════════════╗  │
│  ║         ⭐ (pulse)            ║  │ ← Carte upgrade
│  ║      Version Pro              ║  │   (dégradé violet)
│  ║  Débloquez toutes les         ║  │
│  ║  fonctionnalités              ║  │
│  ║  ┌─────────────────────────┐  ║  │
│  ║  │  Mettre à niveau        │  ║  │
│  ║  └─────────────────────────┘  ║  │
│  ╚═══════════════════════════════╝  │
└─────────────────────────────────────┘
```

## États des éléments de navigation

### 1. État Normal
```
┌───────────────────────────────┐
│ 🏠  Tableau de bord           │  Fond: transparent
└───────────────────────────────┘  Texte: #2c3e50
                                   Icône: #7f8c8d
```

### 2. État Hover
```
┌───────────────────────────────┐
│   🏠  Tableau de bord         │  Fond: rgba(30,58,95,0.05)
└───────────────────────────────┘  Translation: +5px
                                   Icône: #1E3A5F (scale 1.1)
```

### 3. État Actif
```
╔═══════════════════════════════╗
║ 👥  Clients              [5]  ║  Fond: gradient bleu
╚═══════════════════════════════╝  Texte: white
                                   Ombre: 0 4px 12px
```

### 4. Avec Badge
```
┌───────────────────────────────┐
│ 📄  Contrats             [3]  │  Badge vert: #00B894
└───────────────────────────────┘  Badge orange: #F39C12
```

## Palette de couleurs

### Header
```
┌─────────────────────────────────┐
│ Gradient: #1E3A5F → #2d5a8f    │
│ Texte: white                    │
│ Logo fond: white                │
└─────────────────────────────────┘
```

### Profil utilisateur
```
┌─────────────────────────────────┐
│ Fond: #f8f9fa                   │
│ Avatar: #1E3A5F                 │
│ Indicateur: #00B894 (vert)      │
└─────────────────────────────────┘
```

### Navigation
```
┌─────────────────────────────────┐
│ Titre section: #95a5a6          │
│ Item normal: #2c3e50            │
│ Item hover: rgba(30,58,95,0.05) │
│ Item actif: gradient #1E3A5F    │
└─────────────────────────────────┘
```

### Carte Upgrade
```
┌─────────────────────────────────┐
│ Gradient: #667eea → #764ba2     │
│ Texte: white                    │
│ Bouton: white / texte #667eea   │
└─────────────────────────────────┘
```

## Animations

### Hover sur item
```
Avant:  │ 🏠  Dashboard           │
        └─────────────────────────┘

Après:  │   🏠  Dashboard         │  ← Translation +5px
        └─────────────────────────┘     Icône scale 1.1
```

### Pulse sur icône upgrade
```
Frame 1:  ⭐  (scale 1.0)
Frame 2:  ⭐  (scale 1.1)  ← 50%
Frame 3:  ⭐  (scale 1.0)  ← 100%
```

### Ripple au clic
```
Clic:  │ 🏠  Dashboard           │
       └─────────────────────────┘
          ↓
       │ 🏠  Dashboard    ○      │  ← Onde qui s'étend
       └─────────────────────────┘
```

## Responsive Behavior

### Desktop (> 768px)
```
Largeur: 300px
┌─────────────────────────────────┐
│  [🏢]  WillBank CRM        [×]  │
│  [JD]  Jean Dupont              │
│        ● Administrateur         │
│  🏠  Tableau de bord            │
│  👥  Clients              [5]   │
│  ...                            │
└─────────────────────────────────┘
```

### Mobile (< 768px)
```
Largeur: 280px
┌───────────────────────────┐
│  [🏢]  WillBank      [×]  │
│      [JD]                 │
│   Jean Dupont             │
│ ● Administrateur          │
│  🏠  Tableau de bord      │
│  👥  Clients        [5]   │
│  ...                      │
└───────────────────────────┘
```

## Scrollbar personnalisée

```
Normal:     │ ║ │  ← 6px, gris clair
Hover:      │ ▌ │  ← Plus foncé
```

## Interactions

### Ouvrir
```
Toolbar: [☰] ← Clic
         ↓
Sidebar apparaît de gauche
avec overlay modal
```

### Fermer
```
1. Clic sur [×]
2. Clic sur overlay
3. Navigation vers une page
   ↓
Sidebar disparaît vers la gauche
```

### Navigation
```
Clic sur item
   ↓
1. Ripple effect
2. Route change
3. Sidebar se ferme
4. Item devient actif
```

## Espacement

```
Padding header:     1.5rem
Padding profil:     1.5rem
Padding navigation: 1rem 0
Padding item:       0.875rem 1rem
Gap icône-texte:    1rem
Margin items:       0.25rem 0.75rem
Border-radius:      10px
```

## Typographie

```
Header titre:       1.25rem, bold
Header subtitle:    0.875rem
Profil nom:         1rem, semi-bold
Profil rôle:        0.875rem
Section titre:      0.75rem, uppercase
Item label:         0.95rem, medium
Badge:              0.75rem, semi-bold
Upgrade titre:      1.1rem, bold
Upgrade texte:      0.875rem
```
