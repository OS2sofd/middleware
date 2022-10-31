package dk.sofd.opus.utility;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import dk.sofd.opus.service.model.Affiliation;
import dk.sofd.opus.service.model.Person;
import dk.sofd.opus.service.model.Post;
import dk.sofd.opus.service.model.User;

public class ObjectClonerTest
{
    private ObjectCloner objectCloner = new ObjectCloner();

    @Test
    public void deepCopyEquals() {
        Person person1 = getTestPerson();
        Person person2 = objectCloner.deepCopy(person1);

        assertEquals(true, person1.equals(person2));
    }

    Person getTestPerson() {
        Person testPerson = new Person();
        testPerson.setUuid(UUID.randomUUID().toString());
        testPerson.setFirstname("Test");

        Post residentPostAddress = new Post();
        residentPostAddress.setAddressProtected(false);
        residentPostAddress.setCity("TestBy");
        testPerson.setResidencePostAddress(residentPostAddress);

        Set<User> users = new HashSet<>();
        User user1 = new User();
        user1.setUuid(UUID.randomUUID().toString());
        user1.setUserType("ACTIVE_DIRECTORY");
        users.add(user1);
        testPerson.setUsers(users);

        Set<Affiliation> affiliations = new HashSet<>();
        Affiliation affiliation = new Affiliation();
        affiliation.setUuid(UUID.randomUUID().toString());
        Map<String, String> localExtensions = new HashMap<>();
        localExtensions.put("foo","bar");
        localExtensions.put("bar","foo");
        affiliation.setLocalExtensions(localExtensions);
        affiliations.add(affiliation);
        testPerson.setAffiliations(affiliations);

        return testPerson;
    }
}