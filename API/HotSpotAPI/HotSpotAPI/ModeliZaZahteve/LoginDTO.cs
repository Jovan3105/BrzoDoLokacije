using System.ComponentModel.DataAnnotations;

namespace HotSpotAPI.ModeliZaZahteve
{
    public class LoginDTO
    {
        public string Username { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;

    }
    public class LoginResponse
    {
        public string Message { get; set; } = string.Empty;
        public LoginDTO Data { get; set; } = null;
    }

    public class EditUser
    {
        [Required(ErrorMessage = "Nije unet username")]
        [StringLength(maximumLength: 20, ErrorMessage = "Maksimalna duzina username-a je 20")]
        public string Username { get; set; } = string.Empty;
        [RegularExpression("^[a-zA-Z0-9_\\.-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", ErrorMessage = "Nije uneta validna Email adresa")]
        public string Email { get; set; } = string.Empty;
        public string OldPassword { get; set; } = string.Empty;
        [Required(ErrorMessage = "Nije uneta sifra")]
        public string NewPassword { get; set; } = string.Empty;
    }
    public class messageresponse
    {
        public string message { get; set; } = string.Empty;
    }

    public class password
    {
        public string newpassword { get; set; } = string.Empty;
    }

    public class vercode
    {
        public string username { get; set; } = string.Empty;
        public string code { get; set; } = string.Empty;
    }
}
