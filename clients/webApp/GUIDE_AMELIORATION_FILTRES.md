# 🔍 Guide d'Amélioration des Filtres de Transactions

## 🎯 Objectif
Améliorer le système de filtres et recherche pour les transactions avec des fonctionnalités avancées.

---

## 📝 Étape 1 : Mettre à jour le composant de liste des transactions

### Fichier : `src/app/modules/transactions/transaction-list/transaction-list.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { FormBuilder, FormGroup } from '@angular/forms';

interface TransactionFilter {
  type?: string;
  statut?: string;
  dateDebut?: string;
  dateFin?: string;
  montantMin?: number;
  montantMax?: number;
  searchText?: string;
}

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.scss']
})
export class TransactionListComponent implements OnInit {
  transactions: any[] = [];
  filteredTransactions: any[] = [];
  filterForm: FormGroup;
  showFilters = false;

  // Options pour les dropdowns
  transactionTypes = [
    { label: 'Tous', value: '' },
    { label: 'Virement', value: 'virement' },
    { label: 'Dépôt', value: 'depot' },
    { label: 'Retrait', value: 'retrait' }
  ];

  statuts = [
    { label: 'Tous', value: '' },
    { label: 'Complété', value: 'completed' },
    { label: 'En attente', value: 'pending' },
    { label: 'Échoué', value: 'failed' }
  ];

  constructor(
    private apiService: ApiService, 
    private router: Router,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      type: [''],
      statut: [''],
      dateDebut: [''],
      dateFin: [''],
      montantMin: [''],
      montantMax: [''],
      searchText: ['']
    });
  }

  ngOnInit() {
    this.loadTransactions();
    
    // Écouter les changements du formulaire
    this.filterForm.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  loadTransactions() {
    this.apiService.getTransactions().subscribe(data => {
      this.transactions = data;
      this.filteredTransactions = [...data];
    });
  }

  applyFilters() {
    const filters: TransactionFilter = this.filterForm.value;
    
    this.filteredTransactions = this.transactions.filter(transaction => {
      // Filtre par type
      if (filters.type && transaction.type !== filters.type) {
        return false;
      }

      // Filtre par statut
      if (filters.statut && transaction.statut !== filters.statut) {
        return false;
      }

      // Filtre par date début
      if (filters.dateDebut && transaction.date < filters.dateDebut) {
        return false;
      }

      // Filtre par date fin
      if (filters.dateFin && transaction.date > filters.dateFin) {
        return false;
      }

      // Filtre par montant minimum
      if (filters.montantMin && transaction.montant < filters.montantMin) {
        return false;
      }

      // Filtre par montant maximum
      if (filters.montantMax && transaction.montant > filters.montantMax) {
        return false;
      }

      // Recherche textuelle
      if (filters.searchText) {
        const searchLower = filters.searchText.toLowerCase();
        const matchesPhone = transaction.telephoneSource?.includes(searchLower) || 
                           transaction.telephoneDest?.includes(searchLower);
        const matchesType = transaction.type?.toLowerCase().includes(searchLower);
        const matchesStatut = transaction.statut?.toLowerCase().includes(searchLower);
        
        if (!matchesPhone && !matchesType && !matchesStatut) {
          return false;
        }
      }

      return true;
    });
  }

  resetFilters() {
    this.filterForm.reset({
      type: '',
      statut: '',
      dateDebut: '',
      dateFin: '',
      montantMin: '',
      montantMax: '',
      searchText: ''
    });
  }

  toggleFilters() {
    this.showFilters = !this.showFilters;
  }

  exportToCSV() {
    // Logique d'export CSV
    const csv = this.convertToCSV(this.filteredTransactions);
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `transactions_${new Date().toISOString()}.csv`;
    link.click();
  }

  private convertToCSV(data: any[]): string {
    const headers = ['ID', 'Type', 'Montant', 'Téléphone Source', 'Téléphone Dest', 'Date', 'Statut'];
    const rows = data.map(t => [
      t.id,
      t.type,
      t.montant,
      t.telephoneSource || '',
      t.telephoneDest || '',
      t.date,
      t.statut
    ]);
    
    return [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n');
  }

  view(id: number) {
    this.router.navigate(['/transactions', id]);
  }

  edit(id: number) {
    this.router.navigate(['/transactions', id, 'edit']);
  }

  delete(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette transaction ?')) {
      this.apiService.deleteTransaction(id).subscribe(() => {
        this.transactions = this.transactions.filter(t => t.id !== id);
        this.applyFilters();
      });
    }
  }

  add() {
    this.router.navigate(['/transactions/new']);
  }
}
```

---

## 🎨 Étape 2 : Mettre à jour le template HTML

### Fichier : `src/app/modules/transactions/transaction-list/transaction-list.component.html`

```html
<div class="transaction-list-container">
  <!-- En-tête avec actions -->
  <div class="list-header">
    <h2><i class="pi pi-money-bill"></i> Gestion des Transactions</h2>
    <div class="header-actions">
      <button pButton 
              label="Filtres" 
              icon="pi pi-filter" 
              class="p-button-outlined"
              [badge]="getActiveFiltersCount()"
              (click)="toggleFilters()">
      </button>
      <button pButton 
              label="Exporter CSV" 
              icon="pi pi-download" 
              class="p-button-outlined p-button-success"
              (click)="exportToCSV()">
      </button>
      <button pButton 
              label="Nouvelle Transaction" 
              icon="pi pi-plus" 
              (click)="add()">
      </button>
    </div>
  </div>

  <!-- Panneau de filtres -->
  <p-panel *ngIf="showFilters" [toggleable]="true" [collapsed]="false">
    <ng-template pTemplate="header">
      <span><i class="pi pi-filter"></i> Filtres avancés</span>
    </ng-template>
    
    <form [formGroup]="filterForm" class="filter-form">
      <div class="filter-grid">
        <!-- Recherche textuelle -->
        <div class="filter-field full-width">
          <label>Recherche</label>
          <span class="p-input-icon-left">
            <i class="pi pi-search"></i>
            <input pInputText 
                   type="text" 
                   formControlName="searchText"
                   placeholder="Rechercher par téléphone, type, statut...">
          </span>
        </div>

        <!-- Type de transaction -->
        <div class="filter-field">
          <label>Type de transaction</label>
          <p-dropdown [options]="transactionTypes" 
                      formControlName="type"
                      placeholder="Sélectionner un type"
                      [showClear]="true">
          </p-dropdown>
        </div>

        <!-- Statut -->
        <div class="filter-field">
          <label>Statut</label>
          <p-dropdown [options]="statuts" 
                      formControlName="statut"
                      placeholder="Sélectionner un statut"
                      [showClear]="true">
          </p-dropdown>
        </div>

        <!-- Date début -->
        <div class="filter-field">
          <label>Date début</label>
          <p-calendar formControlName="dateDebut"
                      dateFormat="yy-mm-dd"
                      [showIcon]="true"
                      placeholder="Date début">
          </p-calendar>
        </div>

        <!-- Date fin -->
        <div class="filter-field">
          <label>Date fin</label>
          <p-calendar formControlName="dateFin"
                      dateFormat="yy-mm-dd"
                      [showIcon]="true"
                      placeholder="Date fin">
          </p-calendar>
        </div>

        <!-- Montant minimum -->
        <div class="filter-field">
          <label>Montant minimum (€)</label>
          <p-inputNumber formControlName="montantMin"
                         mode="currency"
                         currency="EUR"
                         locale="fr-FR"
                         placeholder="0.00">
          </p-inputNumber>
        </div>

        <!-- Montant maximum -->
        <div class="filter-field">
          <label>Montant maximum (€)</label>
          <p-inputNumber formControlName="montantMax"
                         mode="currency"
                         currency="EUR"
                         locale="fr-FR"
                         placeholder="0.00">
          </p-inputNumber>
        </div>
      </div>

      <div class="filter-actions">
        <button pButton 
                label="Réinitialiser" 
                icon="pi pi-refresh" 
                class="p-button-outlined p-button-secondary"
                (click)="resetFilters()">
        </button>
      </div>
    </form>
  </p-panel>

  <!-- Résultats -->
  <div class="results-info">
    <span class="results-count">
      <i class="pi pi-list"></i>
      {{ filteredTransactions.length }} transaction(s) trouvée(s)
    </span>
  </div>

  <!-- Tableau des transactions -->
  <p-table [value]="filteredTransactions" 
           [paginator]="true" 
           [rows]="10"
           [rowsPerPageOptions]="[10, 25, 50]"
           [showCurrentPageReport]="true"
           currentPageReportTemplate="Affichage de {first} à {last} sur {totalRecords} transactions"
           [globalFilterFields]="['type', 'statut', 'telephoneSource', 'telephoneDest']"
           styleClass="p-datatable-striped">
    
    <ng-template pTemplate="header">
      <tr>
        <th pSortableColumn="id">
          ID <p-sortIcon field="id"></p-sortIcon>
        </th>
        <th pSortableColumn="type">
          Type <p-sortIcon field="type"></p-sortIcon>
        </th>
        <th pSortableColumn="montant">
          Montant <p-sortIcon field="montant"></p-sortIcon>
        </th>
        <th>Téléphone Source</th>
        <th>Téléphone Dest</th>
        <th pSortableColumn="date">
          Date <p-sortIcon field="date"></p-sortIcon>
        </th>
        <th pSortableColumn="statut">
          Statut <p-sortIcon field="statut"></p-sortIcon>
        </th>
        <th>Actions</th>
      </tr>
    </ng-template>
    
    <ng-template pTemplate="body" let-transaction>
      <tr>
        <td>{{ transaction.id }}</td>
        <td>
          <span class="transaction-type" [ngClass]="'type-' + transaction.type">
            <i [class]="getTypeIcon(transaction.type)"></i>
            {{ transaction.type | titlecase }}
          </span>
        </td>
        <td>
          <span class="transaction-amount">
            {{ transaction.montant | currency:'EUR':'symbol':'1.2-2' }}
          </span>
        </td>
        <td>{{ transaction.telephoneSource }}</td>
        <td>{{ transaction.telephoneDest || '-' }}</td>
        <td>{{ transaction.date | date:'dd/MM/yyyy' }}</td>
        <td>
          <span class="status-badge" [ngClass]="'status-' + transaction.statut">
            {{ getStatutLabel(transaction.statut) }}
          </span>
        </td>
        <td>
          <div class="action-buttons">
            <button pButton 
                    icon="pi pi-eye" 
                    class="p-button-rounded p-button-info p-button-sm" 
                    pTooltip="Voir"
                    (click)="view(transaction.id)">
            </button>
            <button pButton 
                    icon="pi pi-pencil" 
                    class="p-button-rounded p-button-warning p-button-sm" 
                    pTooltip="Modifier"
                    (click)="edit(transaction.id)">
            </button>
            <button pButton 
                    icon="pi pi-trash" 
                    class="p-button-rounded p-button-danger p-button-sm" 
                    pTooltip="Supprimer"
                    (click)="delete(transaction.id)">
            </button>
          </div>
        </td>
      </tr>
    </ng-template>

    <ng-template pTemplate="emptymessage">
      <tr>
        <td colspan="8" class="empty-message">
          <i class="pi pi-info-circle"></i>
          Aucune transaction trouvée
        </td>
      </tr>
    </ng-template>
  </p-table>
</div>
```

---

## 🎨 Étape 3 : Ajouter les styles CSS

### Fichier : `src/app/modules/transactions/transaction-list/transaction-list.component.scss`

```scss
.transaction-list-container {
  padding: 1.5rem;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;

  h2 {
    margin: 0;
    color: #333;
    display: flex;
    align-items: center;
    gap: 0.5rem;

    i {
      color: #667eea;
    }
  }

  .header-actions {
    display: flex;
    gap: 0.75rem;
  }
}

.filter-form {
  padding: 1rem 0;

  .filter-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1rem;
    margin-bottom: 1rem;

    .filter-field {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;

      &.full-width {
        grid-column: 1 / -1;
      }

      label {
        font-weight: 600;
        color: #555;
        font-size: 0.9rem;
      }

      input, ::ng-deep .p-dropdown, ::ng-deep .p-calendar {
        width: 100%;
      }
    }
  }

  .filter-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
  }
}

.results-info {
  margin: 1rem 0;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 4px;

  .results-count {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 600;
    color: #555;

    i {
      color: #667eea;
    }
  }
}

.transaction-type {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;

  &.type-virement {
    background: #e3f2fd;
    color: #1976d2;
  }

  &.type-depot {
    background: #e8f5e9;
    color: #388e3c;
  }

  &.type-retrait {
    background: #fff3e0;
    color: #f57c00;
  }
}

.transaction-amount {
  font-weight: 700;
  color: #2c3e50;
  font-size: 1rem;
}

.status-badge {
  display: inline-block;
  padding: 0.35rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;

  &.status-completed {
    background: #d4edda;
    color: #155724;
  }

  &.status-pending {
    background: #fff3cd;
    color: #856404;
  }

  &.status-failed {
    background: #f8d7da;
    color: #721c24;
  }
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.empty-message {
  text-align: center;
  padding: 2rem;
  color: #999;
  font-size: 1.1rem;

  i {
    font-size: 2rem;
    display: block;
    margin-bottom: 0.5rem;
  }
}

@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;

    .header-actions {
      width: 100%;
      flex-wrap: wrap;
    }
  }

  .filter-grid {
    grid-template-columns: 1fr !important;
  }
}
```

---

## 🔧 Étape 4 : Ajouter les méthodes helper dans le component

Ajouter ces méthodes dans `transaction-list.component.ts` :

```typescript
getTypeIcon(type: string): string {
  const icons: { [key: string]: string } = {
    'virement': 'pi pi-arrow-right-arrow-left',
    'depot': 'pi pi-arrow-down',
    'retrait': 'pi pi-arrow-up'
  };
  return icons[type] || 'pi pi-circle';
}

getStatutLabel(statut: string): string {
  const labels: { [key: string]: string } = {
    'completed': 'Complété',
    'pending': 'En attente',
    'failed': 'Échoué'
  };
  return labels[statut] || statut;
}

getActiveFiltersCount(): string {
  const filters = this.filterForm.value;
  let count = 0;
  
  Object.keys(filters).forEach(key => {
    if (filters[key] && filters[key] !== '') {
      count++;
    }
  });
  
  return count > 0 ? count.toString() : '';
}
```

---

## ✅ Fonctionnalités Ajoutées

1. ✅ **Recherche textuelle** globale
2. ✅ **Filtres par type** de transaction
3. ✅ **Filtres par statut**
4. ✅ **Filtres par période** (date début/fin)
5. ✅ **Filtres par montant** (min/max)
6. ✅ **Tri des colonnes**
7. ✅ **Pagination avancée**
8. ✅ **Export CSV**
9. ✅ **Compteur de résultats**
10. ✅ **Badge de filtres actifs**
11. ✅ **Design moderne** avec badges colorés
12. ✅ **Responsive** pour mobile

---

## 🎯 Prochaines améliorations possibles

- [ ] Sauvegarde des filtres dans localStorage
- [ ] Filtres prédéfinis (aujourd'hui, cette semaine, ce mois)
- [ ] Export PDF en plus du CSV
- [ ] Graphique des transactions filtrées
- [ ] Recherche avancée avec opérateurs (AND, OR)
