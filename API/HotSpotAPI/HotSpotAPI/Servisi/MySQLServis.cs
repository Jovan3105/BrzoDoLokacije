using HotSpotAPI.Data;
using HotSpotAPI.Modeli;
using HotSpotAPI.ModeliZaZahteve;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using System.Diagnostics;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;

namespace HotSpotAPI.Servisi
{
    public interface IMySQLServis
    {
        public Task<String> registrujKorisnika(RegistracijaDTO zahtev);
        public Korisnik loginKorisnika(LoginDTO zahtev);
        public string izmeniKorisnika(string username, EditUser user, out bool ind);
        public bool checkPass(string Username, string Password);


    }
    public class MySQLServis : IMySQLServis
    {
        private MySqlDbContext _context;
        private IConfiguration configuration;

        public MySQLServis(IConfiguration configuration, MySqlDbContext context)
        {
            this._context = context;
            this.configuration = configuration;
        }

        public async Task<String> registrujKorisnika(RegistracijaDTO zahtev)
        {
            Korisnik korisnik;
            CreatePasswordHash(zahtev.Password, out byte[] passwordHash, out byte[] passwordSalt);
            korisnik = new Korisnik();
            korisnik.Username = zahtev.Username;
            korisnik.Email = zahtev.Email;
            korisnik.PasswordHash = passwordHash;
            korisnik.PasswordSalt = passwordSalt;
            string EmailToken = CreateToken(korisnik, int.Parse(configuration.GetSection("AppSettings:TrajanjeEmailTokenaUMinutima").Value.ToString()));
            korisnik.EmailToken = EmailToken;
            korisnik.EmailPotvrdjen = false;
            korisnik.ProfileImage = "";


            _context.Korisnici.Add(korisnik);
            await _context.SaveChangesAsync();

            return "UspesnaRegistracija";

        }

        public Korisnik loginKorisnika(LoginDTO zahtev) 
        {
            var korisnik = _context.Korisnici.Where(x => x.Username.Equals(zahtev.Username) || x.Email.Equals(zahtev.Username)).FirstOrDefault();
            if (korisnik == null)
                return null;

            if (!VerifyPasswordHash(zahtev.Password, korisnik.PasswordHash, korisnik.PasswordSalt))
            {
                return null;
            }

            return korisnik;
        }



        private void CreatePasswordHash(string password, out byte[] passwordHash, out byte[] passwordSalt)
        {
            using (var hmac = new HMACSHA512())
            {
                passwordSalt = hmac.Key;
                passwordHash = hmac.ComputeHash(System.Text.Encoding.UTF8.GetBytes(password));
            }
        }


        private bool VerifyPasswordHash(string password, byte[] passwordHash, byte[] passwordSalt)
        {
            using (var hmac = new HMACSHA512(passwordSalt))
            {
                var computedHash = hmac.ComputeHash(System.Text.Encoding.UTF8.GetBytes(password));
                return computedHash.SequenceEqual(passwordHash);
            }
        }

        private string CreateToken(Korisnik korisnik, int trajanjeUMinutima)
        {
            List<Claim> claims = new List<Claim>
            {
                new Claim("username",korisnik.Username),
                new Claim("email",korisnik.Email)
            };

            var key = new SymmetricSecurityKey(System.Text.Encoding.UTF8.GetBytes(
                configuration.GetSection("AppSettings:Token").Value));

            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256Signature);
            var token = new JwtSecurityToken(
                claims: claims,
                expires: DateTime.Now.AddMinutes(trajanjeUMinutima),
                signingCredentials: creds
                );

            var jwt = new JwtSecurityTokenHandler().WriteToken(token);
            return jwt;
        }

        public static string? ValidateToken(string token, IConfiguration konfiguracija)
        {
            if (token == null)
                return null;

            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.ASCII.GetBytes(konfiguracija.GetSection("AppSettings:Token").Value.ToString());



            try
            {
                tokenHandler.ValidateToken(token, new TokenValidationParameters
                {
                    ValidateIssuerSigningKey = true,
                    IssuerSigningKey = new SymmetricSecurityKey(key),
                    ValidateIssuer = false,
                    ValidateAudience = false,

                    ClockSkew = TimeSpan.Zero
                },
                out SecurityToken validatedToken);

                var jwtToken = (JwtSecurityToken)validatedToken;

                var userName = (jwtToken.Claims.First(x => x.Type == "username").Value);

                return userName.ToString();
            }
            catch
            {
                return null;
            }

        }
        public bool checkPass(string Username, string Password)
        {
            Korisnik korisnik = _context.Korisnici.Where(x => x.Username == Username).FirstOrDefault();
            if (korisnik == null)
                return false;
            if (VerifyPasswordHash(Password, korisnik.PasswordHash, korisnik.PasswordSalt))
                return true;
            return false;
        }
        public string izmeniKorisnika(string username, EditUser user, out bool ind)
        {
            Korisnik korisnik = _context.Korisnici.Where(x=>x.Username == username).FirstOrDefault();
            if (korisnik == null)
            {
                ind = false;
                return "";
            }

            if (user.Username != korisnik.Username)
            {
                Korisnik k = _context.Korisnici.FirstOrDefault(x => x.Username == user.Username && x.ID != korisnik.ID);
                if (k != null)
                {
                    ind = false;
                    return "vec postoji korisink sa ovim username-om";
                }
                korisnik.Username = user.Username;
            }
            if (user.Email != korisnik.Email)
            {
                Korisnik k = _context.Korisnici.FirstOrDefault(x => x.Email == user.Email && x.ID != korisnik.ID);
                if (k != null)
                {
                    ind = false;
                    return "vec postoji korisink sa ovim email-om";
                }
                korisnik.Email = user.Email;
                /*poslati mail za verifikaciju*/
            }

            CreatePasswordHash(user.NewPassword, out byte[] passwordHash, out byte[] passwordSalt);
            if (passwordHash != null && passwordHash != korisnik.PasswordHash)
            {
                korisnik.PasswordHash = passwordHash;
                korisnik.PasswordSalt = passwordSalt;
            }

            _context.SaveChanges();
            ind = true;
            return "uspesna izmena";
        }
    }

}
