package fmi.unibuc.ro.mycloudapi.payload.response;

import fmi.unibuc.ro.mycloudapi.model.SizePlan;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class JwtResponse implements Serializable {
	private static final String type = "Bearer";
	private String token;
	private Long id;
	private String email;
	private List<String> roles;
	private SizePlan sizePlan;

	public JwtResponse(String accessToken, Long id, String email, List<String> roles, SizePlan sizePlan) {
		this.token = accessToken;
		this.id = id;
		this.email = email;
		this.roles = roles;
		this.sizePlan = sizePlan;
	}
}
