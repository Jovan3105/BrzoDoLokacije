namespace HotSpotAPI.ModeliZaZahteve
{
    public class refreshTokenDTO
    {
        public string? token { get; set; } = string.Empty;
        public string? refreshToken { get; set; } = string.Empty;
    }

    public class refreshTokenResponse
    {
        public string message { get; set; } = string.Empty;
        public string Token { get; set; } = string.Empty;
        public string refreshToken { get; set; } = string.Empty;
    }
}
