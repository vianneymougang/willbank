import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';
import { environment } from '../../environments/environment';

// Mode mock pour tester sans backend
const MOCK_MODE = true;

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = `${environment.apiGatewayUrl}/api/v1`;
  
  // Données mockées - Clients
  private mockClients = [
    { id: 1, nom: 'Dupont', prenom: 'Jean', email: 'jean.dupont@email.com', telephone: '0612345678', adresse: '123 Rue de Paris' },
    { id: 2, nom: 'Martin', prenom: 'Marie', email: 'marie.martin@email.com', telephone: '0623456789', adresse: '456 Avenue des Champs' },
    { id: 3, nom: 'Bernard', prenom: 'Pierre', email: 'pierre.bernard@email.com', telephone: '0634567890', adresse: '789 Boulevard Victor Hugo' },
    { id: 4, nom: 'Dubois', prenom: 'Sophie', email: 'sophie.dubois@email.com', telephone: '0645678901', adresse: '321 Rue de la République' },
    { id: 5, nom: 'Thomas', prenom: 'Luc', email: 'luc.thomas@email.com', telephone: '0656789012', adresse: '654 Place de la Liberté' }
  ];

  // Données mockées - Comptes
  private mockAccounts = [
    { id: 1, type: 'courant', solde: 2500.50, clientId: 1, numeroCompte: 'FR7612345678901234567890123', dateCreation: '2024-01-15' },
    { id: 2, type: 'epargne', solde: 15000.00, clientId: 1, numeroCompte: 'FR7612345678901234567890124', dateCreation: '2024-02-20' },
    { id: 3, type: 'courant', solde: 3200.75, clientId: 2, numeroCompte: 'FR7612345678901234567890125', dateCreation: '2024-01-10' },
    { id: 4, type: 'professionnel', solde: 45000.00, clientId: 3, numeroCompte: 'FR7612345678901234567890126', dateCreation: '2024-03-05' },
    { id: 5, type: 'courant', solde: 1800.25, clientId: 4, numeroCompte: 'FR7612345678901234567890127', dateCreation: '2024-02-15' }
  ];

  // Données mockées - Transactions
  private mockTransactions = [
    { id: 1, type: 'virement', montant: 500.00, telephoneSource: '0612345678', telephoneDest: '0623456789', date: '2024-11-25', statut: 'completed' },
    { id: 2, type: 'depot', montant: 1000.00, telephoneSource: '0634567890', telephoneDest: null, date: '2024-11-26', statut: 'completed' },
    { id: 3, type: 'retrait', montant: 200.00, telephoneSource: '0645678901', telephoneDest: null, date: '2024-11-27', statut: 'completed' },
    { id: 4, type: 'virement', montant: 750.50, telephoneSource: '0623456789', telephoneDest: '0656789012', date: '2024-11-28', statut: 'pending' },
    { id: 5, type: 'depot', montant: 2000.00, telephoneSource: '0612345678', telephoneDest: null, date: '2024-11-29', statut: 'completed' }
  ];

  // Données mockées - Notifications
  private mockNotifications = [
    { id: 1, clientId: 1, message: 'Votre virement de 500€ a été effectué', date: '2024-11-25', type: 'transaction', lu: true },
    { id: 2, clientId: 2, message: 'Nouveau dépôt de 1000€ sur votre compte', date: '2024-11-26', type: 'transaction', lu: false },
    { id: 3, clientId: 1, message: 'Votre relevé mensuel est disponible', date: '2024-11-27', type: 'info', lu: false }
  ];

  constructor(private http: HttpClient) {}

  // ========== CLIENTS ==========
  getClients(): Observable<any[]> {
    if (MOCK_MODE) {
      return of([...this.mockClients]).pipe(delay(300));
    }
    return this.http.get<any[]>(`${this.baseUrl}/clients`);
  }

  getClient(id: number): Observable<any> {
    if (MOCK_MODE) {
      const client = this.mockClients.find(c => c.id === id);
      return of(client ? { ...client } : null).pipe(delay(300));
    }
    return this.http.get<any>(`${this.baseUrl}/clients/${id}`);
  }

  searchClients(searchText: string): Observable<any[]> {
    if (MOCK_MODE) {
      const searchLower = searchText.toLowerCase();
      const filtered = this.mockClients.filter(client => 
        client.nom.toLowerCase().includes(searchLower) ||
        client.prenom.toLowerCase().includes(searchLower) ||
        client.email.toLowerCase().includes(searchLower) ||
        client.telephone.includes(searchText) ||
        client.adresse.toLowerCase().includes(searchLower)
      );
      return of([...filtered]).pipe(delay(300));
    }
    return this.http.get<any[]>(`${this.baseUrl}/clients/search`, { 
      params: { q: searchText } 
    });
  }

  createClient(client: any): Observable<any> {
    if (MOCK_MODE) {
      const newClient = { ...client, id: Math.max(...this.mockClients.map(c => c.id)) + 1 };
      this.mockClients.push(newClient);
      
      // Format ApiResponse avec notification
      return of({
        data: newClient,
        notification: {
          type: 'success',
          title: 'Client créé',
          message: `Le client "${client.nom} ${client.prenom}" a été créé avec succès`,
          action: 'view-client',
          actionData: { clientId: newClient.id }
        }
      }).pipe(delay(300));
    }
    return this.http.post<any>(`${this.baseUrl}/clients`, client);
  }

  updateClient(id: number, client: any): Observable<any> {
    if (MOCK_MODE) {
      const index = this.mockClients.findIndex(c => c.id === id);
      if (index !== -1) {
        this.mockClients[index] = { ...client, id };
      }
      
      // Format ApiResponse avec notification
      return of({
        data: this.mockClients[index],
        notification: {
          type: 'info',
          title: 'Client modifié',
          message: `Les informations du client "${client.nom} ${client.prenom}" ont été mises à jour`,
          action: 'view-client',
          actionData: { clientId: id }
        }
      }).pipe(delay(300));
    }
    return this.http.put<any>(`${this.baseUrl}/clients/${id}`, client);
  }

  deleteClient(id: number): Observable<any> {
    if (MOCK_MODE) {
      const index = this.mockClients.findIndex(c => c.id === id);
      let clientName = '';
      if (index !== -1) {
        const client = this.mockClients[index];
        clientName = `${client.nom} ${client.prenom}`;
        this.mockClients.splice(index, 1);
      }
      
      // Format ApiResponse avec notification
      return of({
        notification: {
          type: 'warning',
          title: 'Client supprimé',
          message: `Le client "${clientName}" a été supprimé`
        }
      }).pipe(delay(300));
    }
    return this.http.delete<any>(`${this.baseUrl}/clients/${id}`);
  }

  // ========== ACCOUNTS ==========
  getAccounts(): Observable<any[]> {
    if (MOCK_MODE) {
      return of([...this.mockAccounts]).pipe(delay(300));
    }
    return this.http.get<any[]>(`${this.baseUrl}/accounts`);
  }

  getAccount(id: number): Observable<any> {
    if (MOCK_MODE) {
      const account = this.mockAccounts.find(a => a.id === id);
      return of(account ? { ...account } : null).pipe(delay(300));
    }
    return this.http.get<any>(`${this.baseUrl}/accounts/${id}`);
  }

  getAccountsByClient(clientId: number): Observable<any[]> {
    if (MOCK_MODE) {
      const accounts = this.mockAccounts.filter(a => a.clientId === clientId);
      return of([...accounts]).pipe(delay(300));
    }
    return this.http.get<any[]>(`${this.baseUrl}/clients/${clientId}/accounts`);
  }

  createAccount(account: any): Observable<any> {
    if (MOCK_MODE) {
      const newAccount = { 
        ...account, 
        id: Math.max(...this.mockAccounts.map(a => a.id)) + 1,
        numeroCompte: `FR76${Math.random().toString().slice(2, 25)}`,
        dateCreation: new Date().toISOString().split('T')[0]
      };
      this.mockAccounts.push(newAccount);
      
      const notifications = [];
      
      // Notification principale
      notifications.push({
        type: 'success',
        title: 'Nouveau compte',
        message: `Un compte ${account.type} a été créé avec un solde de ${account.solde}€`,
        action: 'view-account',
        actionData: { accountId: newAccount.id }
      });
      
      // Notification supplémentaire si solde faible
      if (account.solde < 100) {
        notifications.push({
          type: 'warning',
          title: 'Solde faible',
          message: 'Le nouveau compte a un solde inférieur à 100€',
          action: 'view-account',
          actionData: { accountId: newAccount.id }
        });
      }
      
      // Format ApiResponse avec notifications multiples
      return of({
        data: newAccount,
        notifications: notifications
      }).pipe(delay(300));
    }
    return this.http.post<any>(`${this.baseUrl}/accounts`, account);
  }

  updateAccount(id: number, account: any): Observable<any> {
    if (MOCK_MODE) {
      const index = this.mockAccounts.findIndex(a => a.id === id);
      if (index !== -1) {
        this.mockAccounts[index] = { ...this.mockAccounts[index], ...account, id };
      }
      
      // Format ApiResponse avec notification
      return of({
        data: this.mockAccounts[index],
        notification: {
          type: 'info',
          title: 'Compte modifié',
          message: `Le compte ${account.type} a été mis à jour`,
          action: 'view-account',
          actionData: { accountId: id }
        }
      }).pipe(delay(300));
    }
    return this.http.put<any>(`${this.baseUrl}/accounts/${id}`, account);
  }

  deleteAccount(id: number): Observable<any> {
    if (MOCK_MODE) {
      const index = this.mockAccounts.findIndex(a => a.id === id);
      let accountInfo = '';
      if (index !== -1) {
        const account = this.mockAccounts[index];
        accountInfo = `${account.type} (${account.numeroCompte})`;
        this.mockAccounts.splice(index, 1);
      }
      
      // Format ApiResponse avec notification
      return of({
        notification: {
          type: 'warning',
          title: 'Compte supprimé',
          message: `Le compte ${accountInfo} a été supprimé`
        }
      }).pipe(delay(300));
    }
    return this.http.delete<any>(`${this.baseUrl}/accounts/${id}`);
  }

  // ========== TRANSACTIONS ==========
  getTransactions(): Observable<any[]> {
    if (MOCK_MODE) {
      return of([...this.mockTransactions]).pipe(delay(300));
    }
    return this.http.get<any[]>(`${this.baseUrl}/transactions`);
  }

  getTransaction(id: number): Observable<any> {
    if (MOCK_MODE) {
      const transaction = this.mockTransactions.find(t => t.id === id);
      return of(transaction ? { ...transaction } : null).pipe(delay(300));
    }
    return this.http.get<any>(`${this.baseUrl}/transactions/${id}`);
  }

  createTransfer(transfer: any): Observable<any> {
    if (MOCK_MODE) {
      const newTransaction = { 
        ...transfer, 
        id: Math.max(...this.mockTransactions.map(t => t.id)) + 1,
        date: new Date().toISOString().split('T')[0],
        statut: 'completed'
      };
      this.mockTransactions.push(newTransaction);
      return of(newTransaction).pipe(delay(500));
    }
    return this.http.post<any>(`${this.baseUrl}/transactions/transfer`, transfer);
  }

  updateTransaction(id: number, transaction: any): Observable<any> {
    if (MOCK_MODE) {
      const index = this.mockTransactions.findIndex(t => t.id === id);
      if (index !== -1) {
        this.mockTransactions[index] = { ...this.mockTransactions[index], ...transaction, id };
      }
      return of(this.mockTransactions[index]).pipe(delay(300));
    }
    return this.http.put<any>(`${this.baseUrl}/transactions/${id}`, transaction);
  }

  deleteTransaction(id: number): Observable<void> {
    if (MOCK_MODE) {
      const index = this.mockTransactions.findIndex(t => t.id === id);
      if (index !== -1) {
        this.mockTransactions.splice(index, 1);
      }
      return of(void 0).pipe(delay(300));
    }
    return this.http.delete<void>(`${this.baseUrl}/transactions/${id}`);
  }

  // ========== NOTIFICATIONS ==========
  getNotifications(): Observable<any[]> {
    if (MOCK_MODE) {
      return of([...this.mockNotifications]).pipe(delay(300));
    }
    return this.http.get<any[]>(`${this.baseUrl}/notifications`);
  }

  sendNotification(notification: any): Observable<any> {
    if (MOCK_MODE) {
      const newNotification = { 
        ...notification, 
        id: Math.max(...this.mockNotifications.map(n => n.id)) + 1,
        date: new Date().toISOString().split('T')[0],
        lu: false
      };
      this.mockNotifications.push(newNotification);
      return of(newNotification).pipe(delay(500));
    }
    return this.http.post<any>(`${this.baseUrl}/notifications/send`, notification);
  }

  // ========== COMPOSITE ==========
  getClientDashboard(clientId: number): Observable<any> {
    if (MOCK_MODE) {
      const client = this.mockClients.find(c => c.id === clientId);
      const accounts = this.mockAccounts.filter(a => a.clientId === clientId);
      const totalSolde = accounts.reduce((sum, acc) => sum + acc.solde, 0);
      const recentTransactions = this.mockTransactions.slice(0, 5);
      
      return of({
        client,
        accounts,
        totalSolde,
        nombreComptes: accounts.length,
        recentTransactions
      }).pipe(delay(300));
    }
    return this.http.get<any>(`${this.baseUrl}/composite/clients/${clientId}/dashboard`);
  }

  getAccountStatement(accountId: number, period: string): Observable<any> {
    if (MOCK_MODE) {
      const account = this.mockAccounts.find(a => a.id === accountId);
      const transactions = this.mockTransactions.filter(t => 
        t.telephoneSource === account?.numeroCompte || t.telephoneDest === account?.numeroCompte
      );
      
      return of({
        account,
        period,
        transactions,
        soldeDebut: (account?.solde || 0) - 1000,
        soldeFin: account?.solde || 0
      }).pipe(delay(300));
    }
    return this.http.get<any>(`${this.baseUrl}/composite/accounts/${accountId}/statement?period=${period}`);
  }

  searchTransactions(params: any): Observable<any[]> {
    if (MOCK_MODE) {
      let results = [...this.mockTransactions];
      
      if (params.type) {
        results = results.filter(t => t.type === params.type);
      }
      if (params.dateDebut) {
        results = results.filter(t => t.date >= params.dateDebut);
      }
      if (params.dateFin) {
        results = results.filter(t => t.date <= params.dateFin);
      }
      
      return of(results).pipe(delay(300));
    }
    return this.http.get<any[]>(`${this.baseUrl}/composite/transactions/search`, { params });
  }
}
