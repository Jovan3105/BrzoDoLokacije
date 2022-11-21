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

    public class getProfilePhoto
    {
        public dynamic? ProfilePhoto { get; set; } = null;
    }
    public class EditUser
    {
        
        [StringLength(maximumLength: 20, ErrorMessage = "Maksimalna duzina username-a je 20")]
        public string Username { get; set; } = string.Empty;
        
        [RegularExpression("^[a-zA-Z0-9_\\.-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", ErrorMessage = "Nije uneta validna Email adresa")]
        public string Email { get; set; } = string.Empty;
        [Required(ErrorMessage = "Nije uneta sifra")]
        public string OldPassword { get; set; } = string.Empty;
        
        public string? NewPassword { get; set; } = string.Empty;
        public IFormFile? slika { get; set; }
    }
    public class messageresponse
    {
        public string message { get; set; } = string.Empty;
    }

    public class changeAccDataResponse
    {
        public string message { get;set; } = string.Empty;
        public string token { get; set; } = string.Empty;
        public int Id { get; set; } = 0;
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
        public string username { get; set; } = string.Empty;
        public string email { get; set; } = string.Empty;
        public IFormFile slika { get; set; }
    }

    public class comment
    {
        public int parentid { get; set; }
        public int postid { get; set; }
        public string text { get; set; }
    }
    public class comments
    {
        public int OwnerID { get; set; }
        public string text { get; set; } = string.Empty;
        public dynamic userPhoto { get; set; }
        public string username { set; get; } = string.Empty;
        public DateTime time { get; set; }
        public int NumOfLikes { get; set; }
    }

    public class com
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
    public class userinfo
    {
        public string username { get; set; } = string.Empty;
        public string photo { get; set; } = string.Empty;
    }
    public class likes
    {
        public int postid { get; set; }
    }

    public class comlikes
    {
        public int postid { get; set; }
        public int commid { get; set; }
    }

    public class follower
    {
        public int ID { get; set; }
        public string username { get; set; }
        public dynamic userPhoto { get; set; }
    }
    public class getuser
    {
        //public string photo { get; set; } = string.Empty;
        public List<follower> followers { get; set; }
    }
    public class specialInfo
    {
        public int brojpostova { get; set; }
        public double prosecanbrojlajkova { get; set; }
        public int brojlokacija { get; set; }
        public int brojpratilaca { get; set; }
    }
}
