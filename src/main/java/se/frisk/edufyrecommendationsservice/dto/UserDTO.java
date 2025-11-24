package se.frisk.edufyrecommendationsservice.dto;

/**
 * Den här klassen representerar en användare så som vi får den från Users-tjänsten.
 *
 * Viktigt:
 * - Fälten här måste matcha JSON-svaret från Users-API:t (samman namn + typer).
 * - Vi använder den både i UserClient och i authentication-delen senare.
 */
public class UserDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private String role;

    public UserDTO() {
    }

    public UserDTO(Long id,
                   String username,
                   String firstName,
                   String lastName,
                   boolean active,
                   String role) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "edufy_ADMIN".equalsIgnoreCase(role);
    }
}
