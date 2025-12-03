package com.willbank.serviceclient.controller;

import com.willbank.serviceclient.dto.ClientCreateDTO;
import com.willbank.serviceclient.dto.ClientDTO;
import com.willbank.serviceclient.model.Client;
import com.willbank.serviceclient.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@Validated
@CrossOrigin(origins = "*")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<ClientDTO> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        try {
            ClientDTO client = clientService.getClientById(id);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClientDTO> getClientByEmail(@PathVariable String email) {
        try {
            ClientDTO client = clientService.getClientByEmail(email);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/compte/{numeroCompte}")
    public ResponseEntity<ClientDTO> getClientByNumeroCompte(@PathVariable String numeroCompte) {
        try {
            ClientDTO client = clientService.getClientByNumeroCompte(numeroCompte);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@Valid @RequestBody ClientCreateDTO clientCreateDTO) {
        try {
            ClientDTO createdClient = clientService.createClient(clientCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, 
                                                   @Valid @RequestBody ClientDTO clientDTO) {
        try {
            ClientDTO updatedClient = clientService.updateClient(id, clientDTO);
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<ClientDTO>> getClientsByStatut(@PathVariable Client.StatutClient statut) {
        List<ClientDTO> clients = clientService.getClientsByStatut(statut);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<ClientDTO>> getClientsByVille(@PathVariable String ville) {
        List<ClientDTO> clients = clientService.getClientsByVille(ville);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientDTO>> searchClients(@RequestParam String term) {
        List<ClientDTO> clients = clientService.searchClients(term);
        return ResponseEntity.ok(clients);
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<ClientDTO> updateClientStatut(@PathVariable Long id, 
                                                         @RequestParam Client.StatutClient statut) {
        try {
            ClientDTO updatedClient = clientService.updateClientStatut(id, statut);
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

