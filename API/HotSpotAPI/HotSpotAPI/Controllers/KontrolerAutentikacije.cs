using HotSpotAPI.Data;
using HotSpotAPI.Modeli;
using HotSpotAPI.ModeliZaZahteve;
using HotSpotAPI.Servisi;
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




        [HttpPost("Login")]
        public async Task<ActionResult<LoginResponse>> Login(LoginDTO zahtev)
        {

            var korisnik = mySQLServis.loginKorisnika(zahtev);
            if (korisnik == null)
            {

                return BadRequest(new LoginResponse
                {
                    Message = "WrongUsernameOrPasswordError",
                    Data = null
                });
            }
            if (korisnik.EmailPotvrdjen == false)
            {
                return BadRequest(new LoginResponse
                {
                    Message = "EmailNotVerified",
                    Data = null
                });
            }

            return Ok(new LoginResponse
            {
                Message = "SuccessfulLogin",
                Data = zahtev
            });


        }

        [HttpPost("signUp")]
        public async Task<ActionResult<RegistracijaDTO>> SignUp(RegistracijaDTO zahtev)
        {


            if (ModelState.IsValid)
            {
                try
                {
                    object value = await mySQLServis.registrujKorisnika(zahtev);
                    int dodajkod = mySQLServis.dodajKod(zahtev.Username);
                    //string url = configuration.GetSection("Front_Server_Config:host").Value + ":" + configuration.GetSection("Front_Server_Config:port").Value;
                    MailPotvrdeRegistracije mailsend = new MailPotvrdeRegistracije();
                    mailsend.Name = zahtev.Username;
                    mailsend.Email = zahtev.Email;
                    // mailsend.UrlZaRegistraciju = url;
                    //MailData maildata = new MailData(new List<string> { zahtev.Email }, "Potvrda registracije", mail.GetEmailTemplate("PotvrdaRegistracije", mailsend));
                    MailData maildata = new MailData(new List<string> { zahtev.Email }, "Potvrda registracije");
                    bool sendResult = await mail.SendAsync(maildata, new CancellationToken(), dodajkod);
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
        [HttpPut("{username}/edituser")]
        public async Task<ActionResult<string>> EditUser(string username, EditUser zahtev)
        {
            /*int id = userService.GetUserId();
            if (id == -1)
                return Ok("korisnik sa usernameom ne postoji");*/
            bool pass = mySQLServis.checkPass(username, zahtev.OldPassword);
            if (pass == false)
                return BadRequest
                    (
                        new messageresponse
                        {
                            message = "pogresan password"
                        }
                    );
            string res = mySQLServis.izmeniKorisnika(username, zahtev, out bool ind);
            if (!ind)
            {
                return BadRequest
                    (
                        new messageresponse
                        {
                            message = res
                        }
                    );
            }
            return Ok
                    (
                        new messageresponse
                        {
                            message = res
                        }
                    );
        }
        /*[HttpPost("{username}/changepass")]
        public async Task<ActionResult<string>> ChangePass(string username)
        {
            string res = userService.ChangePassword(username, out bool ind);
            if (ind)
                return Ok(
                            new messageresponse
                            {
                                message = res
                            }
                        );

            return BadRequest(
                        new messageresponse
                        {
                            message = res
                        }
                    );
        }*/

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
    }
}
