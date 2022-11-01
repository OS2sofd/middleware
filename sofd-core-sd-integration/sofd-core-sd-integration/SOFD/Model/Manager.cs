using Newtonsoft.Json;
using System;

namespace DigitalIdentity.SOFD.Model
{
    public class Manager
    {
        [JsonProperty("uuid")]
        public string Uuid { get; set; }
        
        [JsonProperty("name")]
        public string Name { get; set; }
        
        [JsonProperty("inherited")]
        public bool Inherited { get; set; }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj.GetType() != this.GetType())
            {
                return false;
            }
            var other = (Manager)obj;
            if (other.Uuid != this.Uuid)
            {
                return false;
            }
            if (other.Name != this.Name)
            {
                return false;
            }
            if (other.Inherited != this.Inherited)
            {
                return false;
            }
            return true;
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();
        }

    }
}