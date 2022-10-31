package dk.sofd.opus.utility;

import com.esotericsoftware.kryo.Kryo;
import org.springframework.stereotype.Component;

@Component
public class ObjectCloner {
	private Kryo kryo = new Kryo();

	public ObjectCloner() {
		kryo.setRegistrationRequired(false);
	}

	public <T> T deepCopy(T object) {
		return kryo.copy(object);
	}
}