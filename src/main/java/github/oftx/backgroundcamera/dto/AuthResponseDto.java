package github.oftx.backgroundcamera.dto;

public class AuthResponseDto {
    private String token;
    public AuthResponseDto(String token) { this.token = token; }
    // Getter and setter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
