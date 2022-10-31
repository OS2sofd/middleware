package dk.sofd.organization.ad.utility;

import com.esotericsoftware.kryo.Kryo;

public class ObjectCloner {
    private Kryo kryo = new Kryo();

    public ObjectCloner() {
        kryo.setRegistrationRequired(false);
    }

    public <T> T deepCopy(T object) {
        return kryo.copy(object);
    }
}
