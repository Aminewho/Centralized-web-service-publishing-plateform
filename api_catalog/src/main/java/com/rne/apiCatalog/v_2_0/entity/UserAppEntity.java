package com.rne.apiCatalog.v_2_0.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_apps")
@Getter @Setter
public class UserAppEntity {
    @Id
     private String applicationId;


    // Infos Utilisateur
  

    // Infos Application WSO2 (Miroir de ce qu'on reçoit)
@Column(unique = true, nullable = false)
    private String applicationName;  
      private String consumerKey;
    private String consumerSecret;
    private String keyState;
}