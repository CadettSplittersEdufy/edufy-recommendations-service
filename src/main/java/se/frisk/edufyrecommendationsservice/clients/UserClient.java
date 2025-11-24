package se.frisk.edufyrecommendationsservice.clients;

import se.frisk.edufyrecommendationsservice.dto.UserDTO;

public interface UserClient {

    UserDTO getUserById(Long userId);

    boolean userIsActive(Long userId);
}