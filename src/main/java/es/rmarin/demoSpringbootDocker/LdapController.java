package es.rmarin.demoSpringbootDocker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PropertySource("classpath:wsldap.properties")
public class LdapController {
	@Autowired
	Environment env;
	
	@Autowired
	public LdapController () {
		
	}
	
	@RequestMapping(value = "/LoginMicroservicio")
    public String Login() throws Exception {

        String ldapUrl = env.getProperty("ldap.url");
        return new StringBuilder().append("Hello from LDAP LOGIN: ").append(ldapUrl).append("\n").toString();
    }

}
