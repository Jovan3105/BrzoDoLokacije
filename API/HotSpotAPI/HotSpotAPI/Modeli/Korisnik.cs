using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;

namespace HotSpotAPI.Modeli
{
    [Index(nameof(Email), IsUnique = true)]
    [Index(nameof(Username), IsUnique = true)]

    public class Korisnik
    {
        public int ID { get; set; }


        [StringLength(maximumLength: 20)]
        public string Username { get; set; } = string.Empty;

        [StringLength(maximumLength: 50)]
        public string Email { get; set; } = string.Empty;
        public byte[] PasswordHash { get; set; }

        public byte[] PasswordSalt { get; set; }

        public bool EmailPotvrdjen { get; set; } = false;
        public string EmailToken { get; set; }
        public string ProfileImage { get; set; }
    }


}
