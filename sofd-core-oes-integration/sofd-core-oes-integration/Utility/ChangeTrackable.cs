using Newtonsoft.Json;

namespace DigitalIdentity.Utility
{
    public abstract class ChangeTrackable
    {
        private string jsonState;

        public void TrackChanges()
        {
            jsonState = JsonConvert.SerializeObject(this);
        }

        public bool IsChanged()
        {
            return !JsonConvert.SerializeObject(this).Equals(jsonState);
        }
    }
}
