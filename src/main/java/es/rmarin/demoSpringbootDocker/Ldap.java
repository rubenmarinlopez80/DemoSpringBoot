package es.rmarin.demoSpringbootDocker;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Ldap {
	
	private static String fichPropiedades = "wsldap.properties";
	protected static Properties propiedades = null;
	private static Logger log = LogManager.getLogger("Ldap");
	
	public Ldap() {
        log.debug("Inicializando LdapWSImpl");
        getPropiedades();
    }
	
	public String getConexionLdap(String user, String passwd) {

		log.debug("Entrando en getConexionLdap....");
		DirContext contexto = null;
		DirContext validacion = null;
		
		Hashtable entorno = new Hashtable();
		Hashtable entornoValidacion = new Hashtable();
		
		String rama = null;
		
		/** obtenemos la rama de busqueda: general, salud o educacion a partir del email***/
		String ramaBusqueda = null;
		String idUsuario = null;
		String ramaUsuario = null;
		String agr_id = null;
		String msj = null;
		
		ramaBusqueda = getRamaBusquedaUsuario(user, propiedades.getProperty("ldap.rama.principal.dga"),  msj);
		
		if (ramaBusqueda == null){
			// si el usuario es de la forma usuario@domio, pero no es formalmente correcto, devuelve un mensaje.
			return msj;
		}
		
		idUsuario = getUsuarioFromUser(user);
		
		try {
			entorno.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			entorno.put(Context.PROVIDER_URL, propiedades.getProperty("ldap.url"));
			entorno.put(Context.SECURITY_AUTHENTICATION, "none");
			entorno.put(Context.SECURITY_PRINCIPAL, ramaBusqueda);
			
			log.debug("ldap url:"+propiedades.getProperty("ldap.url"));
			
			//gestorLogs.debug("Lanzando conexion LDAP");
			contexto = new InitialDirContext(entorno);
			NamingEnumeration answer = null;
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			//String filter = "(" + propiedades.getProperty("ldap.campo.busqueda1") + "=" + idUsuario + ")";
			String filter =  devuelveFiltroUsuarioLogin(idUsuario);
			log.debug("filtro: "+filter);
			answer = contexto.search(ramaBusqueda, filter, ctls);
			if (answer != null) {
				while (answer.hasMore()) {
					SearchResult searchResult = (SearchResult) answer.next();					
					if(searchResult != null) {
						rama = searchResult.getName();
						log.debug("rama: "+rama);
						Attribute attr_agr_id = (Attribute) searchResult.getAttributes().get("dgaagriculturaid");
						if (attr_agr_id != null)
							agr_id = (String) attr_agr_id.get();
					} 
				}
			} 
			
			if(rama != null) {
				entornoValidacion.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				entornoValidacion.put(Context.PROVIDER_URL, propiedades.getProperty("ldap.url"));
				entornoValidacion.put(Context.SECURITY_AUTHENTICATION, "simple");
				entornoValidacion.put(Context.SECURITY_PRINCIPAL, rama + ", " + ramaBusqueda);
				entornoValidacion.put(Context.SECURITY_CREDENTIALS, passwd);
				validacion = new InitialDirContext(entornoValidacion);
				
				log.debug("Usuario SI existe en LDAP: RAMA - " + rama);
				log.debug("DGAagriculturaId: " + agr_id);
				
				//Devolvemos el id de ORACLE del usuario de agricultura.
				return "Ok-" + agr_id;
			} else {
				log.error("Usuario NO existe en LDAP: RAMA - " + rama);
				return "Error 1 - Usuario no existe en el LDAP";
			}
		} catch (Exception excep) {
			return (getMensajeExcepcion(excep));
		}
	}

	public String getUserDataLDAP(String user_id, String psw, String user) {
		log.debug("Entrando en getUserDataLDAP....");
		log.debug("user_id:" + user_id);
		log.debug("psw:" + psw);
		log.debug("user:" + user);
		
		
		DirContext contexto = null;
		DirContext validacion = null;
		
		Hashtable entorno = new Hashtable();
		Hashtable entornoValidacion = new Hashtable();
		
		String rama = null;
		String nombre = null;
		String apellidos = null;
		String mail = null;
		String dni = null;
		String uid_oracle = null;
		
		/** obtenemos la rama de busqueda: general, salud o educacion a partir del email***/
		String ramaBusqueda = null;
		String idUsuario = null;
		String ramaUsuario = null;
		
		String msj = null;
		
		ramaBusqueda = getRamaBusquedaUsuario(user, propiedades.getProperty("ldap.rama.principal"),  msj);
		if (ramaBusqueda == null){
			// si el usuario es de la forma usuario@domio, pero no es formalmente correcto, devuelve un mensaje.
			return msj;
		}
		
		idUsuario = getUsuarioFromUser(user);
		
		
		try {
			entorno.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			entorno.put(Context.PROVIDER_URL, propiedades.getProperty("ldap.url"));
			entorno.put(Context.SECURITY_AUTHENTICATION, "none");
			entorno.put(Context.SECURITY_PRINCIPAL, ramaBusqueda);
			
			log.debug("ldap url:"+propiedades.getProperty("ldap.url"));
			
			
			//gestorLogs.debug("Lanzando conexion LDAP");
			contexto = new InitialDirContext(entorno);
			NamingEnumeration answer = null;
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String filter = devuelveFiltroUsuario(idUsuario);
			
			//String filter = "(" + propiedades.getProperty("ldap.campo.busqueda1") + "=" + idUsuario + ")";
			log.debug("filter:"+filter);
			
			answer = contexto.search(ramaBusqueda, filter, ctls);
			log.debug("answer:"+answer.toString());
			
			if (answer != null) {
				while (answer.hasMore()) {
					SearchResult searchResult = (SearchResult) answer.next();					
					if(searchResult != null) {
						rama = searchResult.getName();
						log.debug("rama result:"+rama);
					} 
				}
			}
			log.debug("rama:"+rama);
			
			if(rama != null) {
				entornoValidacion.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				entornoValidacion.put(Context.PROVIDER_URL, propiedades.getProperty("ldap.url"));
				entornoValidacion.put(Context.SECURITY_AUTHENTICATION, "simple");
				entornoValidacion.put(Context.SECURITY_PRINCIPAL, rama + ", " + ramaBusqueda);
				entornoValidacion.put(Context.SECURITY_CREDENTIALS, psw);
				
				log.debug("entornoValidacion:"+entornoValidacion.toString());
				
				validacion = new InitialDirContext(entornoValidacion);
				
				//filter = "(" + propiedades.getProperty("ldap.campo.busqueda1") + "=" + user_id + ")";
				filter = devuelveFiltroUsuario(user_id);
				log.debug("filter1:"+filter);
				// busca el usuario del cual obtener la informacion en la rama principal dga
				answer = validacion.search(propiedades.getProperty("ldap.rama.principal.dga").toString(), filter, ctls);
				log.debug("answer:"+answer.toString());
				
				if (answer != null) {
                    if (answer.hasMore()) {
                        SearchResult searchResult = (SearchResult) answer.next();
                        log.debug("searchResult:"+searchResult.toString());
                        if(searchResult != null) {
                        	
                        	// nombre -> givenName
                        	if (searchResult.getAttributes().get("givenName") != null)
                        		nombre = searchResult.getAttributes().get("givenName").get(0).toString();
                        	// apellidos -> sn
                        	if (searchResult.getAttributes().get("sn") != null)
                        		apellidos = searchResult.getAttributes().get("sn").get(0).toString();
                        	// mail -> mail
                        	if ( searchResult.getAttributes().get("mail") != null )
                        		mail = searchResult.getAttributes().get("mail").get(0).toString();
                        	// dni -> employeeNumber
                        	if (searchResult.getAttributes().get("employeeNumber")  != null)
                        		dni = searchResult.getAttributes().get("employeeNumber").get(0).toString();
                        	// uid oracle -> DGAagriculturaId
                        	if (searchResult.getAttributes().get("DGAagriculturaId")  != null)
                        		uid_oracle = searchResult.getAttributes().get("DGAagriculturaId").get(0).toString();
                        }
                    }
                }
				//Ok-Nombre/*/Apellidos/*/telefono/*/email/*/dni/*/uid_oracle
				if(nombre == null && apellidos == null && mail == null && dni == null && uid_oracle == null) {
					log.debug("Usuario NO existe en LDAP");
					return "Error 4-Usuario a consultar no existe";
				} else {
					log.debug("Usuario SI existe en LDAP: " + nombre + " " + apellidos);
					return "Ok-" + nombre + "/*/" + apellidos + "/*/" + mail + "/*/" + dni + "/*/" + uid_oracle;
				}
			} else {
				log.error("Error 1-Usuario/contraseña genérico de LDAP no es correcto: " + user_id);
				return "Error 1-Usuario/contraseña genérico de LDAP no es correcto";
			}
		} catch (Exception excep) {
			return (getMensajeExcepcion(excep));
			//log.error("Error desconocido en la obtención de datos de un usuario LDAP",excep);
			//return "Error en la comunicación con el servicio";
		}
	}

	
	private static Properties getPropiedades(){
		try{
            if (propiedades == null){
                    java.io.InputStream is = null;
                    is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fichPropiedades);
                    propiedades = new Properties();
                    propiedades.load(is);
            }
            return propiedades;
        }catch(Exception e){
                return null;
        }
	}
	
	
	/**
	 * getMensajeExcepcion
	 * Devuelve el mensaje dependiendo de la expección LDAP
	 * Basado en http://docs.oracle.com/javase/tutorial/jndi/ldap/exceptions.html
	 * Devuelve el texto desciptivo del error a partir del tipo de excepcion.
	 * 
	 * @param Exception e
	 * @return String
	 */
	private static String getMensajeExcepcion(Exception e) {
		String mensaje = null; 
        // code 1 - Operations error -  NamingException
		if (e instanceof NamingException){
			log.error("Error NamingException en la obtención de datos de un usuario LDAP",e);
			mensaje =  "Error en la obtención de datos de un usuario LDAP";
        }

		// code 19 - A constraint violation - InvalidAttributeValueException //suele ser  [LDAP: error code 19 - Exceed password retry limit. Account locked.]
		if (e instanceof InvalidAttributeValueException ){
			log.error("Error InvalidAttributeValueException en la obtención de datos de un usuario LDAP",e);
			mensaje =  "Error 3- Cuenta genérica de LDAP bloqueada temporalmente ";
        }
		
		
		// code 49 - Invalid credentials - AuthenticationException
		if (e instanceof AuthenticationException){
			log.error("Error AuthenticationException en la obtención de datos de un usuario LDAP: ", e);
			mensaje =  "Error 2- Usuario/contraseña incorrecta";
        }
		// otro tipo de excepcion
		if (mensaje == null){
			log.error("Error desconocido en la obtención de datos de un usuario LDAP",e);
			mensaje =  "Error en la comunicación con el servicio";
		}
		
		return mensaje;
		
     }
	
	/**
	 * devuelveFiltroUsuario
	 * Devuelve el filtro correspondiente al usuario dependiendo si contiene o no @.
	 * 
	 * @param String usuario
	 * @return String
	 */
	private String devuelveFiltroUsuario(String usuario){
		String filtro = "";
		if ( usuario.contains("@") ){
			// filtro por campo mail
			filtro = "(" + propiedades.getProperty("ldap.campo.busqueda2") + "=" + usuario + ")";
		}else{
			// filtro por uid
			filtro = "(" + propiedades.getProperty("ldap.campo.busqueda1") + "=" + usuario + ")";
		}
		return filtro;
	}
	
	/**
	 * devuelveFiltroUsuarioLogin
	 * Devuelve el filtro correspondiente al usuario dependiendo si contiene o no @.
	 * 
	 * @param String usuario
	 * @return String
	 */
	private String devuelveFiltroUsuarioLogin(String usuario){
		String filtro = "";
		if ( usuario.contains("@") ){
			// filtro por campo mail
			filtro = "(" + propiedades.getProperty("ldap.campo.busqueda2") + "=" + usuario + ")";
		}else{
			// filtro por uid
			filtro = "(" + propiedades.getProperty("ldap.campo.busqueda3") + "=" + usuario + ")";
		}
		return filtro;
	}
	/**
	 * getRamaBusquedaDominio
	 * Devuelve la rama donde buscar al usuario a partir de su dominio. Las ramas estan definidas en el fichero de 
	 * propiedades. Acepta como parametro el dominio y devuelve la rama correspondiente.
	 * 
	 * @param String dominio
	 * @return String 
	 */
	private String getRamaBusquedaDominio(String dominio){
		// inicializamos la rama
		String rama = null;
		// obtenemos la rama  apartir del dominio.
		if ( dominio.equals(propiedades.getProperty("ldap.dominio.general")) ){
			rama =propiedades.getProperty("ldap.rama.principal.general");
		}else if ( dominio.equals(propiedades.getProperty("ldap.dominio.salud")) ){
			// salud.aragon.es
			rama =propiedades.getProperty("ldap.rama.principal.salud");
		} else if ( dominio.equals(propiedades.getProperty("ldap.dominio.educa")) ){
			// educa.aragon.es
			rama =propiedades.getProperty("ldap.rama.principal.educa");
		}else{
			// dominio desconocido
			log.error("No se encuentra la rama "+ dominio);
		}
		
		return rama;
	}
	
	/**
	 * 
	 * @param String user
	 * @return String
	 */
	private String getRamaBusquedaUsuario(String user, String ramaGeneral, String msj){
		// inicializamos algunas variables
		// id del usuario, en caso de ser en formato email, la parte anterior a la @
		String idUsuario 	= user.trim();
		String usuarioNombre = idUsuario;
		String usuarioDominio = null;
		
		String ramaBusqueda = ramaGeneral;
		
		if (user.contains("@")) {
			String[] tokens = user.trim().split("@");
			if (tokens.length == 2){
				
				usuarioNombre = tokens[0];
				usuarioDominio = tokens[1].toLowerCase();// dominio
				ramaBusqueda = getRamaBusquedaDominio(usuarioDominio);
				
				if (ramaBusqueda == null){
					// dominio no valido
					log.debug("No se encuentra la rama para el dominio "+ usuarioDominio  + "- usuario: "+ user);
					msj = "Error - No se encuentra la rama para el dominio "+ usuarioDominio  + "- usuario: "+ user;
				}
			}else{
				usuarioNombre 	= null;
				ramaBusqueda 	= null;
				log.debug("El usuario no tiene la forma correcta "+ user);
				msj = "El usuario no tiene la forma correcta: " + user;
			}
		} 
		log.debug("**** getRamaBusquedaUsuario ****");
		log.debug("user: " + user);
		log.debug("usuarioNombre: " + usuarioNombre);
		log.debug("usuarioDominio: " + usuarioDominio);
		log.debug("ramaBusqueda: " + ramaBusqueda);
		log.debug("*********************************");
				
		return(ramaBusqueda);
	}
	/**
	 * Deveuleve el usuario
	 * @param user
	 * @return
	 */
	private String getUsuarioFromUser(String user){
		String usuario;
		usuario = user;
		if (user.contains("@")) {
			String[] tokens = user.trim().split("@");
			if (tokens.length == 2){
				usuario = tokens[0];
			}
		}
		return usuario;
	}
}