package dk.sofd.opus.service.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(exclude = {"uuid", "parent","tags"})
public class OrgUnit
{
    // stripped down version of the payload, containing only the fields that OPUS is the master of,
    // and fields that we need to fulfill the service contract
    private String uuid;
    private String master;
    private String masterId;
    private boolean deleted;
    private String parentUuid;  // output only, comes through projection=withParentUuid
    private String parent;      // input only, should be in URI format
    private String shortname;
    private String name;
    private Long cvr;
    private Long ean;
    private Long senr;
    private Long pnr;
    private String costBearer;
    private String orgType;
    private Long orgTypeId;
    private Set<Post> postAddresses;
	private Set<Phone> phones;
    private Set<OrgUnitTag> tags;

	/* we do not currently use localExtensions, so we ignore it, so as to not overwrite when patching
	@JsonSerialize(using = LocalExtensionsSerializer.class)
	@JsonDeserialize(using = LocalExtensionsDeserializer.class)
	private String localExtensions;
	*/
}
