package com.example.ex1.services;

import com.example.ex1.entities.User;
import com.example.ex1.model.CustomUserDetails;
import com.example.ex1.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User u = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Problem during authentication!"));

        return new CustomUserDetails(u);
    }
}
