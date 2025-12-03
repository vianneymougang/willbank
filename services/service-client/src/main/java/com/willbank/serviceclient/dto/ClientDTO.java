package com.willbank.serviceclient.dto;

import com.willbank.serviceclient.model.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClientDTO {

    private Long id;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String telephone;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotBlank(message = "Le code postal est obligatoire")
    private String codePostal;

    @NotBlank(message = "Le pays est obligatoire")
    private String pays;

    private String numeroCompte;

    private Client.StatutClient statut;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    // Constructeurs
    public ClientDTO() {
    }

    public ClientDTO(Long id, String prenom, String nom, String email, String telephone,
                     LocalDate dateNaissance, String adresse, String ville, String codePostal,
                     String pays, String numeroCompte, Client.StatutClient statut,
                     LocalDateTime dateCreation, LocalDateTime dateModification) {
        this.id = id;
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.adresse = adresse;
        this.ville = ville;
        this.codePostal = codePostal;
        this.pays = pays;
        this.numeroCompte = numeroCompte;
        this.statut = statut;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getVille() {
        return ville;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public String getPays() {
        return pays;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public Client.StatutClient getStatut() {
        return statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public void setStatut(Client.StatutClient statut) {
        this.statut = statut;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public static ClientDTO fromEntity(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setPrenom(client.getPrenom());
        dto.setNom(client.getNom());
        dto.setEmail(client.getEmail());
        dto.setTelephone(client.getTelephone());
        dto.setDateNaissance(client.getDateNaissance());
        dto.setAdresse(client.getAdresse());
        dto.setVille(client.getVille());
        dto.setCodePostal(client.getCodePostal());
        dto.setPays(client.getPays());
        dto.setNumeroCompte(client.getNumeroCompte());
        dto.setStatut(client.getStatut());
        dto.setDateCreation(client.getDateCreation());
        dto.setDateModification(client.getDateModification());
        return dto;
    }

    public Client toEntity() {
        Client client = new Client();
        client.setId(this.id);
        client.setPrenom(this.prenom);
        client.setNom(this.nom);
        client.setEmail(this.email);
        client.setTelephone(this.telephone);
        client.setDateNaissance(this.dateNaissance);
        client.setAdresse(this.adresse);
        client.setVille(this.ville);
        client.setCodePostal(this.codePostal);
        client.setPays(this.pays);
        client.setNumeroCompte(this.numeroCompte);
        if (this.statut != null) {
            client.setStatut(this.statut);
        }
        return client;
    }
}

