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

            var korisnik =mySQLServis.loginKorisnika(zahtev);
            if (korisnik == null)
            {
                
                return BadRequest(new LoginResponse
                {
                    Message = "WrongUsernameOrPasswordError",
                    Data = null
                });
            }


            return Ok(new LoginResponse
            {
                Message = "Uspesan login",
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
                    //string url = configuration.GetSection("Front_Server_Config:host").Value + ":" + configuration.GetSection("Front_Server_Config:port").Value;
                    MailPotvrdeRegistracije mailsend = new MailPotvrdeRegistracije();
                    mailsend.Name = zahtev.Username;
                    mailsend.Email = zahtev.Email;
                    // mailsend.UrlZaRegistraciju = url;
                    //MailData maildata = new MailData(new List<string> { zahtev.Email }, "Potvrda registracije", mail.GetEmailTemplate("PotvrdaRegistracije", mailsend));
                    MailData maildata = new MailData(new List<string> { zahtev.Email }, "Potvrda registracije");
                    bool sendResult = await mail.SendAsync(maildata, new CancellationToken());
                    if (sendResult)
                    {
                        return Ok(new
                        {
                            success = true,
                            data = new
                            {
                                message = "Uspešna registracija"
                            }
                        });
                    }
                    else
                    {
                        return StatusCode(StatusCodes.Status500InternalServerError, "Došlo je do greške prilikom slanja email-a.");
                    }

                }
                catch (Exception ex)
                {

                    if (ex.InnerException.Message.Contains("IX_Korisnici_Email"))
                        return BadRequest("Email je vec povezan sa drugim nalogom");

                    if (ex.InnerException.Message.Contains("IX_Korisnici_Username"))
                        return BadRequest("Vec postoji korisnik sa datim username-om");


                }
            }
            return BadRequest("Neuspesna registracija");

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
                            message="pogresan password"
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
        [HttpPost("{username}/changepass")]
        public async Task<ActionResult<string>> ChangePass(string username)
        {
            bool indl;
            string res = userService.ChangePassword(username, out bool ind);
            if(ind)
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
        }
    }
}
