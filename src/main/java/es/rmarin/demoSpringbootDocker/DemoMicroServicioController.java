package es.rmarin.demoSpringbootDocker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DemoMicroServicioController {


    private final AddressService service;

    @Autowired
    public DemoMicroServicioController(AddressService service) {
        this.service = service;
    }

    @RequestMapping(value = "/microservicio")
    public String hello() throws Exception {

        String serverAddress = service.getServerAddress();
        return new StringBuilder().append("Hello from IP address: ").append(serverAddress).append("\n").toString();
    }


}
