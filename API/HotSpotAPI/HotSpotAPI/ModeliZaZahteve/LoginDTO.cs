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

}
