using Newtonsoft.Json;

namespace DigitalIdentity.Utility
{
    public abstract class ChangeTrackable
    {
        private string originalState;
        private string lastChangedState;

        public void TrackChanges()
        {
            originalState = JsonConvert.SerializeObject(this);
        }

        public bool IsChanged()
        {
            lastChangedState = JsonConvert.SerializeObject(this);
            return !lastChangedState.Equals(originalState);
        }

        public string GetOriginalState()
        {
            return originalState;
        }

        public string GetLastChangedState()
        {
            return lastChangedState;
        }
    }
}
