package com.femcoders.ChallengeTrackerAPI.services;

import com.femcoders.ChallengeTrackerAPI.dtos.user.UserMapperImpl;
import com.femcoders.ChallengeTrackerAPI.dtos.user.UserRequest;
import com.femcoders.ChallengeTrackerAPI.dtos.user.UserResponse;
import com.femcoders.ChallengeTrackerAPI.exceptions.EntityNotFoundException;
import com.femcoders.ChallengeTrackerAPI.models.Challenge;
import com.femcoders.ChallengeTrackerAPI.models.Role;
import com.femcoders.ChallengeTrackerAPI.models.User;
import com.femcoders.ChallengeTrackerAPI.repositories.RoleRepository;
import com.femcoders.ChallengeTrackerAPI.repositories.UserRepository;
import com.femcoders.ChallengeTrackerAPI.security.UserDetail;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapperImpl userMapperImpl;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(()-> new EntityNotFoundException(User.class.getSimpleName(), username));
        return userMapperImpl.entityToDto(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException(User.class.getSimpleName(), id));
        return userMapperImpl.entityToDto(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> userMapperImpl.entityToDto(user))
                .toList();
    }

    @Transactional
    public UserResponse addUser(UserRequest userRequest) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(userRequest.username());
        if (user.isPresent()) {
            throw new RuntimeException("User already exists with username: " + userRequest.username());
        }
        Optional<User> email = userRepository.findByEmailIgnoreCase((userRequest.email()));
        if (email.isPresent()) {
            throw new RuntimeException("Email is already registered: " + userRequest.email());
        }
        List<Challenge> initialListOfChallenges = new ArrayList<>();
        Role userRole = roleRepository.findByRoleNameIgnoreCase("ROLE_USER")
                .orElseThrow(() -> new EntityNotFoundException("Role", "USER"));
        List<Role> roles = new ArrayList<>();
        roles.add(userRole);
        User newUser = userMapperImpl.dtoToEntity(userRequest, initialListOfChallenges, roles);
        newUser.setPassword(passwordEncoder.encode(userRequest.password()));
        User savedUser = userRepository.save(newUser);
        return userMapperImpl.entityToDto(savedUser);
    }

    @Override
    public UserDetail loadUserByUsername(String username) throws EntityNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new EntityNotFoundException(User.class.getSimpleName(), username));
        return new UserDetail(user);
    }

    @Transactional
    public UserResponse registerUser(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Role defaultRole = roleRepository.findByRoleNameIgnoreCase("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found."));

        List<Role> roles = new ArrayList<>();
        roles.add(defaultRole);
        User user = userMapperImpl.dtoToEntity(request, new ArrayList<>(), roles);

        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);
        return userMapperImpl.entityToDto(user);
    }
}
