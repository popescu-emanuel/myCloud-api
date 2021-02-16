package fmi.unibuc.ro.mycloudapi.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
	private static final String type = "Bearer";
	private String token;
	private Long id;
	private String email;
	private List<String> roles;

	public JwtResponse(String accessToken, Long id, String email, List<String> roles) {
		this.token = accessToken;
		this.id = id;
		this.email = email;
		this.roles = roles;
	}

}
