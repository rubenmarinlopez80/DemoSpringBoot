package es.rmarin.demoSpringbootDocker;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;


@Service
public class AddressServiceImpl implements AddressService {

    public String getServerAddress() throws Exception {

        final String serverAddress = InetAddress.getLocalHost().getHostAddress();

        return serverAddress;

    }

}
