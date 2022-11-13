using HotSpotAPI.Data;
using HotSpotAPI.Modeli;
using HotSpotAPI.ModeliZaZahteve;
using HotSpotAPI.Servisi;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using System.Diagnostics;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;

namespace HotSpotAPI.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class KontrolerAutentikacije : Controller
    {
        private readonly IMySQLServis mySQLServis;
        private readonly IConfiguration configuration;
        private readonly IUserService userService;
        private readonly ImailService mail;
        public KontrolerAutentikacije(IConfiguration configuration, IMySQLServis mySQLServis, IUserService userService, ImailService mail)
        {
            this.configuration = configuration;
            this.mySQLServis = mySQLServis;
            this.userService = userService;
            this.mail = mail;
        }


        [HttpGet("download")]
        public async Task<ActionResult> Download()
        {
            return File(System.IO.File.ReadAllBytes("app.apk"), "application/octet-stream", Path.GetFileName("app.apk"));
        }

        [HttpPost("Login")]
        public async Task<ActionResult<LoginResponse>> Login(LoginDTO zahtev)
        {

            var korisnik = mySQLServis.loginKorisnika(zahtev);
            if (korisnik == null)
            {

                return BadRequest(new LoginResponse
                {
                    Message = "WrongUsernameOrPasswordError",
                    Token = null
                });
            }
            if (korisnik.EmailPotvrdjen == false)
            {
                return BadRequest(new LoginResponse
                {
                    Message = "EmailNotVerified",
                    Token = null
                });
            }

            string token = mySQLServis.CreateToken(korisnik, int.Parse(configuration.GetSection("AppSettings:TrajanjeTokenaUMinutima").Value.ToString()));
            string slika = korisnik.ProfileImage;
            if (slika == "" || slika == null)
            {
                return Ok(new LoginResponse
                {
                    Message = "SuccessfulLogin",
                    Token = token,
                    refreshToken = korisnik.refreshToken

                }) ;
            }

            Byte[] b = System.IO.File.ReadAllBytes(slika);
            Debug.WriteLine(Convert.ToBase64String(b, 0, b.Length));
            return Ok(new LoginResponse
            {
                Message = "SuccessfulLogin",
                Token = token,
                refreshToken = korisnik.refreshToken,
            }) ;


        }

        [HttpPost("signUp")]
        public async Task<ActionResult<RegistracijaDTO>> SignUp(RegistracijaDTO zahtev)
        {


            if (ModelState.IsValid)
            {
                try
                {
                    string EmailToken = await mySQLServis.registrujKorisnika(zahtev);
                    int dodajkod = mySQLServis.dodajKod(zahtev.Username);
                    //string url = configuration.GetSection("Front_Server_Config:host").Value + ":" + configuration.GetSection("Front_Server_Config:port").Value;
                    MailPotvrdeRegistracije mailsend = new MailPotvrdeRegistracije();
                    mailsend.Name = zahtev.Username;
                    mailsend.Email = zahtev.Email;
                    // mailsend.UrlZaRegistraciju = url;
                    //MailData maildata = new MailData(new List<string> { zahtev.Email }, "Potvrda registracije", mail.GetEmailTemplate("PotvrdaRegistracije", mailsend));
                    MailData maildata = new MailData(new List<string> { zahtev.Email }, "Potvrda registracije");
                    bool sendResult = await mail.SendAsync(maildata, new CancellationToken(), EmailToken);
                    if (sendResult)
                    {
                        return Ok("SuccessfulRegistration");
                    }
                    else
                    {
                        return StatusCode(StatusCodes.Status500InternalServerError, "ErrorWithSendingEmail");
                    }

                }
                catch (Exception ex)
                {

                    if (ex.InnerException.Message.Contains("IX_Korisnici_Email"))
                        return BadRequest("EmailIsAlreadyTaken");

                    if (ex.InnerException.Message.Contains("IX_Korisnici_Username"))
                        return BadRequest("UsernameIsAlreadyTaken");


                }
            }
            return BadRequest("UnsuccessfulRegistration");

        }
        [HttpPost("verifyuser")]
        public async Task<ActionResult<string>> VerifyUser(vercode ver)
        {
            bool res = userService.checkCode(ver);
            if(res)
            {
                userService.verifyUser(ver.username);
                return Ok
                    (
                        new messageresponse
                        {
                            message = "Nalog je uspesno verifikovan, mozete se ulogovati"
                        }
                    );

            }
            return BadRequest
                    (
                        new messageresponse
                        {
                            message = "Kod nije validan, proverite da li ste uneli vazeci kod"
                        }
                    );
        }
        [HttpPost("code")]
        public async Task<ActionResult<string>> CompareCode(vercode code)
        {
            Debug.WriteLine(code.code);
            if(code==null)
            {
                if (code == null)
                    return BadRequest(
                            new messageresponse
                            {
                                message = "Greska pri slanju"
                            }
                        );
            }

            string res = userService.ConfirmCode(code.username, code.code, out bool ind);
            if (ind)
            {
                return Ok(
                        new messageresponse
                        {
                            message = res
                        }
                    );
            }
            return BadRequest(
                        new messageresponse
                        {
                            message = res
                        }
                    );
        }
        [HttpPut("Setpass")]
        public async Task<ActionResult<string>> Setpass(password pass)
        {
            if (pass == null)
                return BadRequest(
                        new messageresponse
                        {
                            message = "Greska pri slanju"
                        }
                    );

            string res = userService.chengePassInDataBase(pass, out bool ind);
            if(ind)
            {
                return Ok(
                        new messageresponse
                        {
                            message = res
                        }
                    );
            }
            return BadRequest(
                        new messageresponse
                        {
                            message = res
                        }
                    );

        }
        [HttpPost("VerifyEmail/{EmailToken}")]
        public async Task<ActionResult<string>> VerifyEmail(string EmailToken)
        {

            
            if (EmailToken.IsNullOrEmpty())
            {
                return BadRequest("InvalidToken");
            }

            JwtSecurityToken token = null;
            try
            {
                token = mySQLServis.ValidateToken(EmailToken);
            }
            catch (SecurityTokenExpiredException ex)
            {
                string noviToken =(await mySQLServis.newUserEmailToken(EmailToken)).ToString();

                if (noviToken == null)
                {
                    return BadRequest("ErrorWhileCreatingNewToken");
                }
                var tokenHandler = new JwtSecurityTokenHandler();
                var key = Encoding.ASCII.GetBytes(configuration.GetSection("AppSettings:Token").Value.ToString());

                string usernameToken = tokenHandler.ReadJwtToken(EmailToken).Claims.First(x => x.Type.Equals("username")).Value;
                string emailtoken = tokenHandler.ReadJwtToken(EmailToken).Claims.First(x => x.Type.Equals("email")).Value;

                MailPotvrdeRegistracije mailsend = new MailPotvrdeRegistracije();
                mailsend.Name = usernameToken;
                mailsend.Email = emailtoken;

                MailData maildata = new MailData(new List<string> { emailtoken }, "Potvrda registracije");
                bool sendResult = await mail.SendAsync(maildata, new CancellationToken(), noviToken);

                return BadRequest("NewUserTokenCreated"); 
            }
            
            if (token == null)
                return BadRequest("InvalidToken");

            var username = (token.Claims.First(x => x.Type == "username").Value);
            
            string result =(await mySQLServis.validateUser(username)).ToString();
            return Ok(result);
        }

        [HttpPost("ResetujToken")]
        public async Task<ActionResult<refreshTokenResponse>> ResetujToken(refreshTokenDTO zahtev)
        {

            
            JwtSecurityToken token= null;
            
            token = mySQLServis.fromStringToToken(zahtev.token);
            

            if(token == null)
                return BadRequest(new refreshTokenResponse
                {
                    message = "ErrorWhileCreatingRefreshToken1",
                    Token = "2",
                    refreshToken = ""
                });

            var username = (token.Claims.First(x => x.Type == "username").Value);

            refreshTokenResponse odgovor = mySQLServis.noviRefreshToken(username,zahtev.refreshToken);


            if(odgovor == null)
                return BadRequest(new refreshTokenResponse
                {
                    message="ErrorWhileCreatingRefreshToken2",
                    Token= "",
                    refreshToken=""
                });

            return Ok(odgovor);
        }

    }
}
