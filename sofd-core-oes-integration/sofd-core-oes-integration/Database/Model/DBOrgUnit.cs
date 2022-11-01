using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Text;

namespace sofd_core_oes_integration.Database.Model
{
    public class DBOrgUnit
    {
        [Key]
        public int Id { get; set; }

        // SOFD uuid key
        public string SOFDUuid { get; set; }

        // OES Adm org
        public string KTOPDEL{ get; set; }
        public string TXTSTRUK { get; set; }
        public string TXTLIN { get; set; }
        public string FBENAAR { get; set; }
        public string SBENAAR { get; set; }

        // Insitution
        public string INSTNR { get; set; }      // CHAR	10 Tal Institutionsnr – entydig nøgle – skal udfyldes.
        public string CVR { get; set; }         // CHAR 10 8 ciffer Modulus 11 – skal udfyldes ved opret. Institutionens CVR nr.
        public string TEKST1 { get; set; }      // CHAR	35 Tal/bogstaver Institutionsnavn – skal udfyldes ved opret
        public string TEKST2 { get; set; }      // CHAR	35 Tal/bogstaver Evt. supplerende tekst
        public string ADR1 { get; set; }        // CHAR 35 Tal/bogstaver Gade og husnr
        public string ADR2 { get; set; }        // CHAR 35 Tal/bogstaver Evt. sted
        public string PNR { get; set; }         // CHAR 4 Tal Postnr. Ved indlæsning tjekkes i Postnr.Tabel om det angivne nr.eksisterer.
        public string TLF { get; set; }         // CHAR 8 Tal
        public string INSTEMAIL { get; set; }   // CHAR 40 Valid mailadresse.
    }
}