package com.urjcservice.backend.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.repositories.UserRepository;

@Service
public class RepositoryUserDetailsService implements UserDetailsService {

	
    private final UserRepository userRepository;
    
    public RepositoryUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

	@Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> roles = new ArrayList<>();
        for (String role : user.getRoles()) {
            roles.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), 
                user.getEncodedPassword(), 
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isBlocked(), // accountNonLocked (if blocked false)
                roles);
    }
	
}
