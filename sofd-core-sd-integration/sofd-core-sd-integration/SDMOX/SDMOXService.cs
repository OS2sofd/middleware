using DigitalIdentity.SDMOX.Model;
using Microsoft.Extensions.Logging;
using RabbitMQ.Client;
using sofd_core_sd_integration.Database;
using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using Microsoft.Extensions.DependencyInjection;
using sofd_core_sd_integration.Database.Model;

namespace DigitalIdentity.SDMOX
{
    public class SDMOXService : BaseClass<SDMOXService>
    {
        private readonly DatabaseContext databaseContext;
        private readonly ConnectionFactory connectionFactory;
        public SDMOXService(IServiceProvider sp) : base(sp)
        {
            databaseContext = sp.GetService<DatabaseContext>();
            connectionFactory = new ConnectionFactory()
            {
                HostName = appSettings.SDMOXSettings.HostName,
                Port = appSettings.SDMOXSettings.Port,
                VirtualHost = appSettings.SDMOXSettings.VirtualHost,
                UserName = appSettings.SDMOXSettings.UserName,
                Password = appSettings.SDMOXSettings.Password
            };
        }

        private void SendToMq(RegistreringBeskedType message)
        {
            var topic = "org-struktur-changes-topic";
            var xml = message.ToXml();
            var body = Encoding.UTF8.GetBytes(xml);
            var messageId = Guid.NewGuid().ToString();
            var mqLog = new MQLog()
            {
                MessageId = messageId,
                Operation = message.Registrering.LivscyklusKode.ToString(),
                Message = xml,
                Timestamp = DateTime.Now,
                OrgUnitUuid = message.ObjektID.Item
            };
            databaseContext.Add(mqLog);
            databaseContext.SaveChanges();

            if (appSettings.DryRun)
            {
                logger.LogInformation("Dry run - not sending actual MQ message");
                return;
            }
            using var connection = connectionFactory.CreateConnection();
            using var channel = connection.CreateModel();
            var basicProperties = channel.CreateBasicProperties();
            basicProperties.MessageId = messageId;
            channel.BasicPublish(exchange: topic,
                                 routingKey: topic,
                                 basicProperties: basicProperties,
                                 body: body);
            logger.LogDebug($"Sent MessageId {mqLog.MessageId}, OrgUnitUuid: {mqLog.OrgUnitUuid}, Operation: {mqLog.Operation}");
            mqLog.IsSent = true;
            databaseContext.SaveChanges();
        }


        private RegistreringBeskedType GetRegistreringBeskedType(string uuid)
        {
            var obj = new RegistreringBeskedType();
            obj.ObjektID = new UnikIdType();
            obj.ObjektID.IdentifikatorType = "OrganisationEnhed";
            obj.ObjektID.ItemElementName = ItemChoiceType.UUIDIdentifikator;
            obj.ObjektID.Item = uuid;

            // Registrering
            obj.Registrering = new RegistreringType1();
            obj.Registrering.FraTidspunkt = new TidspunktType() { Item = DateTime.Now }; // SD doesn't use this
            obj.Registrering.BrugerRef = new UnikIdType() // SD doesn't use this
            {
                IdentifikatorType = "sofd-core-sd-integration",
                ItemElementName = ItemChoiceType.UUIDIdentifikator,
                Item = "b83d323f-a508-4838-b29b-1be82e90c0b2"
            };

            return obj;
        }


        private VirkningType GetVirkningMonth()
        {
            var virkning = new VirkningType();
            virkning.FraTidspunkt = new TidspunktType() { Item = new DateTime(DateTime.Now.Year, DateTime.Now.Month, 1) };
            virkning.TilTidspunkt = new TidspunktType() { Item = DateTime.MaxValue };
            return virkning;
        }

        private VirkningType GetVirkningYear()
        {
            var virkning = new VirkningType();
            virkning.FraTidspunkt = new TidspunktType() { Item = new DateTime(DateTime.Now.Year, 1, 1) };
            virkning.TilTidspunkt = new TidspunktType() { Item = DateTime.MaxValue };
            return virkning;
        }

        private void SetParent(ref RegistreringBeskedType obj, string parentUuid, VirkningType virkning)
        {
            // Relationliste
            obj.Registrering.RelationListe = new RelationListeType();
            var overordnet = new OrganisationEnhedRelationType();
            overordnet.ReferenceID = new UnikIdType() { ItemElementName = ItemChoiceType.UUIDIdentifikator, Item = parentUuid ?? appSettings.SDMOXSettings.InstitutionUUID };
            overordnet.Virkning = virkning;
            obj.Registrering.RelationListe.Overordnet = new OrganisationEnhedRelationType[] { overordnet };
        }


        public void Flyt(SDMoxOrgDto orgDto)
        {
            var obj = GetRegistreringBeskedType(orgDto.Uuid);
            obj.Registrering.LivscyklusKode = LivscyklusKodeType.Flyttet;
            SetParent(ref obj, orgDto.ParentUuid, GetVirkningMonth());
            SendToMq(obj);
        }

        public void Ret(SDMoxOrgDto orgDto, VirkningType virkning = null)
        {
            virkning ??= GetVirkningMonth();
            var obj = GetRegistreringBeskedType(orgDto.Uuid);
            obj.Registrering.LivscyklusKode = LivscyklusKodeType.Rettet;

            // Tilstandliste
            obj.Registrering.TilstandListe = new TilstandListeType();
            var gyldighed = new GyldighedType();
            gyldighed.GyldighedStatusKode = GyldighedStatusKodeType.Aktiv; // SD doesn't use this
            gyldighed.Virkning = virkning;
            obj.Registrering.TilstandListe.Gyldighed = new GyldighedType[] { gyldighed };

            // Attributliste
            obj.Registrering.AttributListe = new AttributListeType();
            var egenskab = new EgenskabType();
            egenskab.EnhedNavn = orgDto.Name;
            egenskab.Virkning = virkning;
            obj.Registrering.AttributListe.Egenskab = new EgenskabType[] { egenskab };

            // Attributliste - LokalUdvidelse”
            // NB: SD can't actually change this attribute once created :-/
            var attributUdvidelser = new List<XmlElement>();
            var niveau = new IntegrationType();
            niveau.AttributNavn = "Niveau";
            niveau.AttributVaerdi = orgDto.Level;
            niveau.Virkning = virkning;
            attributUdvidelser.Add(niveau.ToXmlElement());
            obj.Registrering.AttributListe.LokalUdvidelse = new LokalUdvidelseType() { Any = attributUdvidelser.ToArray() };


            // Relationliste (parent relation is not set in "ret" operation)
            obj.Registrering.RelationListe = new RelationListeType();
            var relationUdvidelser = new List<XmlElement>();
            var lokation = new LokationType();
            lokation.ProduktionEnhed = new ProduktionEnhedType() { ProduktionEnhedIdentifikator = orgDto.Pnr, Virkning = virkning };
            lokation.Kontakt = new KontaktType() { LokalTelefonnummerIdentifikator = orgDto.Phone, Virkning = virkning };
            lokation.DanskAdresse = new DanskAdresseType()
            {
                AdresseNavn = orgDto.Address,
                PostKodeIdentifikator = orgDto.PostalCode,
                ByNavn = orgDto.City,
                Virkning = virkning
            };
            relationUdvidelser.Add(lokation.ToXmlElement());
            obj.Registrering.RelationListe.LokalUdvidelse = new LokalUdvidelseType { Any = relationUdvidelser.ToArray() };

            SendToMq(obj);
        }


        public void Import(SDMoxOrgDto orgDto)
        {
            var virkning = appSettings.StartVirkningJanuary ? GetVirkningYear() : GetVirkningMonth();

            var obj = GetRegistreringBeskedType(orgDto.Uuid);
            obj.Registrering.LivscyklusKode = LivscyklusKodeType.Opstaaet;


            // Tilstandliste
            obj.Registrering.TilstandListe = new TilstandListeType();
            var gyldighed = new GyldighedType();
            gyldighed.GyldighedStatusKode = GyldighedStatusKodeType.Aktiv; // SD doesn't use this
            gyldighed.Virkning = virkning;
            obj.Registrering.TilstandListe.Gyldighed = new GyldighedType[] { gyldighed };

            // Attributliste
            obj.Registrering.AttributListe = new AttributListeType();
            var egenskab = new EgenskabType();
            egenskab.EnhedNavn = orgDto.Name;
            egenskab.Virkning = virkning;
            obj.Registrering.AttributListe.Egenskab = new EgenskabType[] { egenskab };

            // Attributliste - LokalUdvidelse”
            var lokalUdvidelser = new List<XmlElement>();
            var niveau = new IntegrationType();
            niveau.AttributNavn = "Niveau";
            niveau.AttributVaerdi = orgDto.Level;
            niveau.Virkning = virkning;
            lokalUdvidelser.Add(niveau.ToXmlElement());

            if (orgDto.Code != null){
                var code = new IntegrationType();
                code.AttributNavn = "EnhedKode";
                code.AttributVaerdi = orgDto.Code;
                code.Virkning = virkning;
                lokalUdvidelser.Add(code.ToXmlElement());
            }

            obj.Registrering.AttributListe.LokalUdvidelse = new LokalUdvidelseType() { Any = lokalUdvidelser.ToArray() };

            SetParent(ref obj, orgDto.ParentUuid, virkning);

            SendToMq(obj);
            Ret(orgDto, virkning);
        }
    }
}
