using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;

namespace HotSpotAPI.Modeli
{
    [Index(nameof(Email), IsUnique = true)]
    [Index(nameof(Username), IsUnique = true)]

    public class Korisnik
    {
        public int ID { get; set; }

        [Required(ErrorMessage = "Insert your username")]
        [StringLength(maximumLength: 20)]
        [MinLength(4, ErrorMessage = "Username must contain at least four characters")]
        public string Username { get; set; } = string.Empty;

        [RegularExpression("^[a-zA-Z0-9_\\.-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", ErrorMessage = "Invalid email")]
        [StringLength(maximumLength: 50, ErrorMessage = "Email can not contain more than fifty characters")]
        public string Email { get; set; } = string.Empty;
        public byte[] PasswordHash { get; set; }

        public byte[] PasswordSalt { get; set; }

        public bool EmailPotvrdjen { get; set; } = false;
        public string refreshToken { get; set; }
        public string ProfileImage { get; set; } = "";
    }


}
