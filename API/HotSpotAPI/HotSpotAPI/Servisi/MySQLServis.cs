using HotSpotAPI.Data;
using HotSpotAPI.Modeli;
using HotSpotAPI.ModeliZaZahteve;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
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

        public refreshTokenResponse noviRefreshToken(string Username, string refreshToken);
        public string izmeniKorisnika(int id, EditUser user, out bool ind, out bool indPromeneTokena);
        public bool checkPass(int id, string Password);
        public void CreatePasswordHash(string password, out byte[] passwordHash, out byte[] passwordSalt);
        public bool VerifyPasswordHash(string password, byte[] passwordHash, byte[] passwordSalt);
        public int dodajKod(string username);
        public string CreateToken(Korisnik korisnik, int trajanjeUMinutima);
        public JwtSecurityToken? ValidateToken(string token);
        public JwtSecurityToken? fromStringToToken(string token);
        public Task<String> validateUser(string username);
        public Task<String> newUserEmailToken(string token);
        public Task<String> newUserToken(string token);
        public string GenerateRefreshToken();
    }
    public class MySQLServis : IMySQLServis
    {
        private MySqlDbContext _context;
        private IConfiguration configuration;
        private ImailService mail;
        private readonly IStorageService storageService;

        public MySQLServis(IConfiguration configuration, MySqlDbContext context, ImailService mail, IStorageService storageService)
        {
            _context = context;
            this.configuration = configuration;
            this.mail = mail;
            this.storageService = storageService;
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
            korisnik.refreshToken = "";
            korisnik.EmailPotvrdjen = false;
            korisnik.ProfileImage = "";


            _context.Korisnici.Add(korisnik);

            await _context.SaveChangesAsync();

            int id = _context.Korisnici.Where(x => x.Username.Equals(korisnik.Username)).FirstOrDefault().ID;
            _context.TokenRegistracije.Add(new TokenRegistracije()
            {
                Token = EmailToken,
                userID = id,
            });

            await _context.SaveChangesAsync();
            return EmailToken;



        }

        public int dodajKod(string username)
        {
            Random rnd = new Random();
            int code = rnd.Next(1000, 9999);

            var user = _context.Korisnici.FirstOrDefault(x=>x.Username== username);
            if (user == null)
                return -1;
            Kod k = new Kod();
            k.UserID = user.ID;
            k.RegisterCode = code;
            _context.Kodovi.Add(k);
            _context.SaveChanges();
            return code;
        }

        public Korisnik loginKorisnika(LoginDTO zahtev) 
        {
            var korisnik = _context.Korisnici.Where(x => (x.Username.Equals(zahtev.Username)|| x.Email.Equals(zahtev.Username))).FirstOrDefault();
            if (korisnik == null)
                return null;

            if (!VerifyPasswordHash(zahtev.Password, korisnik.PasswordHash, korisnik.PasswordSalt))
            {
                return null;
            }
            korisnik.refreshToken=this.GenerateRefreshToken();
            _context.SaveChanges();
            return korisnik;
        }

        public refreshTokenResponse noviRefreshToken(string Username,string refreshToken)
        {
            var korisnik = _context.Korisnici.Where(x => (x.Username.Equals(Username))).FirstOrDefault();
            if (korisnik == null)
                return null;


            if (!korisnik.refreshToken.Equals(refreshToken))
                return null;

            var newRefToken = GenerateRefreshToken();

            korisnik.refreshToken = newRefToken;
            _context.SaveChanges();


            string token = this.CreateToken(korisnik, int.Parse(configuration.GetSection("AppSettings:TrajanjeTokenaUMinutima").Value.ToString()));


            return new refreshTokenResponse
            {
                message = "NewRefTokenAdded",
                refreshToken = newRefToken,
                Token = token
            };
        }

        public string GenerateRefreshToken()
        {
            var randomNumber = new byte[32];
            using (var rng = RandomNumberGenerator.Create())
            {
                rng.GetBytes(randomNumber);
                return Convert.ToBase64String(randomNumber);
            }
        }

        public  void CreatePasswordHash(string password, out byte[] passwordHash, out byte[] passwordSalt)
        {
            using (var hmac = new HMACSHA512())
            {
                passwordSalt = hmac.Key;
                passwordHash = hmac.ComputeHash(System.Text.Encoding.UTF8.GetBytes(password));
            }
        }


        public  bool VerifyPasswordHash(string password, byte[] passwordHash, byte[] passwordSalt)
        {
            using (var hmac = new HMACSHA512(passwordSalt))
            {
                var computedHash = hmac.ComputeHash(System.Text.Encoding.UTF8.GetBytes(password));
                return computedHash.SequenceEqual(passwordHash);
            }
        }

        public string CreateToken(Korisnik korisnik, int trajanjeUMinutima)
        {
            string uid = korisnik.ID + "";
            List<Claim> claims = new List<Claim>
            {
                new Claim("username",korisnik.Username),
                new Claim("email",korisnik.Email),
                new Claim("id", uid),
                new Claim("photo",korisnik.ProfileImage)
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

        public JwtSecurityToken? ValidateToken(string token)
        {
            if (token == null)
                return null;

            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.ASCII.GetBytes(configuration.GetSection("AppSettings:Token").Value.ToString());



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

                //var userName = (jwtToken.Claims.First(x => x.Type == "username").Value);
                return jwtToken;
            }
            catch(ArgumentException ex)
            {
                //Debug.WriteLine("Token nije validan");
                return null;
            }
            catch(SecurityTokenExpiredException ex)
            {
                throw ex;
            }

        }

        public JwtSecurityToken? fromStringToToken(string token)
        {
            if (token == null)
                return null;

            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.ASCII.GetBytes(configuration.GetSection("AppSettings:Token").Value.ToString());



            try
            {
                tokenHandler.ValidateToken(token, new TokenValidationParameters
                {
                    ValidateIssuerSigningKey = true,
                    IssuerSigningKey = new SymmetricSecurityKey(key),
                    ValidateIssuer = false,
                    ValidateAudience = false,

                    ClockSkew = TimeSpan.Zero,
                    ValidateLifetime=false
                },
                out SecurityToken validatedToken);

                var jwtToken = (JwtSecurityToken)validatedToken;

                //var userName = (jwtToken.Claims.First(x => x.Type == "username").Value);
                return jwtToken;
            }
            catch (ArgumentException ex)
            {
                //Debug.WriteLine("Token nije validan");
                return null;
            }

        }


        public bool checkPass(int id, string Password)
        {
            Korisnik korisnik = _context.Korisnici.Find(id);
            if (korisnik == null)
                return false;
            if (VerifyPasswordHash(Password, korisnik.PasswordHash, korisnik.PasswordSalt))
                return true;
            return false;
        }
        public string izmeniKorisnika(int id, EditUser user, out bool ind,out bool indPromeneTokena)
        {
            bool pom = false;
            Korisnik korisnik = _context.Korisnici.Find(id);
            Boolean indPromeneUsername = false;
            Boolean indPromeneSlike = false;
            string EmailToken=null;
            if (korisnik == null)
            {
                ind = false;
                indPromeneTokena = false;
                return "ErrorwhileSavingChanges";
            }

            if (user.Username != korisnik.Username && user.Username!=null && user.Username!="")
            {
                Korisnik k = _context.Korisnici.FirstOrDefault(x => x.Username == user.Username && x.ID != korisnik.ID);
                if (k != null)
                {
                    ind = false;
                    indPromeneTokena = false;
                    return "UsernameIsAlreadyTaken";
                }
                korisnik.Username = user.Username;
                indPromeneUsername = true;
            }
            if (user.Email != korisnik.Email && user.Email!=null && user.Email!="")
            {
                Korisnik k = _context.Korisnici.FirstOrDefault(x => x.Email == user.Email && x.ID != korisnik.ID);
                if (k != null)
                {
                    ind = false;
                    indPromeneTokena = false;
                    return "EmailIsAlreadyTaken";
                }
                korisnik.Email = user.Email;
                EmailToken = CreateToken(korisnik, int.Parse(configuration.GetSection("AppSettings:TrajanjeEmailTokenaUMinutima").Value.ToString()));
                MailData maildata = new MailData(new List<string> { user.Email }, "Izmena Email-a");

                Task<bool> sendResult = mail.SendAsync(maildata, new CancellationToken(), EmailToken);
                if (sendResult != null)
                {
                    pom = true;
                    korisnik.EmailPotvrdjen = false;
                }
                else
                {
                    ind = false;
                    indPromeneTokena = false;
                    return "ErrorwhileSavingChanges";
                }
            }

            if(!user.NewPassword.IsNullOrEmpty())
            {
                CreatePasswordHash(user.NewPassword, out byte[] passwordHash, out byte[] passwordSalt);
                if (passwordHash != null && passwordHash != korisnik.PasswordHash)
                {
                    korisnik.PasswordHash = passwordHash;
                    korisnik.PasswordSalt = passwordSalt;
                }
            }

            string path = storageService.CreatePhoto();
            if (!Directory.Exists(path))
                Directory.CreateDirectory(path);

            path = Path.Combine(path, "user" + id + ".jpg");
            if (File.Exists(path))
                System.IO.File.Delete(path);

            if (user.slika!=null)
            {
               
                korisnik.ProfileImage = path;
                indPromeneSlike = true;
                using (FileStream stream = System.IO.File.Create(path))
                {
                    user.slika.CopyTo(stream);
                    stream.Flush();
                }
            }
            else
            {
                korisnik.ProfileImage = "";
            }
            
            _context.SaveChanges();

            
            if (pom == true)
            {
                ind = true;
                indPromeneTokena = true;
                return EmailToken;
            }

            if(indPromeneUsername || indPromeneSlike)
            {
                ind = true;
                indPromeneTokena = true;
                return CreateToken(korisnik, int.Parse(configuration.GetSection("AppSettings:TrajanjeTokenaUMinutima").Value.ToString()));
            }
            ind = true;
            indPromeneTokena = false;
            return "Uspesna izmena podataka";
        }

        public async Task<String> validateUser(string username)
        {
            Korisnik korisnik = _context.Korisnici.Where(x => x.Username.Equals(username)).FirstOrDefault();
            if (korisnik == null)
            {
                
                return null;
            }
            if (korisnik.EmailPotvrdjen == true)
            {
                return "EmailAlreadyVerified";
            }
            
            korisnik.EmailPotvrdjen = true;
            _context.SaveChanges();

            return "SuccessfulEmailValidation";

        }

        public async Task<String> newUserEmailToken(string token)
        {
            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.ASCII.GetBytes(configuration.GetSection("AppSettings:Token").Value.ToString());

            string username = tokenHandler.ReadJwtToken(token).Claims.First(x=>x.Type.Equals("username")).Value;

            Korisnik korisnik = _context.Korisnici.Where(x => x.Username.Equals(username)).FirstOrDefault();
            if (korisnik == null)
            {
                return null;
            }
            string EmailToken = CreateToken(korisnik, int.Parse(configuration.GetSection("AppSettings:TrajanjeEmailTokenaUMinutima").Value.ToString()));
            
            TokenRegistracije currentToken=_context.TokenRegistracije.Where(x=>x.userID==korisnik.ID).FirstOrDefault();
            if (currentToken != null)
                _context.TokenRegistracije.Remove(currentToken);


            _context.TokenRegistracije.Add(new TokenRegistracije()
            {
                Token = EmailToken,
                userID = korisnik.ID,
            });

             _context.SaveChanges();
            return EmailToken;
            

        }

        public async Task<String> newUserToken(string token)
        {
            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.ASCII.GetBytes(configuration.GetSection("AppSettings:Token").Value.ToString());

            string username = tokenHandler.ReadJwtToken(token).Claims.First(x => x.Type.Equals("username")).Value;

            Korisnik korisnik = _context.Korisnici.Where(x => x.Username.Equals(username)).FirstOrDefault();
            if (korisnik == null)
            {
                return null;
            }
            string noviToken = CreateToken(korisnik, int.Parse(configuration.GetSection("AppSettings:TrajanjeTokenaUMinutima").Value.ToString()));

            return token;


        }


    }

}
