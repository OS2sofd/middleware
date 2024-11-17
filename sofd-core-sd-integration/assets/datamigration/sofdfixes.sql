
# Set stop-date for all OPUS affiliations with a future or null stopdate, but only for persons that already has an affiliation from SD
update affiliations a1 inner join affiliations a2 ON a2.person_uuid = a1.person_uuid and a2.master = 'SD-ZT' set a1.stop_date = '2020-11-17' where (a1.stop_date is null or a1.stop_date > '2020-11-17') and a1.master = 'OPUS';

# extract still active OPUS accounts for manual check
select p.firstname as Fornavn,p.surname as Efternavn,a1.master_id as MaNr, a1.start_date as StartDato, a1.stop_date as StopDato, o.name as OrgEnhed, a1.position_name as Stilling from affiliations a1 inner join persons p on p.uuid = a1.person_uuid inner join orgunits o on o.uuid = a1.orgunit_uuid where (a1.stop_date is null or a1.stop_date > '2020-11-17') and a1.master = 'OPUS' and a1.deleted = 0;
