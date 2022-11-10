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
        public string Token { get; set; } = string.Empty;

        public string? refreshToken { get; set; } = string.Empty;
    }

    public class UserInfo
    {
        public string Username { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public dynamic ProfilePhoto { get; set; } = null;
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
        public string username { get; set; }= string.Empty;
        public string newpassword { get; set; } = string.Empty;
    }

    public class vercode
    {
        public string username { get; set; } = string.Empty;
        public string code { get; set; } = string.Empty;
    }

    public class addPost
    {
        public string username { get; set; } = string.Empty;
        public string description { get; set; } = string.Empty;
        public string location { get; set; } = string.Empty;
        public List<IFormFile> photos { get; set; }
    }

    public class getPosts
    {
        public string description { get; set; } = string.Empty;
        public string location { get; set; } = string.Empty;
        public DateTime DateTime { get; set; }
        public List<string> photos { get; set; }
        public int brojslika { get; set; }
    }
    
    public class Photo
    {
        public IFormFile slika { get; set; }
    }

    public class comment
    {
        public int postid { get; set; }
        public string text { get; set; }
    }
    public class comments
    {
        public int OwnerID { get; set; }
        public string text { get; set; } = string.Empty;
        public DateTime time { get; set; }
    }

    public class deletecom
    {
        public int postId { get; set; }
        public int commid { get; set; }
    }
    public class editcom
    {
        public int postId { get; set; }
        public int commid { get; set; }
        public string newtext { get; set; } = string.Empty;
    }
}
