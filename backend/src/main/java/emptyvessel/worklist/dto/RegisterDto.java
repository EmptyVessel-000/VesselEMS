package emptyvessel.worklist.dto;

import emptyvessel.worklist.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterDto(
        @NotBlank String username,
        @NotBlank @Email String email,
        @Pattern(regexp = "^[\\d\\-+]{0,32}$") String telephone,
        @NotBlank String roleName,
        @NotBlank String password) {

    public void apply(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Target user cannot be null");
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setTelephone(telephone);
        user.setPassword(password);
    }
}