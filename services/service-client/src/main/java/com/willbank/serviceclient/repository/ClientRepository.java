package com.willbank.serviceclient.repository;

import com.willbank.serviceclient.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    Optional<Client> findByNumeroCompte(String numeroCompte);

    List<Client> findByStatut(Client.StatutClient statut);

    List<Client> findByVille(String ville);

    @Query("SELECT c FROM Client c WHERE c.nom LIKE %:nom% OR c.prenom LIKE %:prenom%")
    List<Client> findByNomOrPrenomContaining(@Param("nom") String nom, @Param("prenom") String prenom);

    @Query("SELECT c FROM Client c WHERE c.email LIKE %:email%")
    List<Client> findByEmailContaining(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByNumeroCompte(String numeroCompte);
}

