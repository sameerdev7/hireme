package io.hireme.hireme.user;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails { // Implementing Spring Security's UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // Singular role field
    private String role; 

    // --- UserDetails Interface Implementations ---

    /**
     * Converts the single role string (e.g., "ADMIN" or "CLIENT") 
     * into a Collection of GrantedAuthority objects required by Spring Security.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // AuthorityUtils.createAuthorityList can take a single string or an array of strings
        // and converts them into the necessary GrantedAuthority objects.
        return AuthorityUtils.createAuthorityList("ROLE_"+this.role);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * Indicates whether the user's account has expired. 
     * Returns true by default.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked. 
     * Returns true (unlocked) by default.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     * Returns true by default.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled. 
     * Returns true (enabled) by default.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}