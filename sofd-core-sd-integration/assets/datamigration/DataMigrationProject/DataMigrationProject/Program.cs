using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DataMigrationProject
{
    class Program
    {
        static void Main(string[] args)
        {
            var xmlParser = new XMLParser();
            //xmlParser.ParseOrg();
            xmlParser.FixPnr();
            
        }
    }
}
