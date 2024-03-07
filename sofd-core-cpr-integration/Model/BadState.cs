using Newtonsoft.Json;
using System;
using System.ComponentModel.DataAnnotations;
using System.Numerics;

namespace SofdCprIntegration.Model
{
    public class BadState
    {
        public long Id { get; set; }
        [MaxLength(10)]
        public string Cpr { get; set; }

        public bool IsDead { get; set; }
        public bool Gone { get; set; }
        public bool Disenfranchised { get; set; }
        public DateTime Tts { get; set; }
        public BadState() { }
    }
}
