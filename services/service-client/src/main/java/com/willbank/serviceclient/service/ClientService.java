package com.willbank.serviceclient.service;

import com.willbank.serviceclient.dto.ClientCreateDTO;
import com.willbank.serviceclient.dto.ClientDTO;
import com.willbank.serviceclient.model.Client;
import com.willbank.serviceclient.repository.ClientRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(ClientDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + id));
        return ClientDTO.fromEntity(client);
    }

    public ClientDTO getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'email: " + email));
        return ClientDTO.fromEntity(client);
    }

    public ClientDTO getClientByNumeroCompte(String numeroCompte) {
        Client client = clientRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec le numéro de compte: " + numeroCompte));
        return ClientDTO.fromEntity(client);
    }

    @Transactional
    public ClientDTO createClient(ClientCreateDTO clientCreateDTO) {
        // Vérifier si l'email existe déjà
        if (clientRepository.existsByEmail(clientCreateDTO.getEmail())) {
            throw new RuntimeException("Un client avec cet email existe déjà: " + clientCreateDTO.getEmail());
        }

        Client client = new Client();
        client.setPrenom(clientCreateDTO.getPrenom());
        client.setNom(clientCreateDTO.getNom());
        client.setEmail(clientCreateDTO.getEmail());
        client.setTelephone(clientCreateDTO.getTelephone());
        client.setDateNaissance(clientCreateDTO.getDateNaissance());
        client.setAdresse(clientCreateDTO.getAdresse());
        client.setVille(clientCreateDTO.getVille());
        client.setCodePostal(clientCreateDTO.getCodePostal());
        client.setPays(clientCreateDTO.getPays());
        client.setStatut(Client.StatutClient.ACTIF);
        
        // Générer un numéro de compte unique
        client.setNumeroCompte(generateNumeroCompte());

        Client savedClient = clientRepository.save(client);
        return ClientDTO.fromEntity(savedClient);
    }

    @Transactional
    public ClientDTO updateClient(Long id, ClientDTO clientDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + id));

        // Vérifier si l'email change et s'il existe déjà
        if (!client.getEmail().equals(clientDTO.getEmail()) && 
            clientRepository.existsByEmail(clientDTO.getEmail())) {
            throw new RuntimeException("Un client avec cet email existe déjà: " + clientDTO.getEmail());
        }

        client.setPrenom(clientDTO.getPrenom());
        client.setNom(clientDTO.getNom());
        client.setEmail(clientDTO.getEmail());
        client.setTelephone(clientDTO.getTelephone());
        client.setDateNaissance(clientDTO.getDateNaissance());
        client.setAdresse(clientDTO.getAdresse());
        client.setVille(clientDTO.getVille());
        client.setCodePostal(clientDTO.getCodePostal());
        client.setPays(clientDTO.getPays());
        if (clientDTO.getStatut() != null) {
            client.setStatut(clientDTO.getStatut());
        }

        Client updatedClient = clientRepository.save(client);
        return ClientDTO.fromEntity(updatedClient);
    }

    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client non trouvé avec l'ID: " + id);
        }
        clientRepository.deleteById(id);
    }

    public List<ClientDTO> getClientsByStatut(Client.StatutClient statut) {
        return clientRepository.findByStatut(statut)
                .stream()
                .map(ClientDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ClientDTO> getClientsByVille(String ville) {
        return clientRepository.findByVille(ville)
                .stream()
                .map(ClientDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ClientDTO> searchClients(String searchTerm) {
        List<Client> clients = clientRepository.findByNomOrPrenomContaining(searchTerm, searchTerm);
        if (clients.isEmpty()) {
            clients = clientRepository.findByEmailContaining(searchTerm);
        }
        return clients.stream()
                .map(ClientDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientDTO updateClientStatut(Long id, Client.StatutClient statut) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + id));
        client.setStatut(statut);
        Client updatedClient = clientRepository.save(client);
        return ClientDTO.fromEntity(updatedClient);
    }

    private String generateNumeroCompte() {
        // Générer un numéro de compte unique (format: WB + timestamp + random)
        String prefix = "WB";
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        String numeroCompte = prefix + timestamp + String.format("%03d", random);
        
        // Vérifier l'unicité
        while (clientRepository.existsByNumeroCompte(numeroCompte)) {
            random = (int) (Math.random() * 1000);
            numeroCompte = prefix + timestamp + String.format("%03d", random);
        }
        
        return numeroCompte;
    }
}

