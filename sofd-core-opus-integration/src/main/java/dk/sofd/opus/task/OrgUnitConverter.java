package dk.sofd.opus.task;

import dk.sofd.opus.dao.model.Municipality;
import dk.sofd.opus.service.model.OrgUnit;
import dk.sofd.opus.service.model.Phone;
import dk.sofd.opus.service.model.Post;
import dk.sofd.opus.task.model.KmdOrgUnitWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class OrgUnitConverter {

	@Value("${opus.master.identifier:OPUS}")
	private String opusMasterIdentifier;

	public OrgUnit toOrgUnit(Municipality municipality, KmdOrgUnitWrapper wrapper) {
		dk.kmd.opus.OrgUnit opusOrgUnit = wrapper.getOrgUnit();
		OrgUnit orgUnit = new OrgUnit();

		orgUnit.setUuid(wrapper.getUuid());
		orgUnit.setCostBearer(opusOrgUnit.getCostCenter() != null ? Long.toString(opusOrgUnit.getCostCenter()) : null);
		orgUnit.setCvr(opusOrgUnit.getCvrNr() != null ? (long) opusOrgUnit.getCvrNr() : null);
		orgUnit.setDeleted(false);
		orgUnit.setEan(opusOrgUnit.getEanNr());
		orgUnit.setMaster(opusMasterIdentifier);
		orgUnit.setMasterId(opusOrgUnit.getId());
		orgUnit.setName(opusOrgUnit.getLongName());
		orgUnit.setParentUuid((wrapper.getParent() != null) ? wrapper.getParent().getUuid() : null);
		orgUnit.setOrgType(opusOrgUnit.getOrgTypeTxt());
		orgUnit.setOrgTypeId(opusOrgUnit.getOrgType() != null ? (long) opusOrgUnit.getOrgType() : null);
		orgUnit.setPnr(opusOrgUnit.getPNr() != null ? (long) opusOrgUnit.getPNr() : null);

		orgUnit.setPostAddresses(new HashSet<>());
		orgUnit.getPostAddresses()
				.add(Post.builder()
						.addressProtected(false)
						.city(opusOrgUnit.getCity())
						.country("Danmark")
						.postalCode(opusOrgUnit.getZipCode() != null ? Short.toString(opusOrgUnit.getZipCode()) : null)
						.prime(true)
						.master(opusMasterIdentifier)
						.masterId(opusOrgUnit.getId())
						.street(opusOrgUnit.getStreet())
						.build());

		// add all existing post addresses from other masters to prevent them from being deleted
		if( wrapper.getSofdOrgUnit() != null ) {
			orgUnit.getPostAddresses().addAll(wrapper.getSofdOrgUnit().getPostAddresses().stream().filter(pa -> !Objects.equals(pa.getMaster(), opusMasterIdentifier)).collect(Collectors.toList()));
		}

		orgUnit.setPhones(new HashSet<>());
		if (!municipality.isNoOuPhones() && !StringUtils.isEmpty(opusOrgUnit.getPhoneNumber())) {
			orgUnit.getPhones().add(Phone.builder()
							.master(opusMasterIdentifier)
							.masterId(opusOrgUnit.getId())
							.phoneNumber(opusOrgUnit.getPhoneNumber())
							.phoneType("IP")
							.visibility("VISIBLE")
							.build());
		}

		orgUnit.setSenr(opusOrgUnit.getSeNr() != null ? (long) opusOrgUnit.getSeNr() : null);
		orgUnit.setShortname(opusOrgUnit.getShortName());

		return orgUnit;
	}
}
