SELECT 
CONCAT("UPDATE orgunits SET parent_uuid='",o2.sofduuid,"' WHERE uuid='",o1.SofdUuid,"';") as updates
FROM sdintegration.dborgunits o1
INNER JOIN sdintegration.dborgunits o2 ON o2.VirtualUuid = o1.ParentVirtualUuid;