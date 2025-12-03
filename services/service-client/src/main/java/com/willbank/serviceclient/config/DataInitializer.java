package com.willbank.serviceclient.config;

import com.willbank.serviceclient.model.Client;
import com.willbank.serviceclient.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ClientRepository clientRepository;

    public DataInitializer(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si des données existent déjà
        if (clientRepository.count() > 0) {
            System.out.println("Des clients existent déjà dans la base de données. Initialisation ignorée.");
            return;
        }

        System.out.println("Initialisation de 15 clients camerounais de test...");

        int counter = 1;
        List<Client> clients = Arrays.asList(
            createClient("Jean", "Nkeng", "jean.nkeng@email.com", "+237612345678",
                LocalDate.of(1985, 3, 15), "123 Avenue Kennedy", "Yaoundé", "BP 1234", "Cameroun", counter++),
            
            createClient("Marie", "Fotso", "marie.fotso@email.com", "+237623456789",
                LocalDate.of(1990, 7, 22), "45 Boulevard de la Liberté", "Douala", "BP 5678", "Cameroun", counter++),
            
            createClient("Pierre", "Mvondo", "pierre.mvondo@email.com", "+237634567890",
                LocalDate.of(1988, 11, 5), "78 Rue du Marché Central", "Bafoussam", "BP 9012", "Cameroun", counter++),
            
            createClient("Sophie", "Tchoumi", "sophie.tchoumi@email.com", "+237645678901",
                LocalDate.of(1992, 2, 18), "12 Avenue des Cocotiers", "Garoua", "BP 3456", "Cameroun", counter++),
            
            createClient("Luc", "Ndjock", "luc.ndjock@email.com", "+237656789012",
                LocalDate.of(1987, 9, 30), "56 Boulevard de l'Indépendance", "Buea", "BP 7890", "Cameroun", counter++),
            
            createClient("Emma", "Tchinda", "emma.tchinda@email.com", "+237667890123",
                LocalDate.of(1995, 4, 12), "89 Rue du Port", "Limbé", "BP 2345", "Cameroun", counter++),
            
            createClient("Thomas", "Nguimfack", "thomas.nguimfack@email.com", "+237678901234",
                LocalDate.of(1983, 6, 25), "34 Avenue Ahmadou Ahidjo", "Bamenda", "BP 6789", "Cameroun", counter++),
            
            createClient("Julie", "Mefire", "julie.mefire@email.com", "+237689012345",
                LocalDate.of(1991, 1, 8), "67 Boulevard de la République", "Kribi", "BP 1235", "Cameroun", counter++),
            
            createClient("Antoine", "Ntamack", "antoine.ntamack@email.com", "+237690123456",
                LocalDate.of(1989, 8, 14), "23 Rue du Stade", "Ebolowa", "BP 5679", "Cameroun", counter++),
            
            createClient("Camille", "Nkono", "camille.nkono@email.com", "+237601234567",
                LocalDate.of(1993, 12, 3), "45 Avenue du Palais", "Maroua", "BP 9013", "Cameroun", counter++),
            
            createClient("Nicolas", "Tchakounte", "nicolas.tchakounte@email.com", "+237613456789",
                LocalDate.of(1986, 5, 20), "12 Boulevard de l'Université", "Dschang", "BP 3457", "Cameroun", counter++),
            
            createClient("Laura", "Ngo", "laura.ngo@email.com", "+237624567890",
                LocalDate.of(1994, 10, 7), "78 Rue des Banques", "Foumban", "BP 7891", "Cameroun", counter++),
            
            createClient("Alexandre", "Mvogo", "alexandre.mvogo@email.com", "+237635678901",
                LocalDate.of(1984, 3, 28), "34 Avenue de la Poste", "Bertoua", "BP 2346", "Cameroun", counter++),
            
            createClient("Clara", "Nguema", "clara.nguema@email.com", "+237646789012",
                LocalDate.of(1996, 7, 15), "56 Boulevard du Marché", "Nkongsamba", "BP 6790", "Cameroun", counter++),
            
            createClient("Maxime", "Tchouangue", "maxime.tchouangue@email.com", "+237657890123",
                LocalDate.of(1982, 11, 22), "90 Rue de l'Hôpital", "Edéa", "BP 1236", "Cameroun", counter++)
        );

        clientRepository.saveAll(clients);
        System.out.println("15 clients camerounais de test ont été initialisés avec succès !");
    }

    private Client createClient(String prenom, String nom, String email, String telephone,
                                LocalDate dateNaissance, String adresse, String ville,
                                String codePostal, String pays, int numero) {
        Client client = new Client();
        client.setPrenom(prenom);
        client.setNom(nom);
        client.setEmail(email);
        client.setTelephone(telephone);
        client.setDateNaissance(dateNaissance);
        client.setAdresse(adresse);
        client.setVille(ville);
        client.setCodePostal(codePostal);
        client.setPays(pays);
        client.setStatut(Client.StatutClient.ACTIF);
        client.setNumeroCompte(generateNumeroCompte(prenom, nom, numero));
        client.setDateCreation(LocalDateTime.now());
        client.setDateModification(LocalDateTime.now());
        return client;
    }

    private String generateNumeroCompte(String prenom, String nom, int numero) {
        // Générer un numéro de compte unique basé sur les initiales et un numéro séquentiel
        String prefix = "WB";
        String initials = prenom.substring(0, 1).toUpperCase() + nom.substring(0, 1).toUpperCase();
        return prefix + initials + String.format("%08d", numero * 1000 + (int)(Math.random() * 999));
    }
}

