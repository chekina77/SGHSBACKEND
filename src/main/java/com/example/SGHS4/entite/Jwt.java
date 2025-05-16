package com.example.SGHS4.entite;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;


import jakarta.persistence.*;

@Entity
@Table(name = "jwt")
public class Jwt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String value;
    private boolean desactive;
    private boolean expire;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    public Jwt() {}

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public boolean isDesactive() { return desactive; }
    public void setDesactive(boolean desactive) { this.desactive = desactive; }

    public boolean isExpire() { return expire; }
    public void setExpire(boolean expire) { this.expire = expire; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public RefreshToken getRefreshToken() { return refreshToken; }
    public void setRefreshToken(RefreshToken refreshToken) { this.refreshToken = refreshToken; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String value;
        private boolean desactive;
        private boolean expire;
        private Utilisateur utilisateur;
        private RefreshToken refreshToken;

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder desactive(boolean desactive) {
            this.desactive = desactive;
            return this;
        }

        public Builder expire(boolean expire) {
            this.expire = expire;
            return this;
        }

        public Builder utilisateur(Utilisateur utilisateur) {
            this.utilisateur = utilisateur;
            return this;
        }

        public Builder refreshToken(RefreshToken refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Jwt build() {
            Jwt jwt = new Jwt();
            jwt.setValue(value);
            jwt.setDesactive(desactive);
            jwt.setExpire(expire);
            jwt.setUtilisateur(utilisateur);
            jwt.setRefreshToken(refreshToken);
            return jwt;
        }
    }
}
