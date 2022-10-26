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
        public KontrolerAutentikacije(IConfiguration configuration, IMySQLServis mySQLServis)
        {
            this.configuration = configuration;
            this.mySQLServis = mySQLServis;
        }




        [HttpPost("Login")]
        public async Task<ActionResult<LoginResponse>> Login(LoginDTO zahtev)
        {

            var korisnik =mySQLServis.loginKorisnika(zahtev);
            if (korisnik == null)
            {
                Debug.WriteLine("SEX/n");
                return BadRequest(new LoginResponse
                {
                    Message = "Wrong username or password",
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

                    return Ok("Uspesna Registracija");
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







    }
}
