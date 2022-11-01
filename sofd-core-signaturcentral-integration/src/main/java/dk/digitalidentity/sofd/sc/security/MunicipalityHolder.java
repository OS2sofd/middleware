package dk.digitalidentity.sofd.sc.security;

import dk.digitalidentity.sofd.sc.dao.model.Municipality;

public class MunicipalityHolder {
	private static ThreadLocal<Municipality> municipality = new ThreadLocal<>();
	
	public static Municipality get() {
		return municipality.get();
	}
	
	public static void set(Municipality m) {
		municipality.set(m);
	}
	
	public static void clear() {
		municipality.remove();
	}
}
