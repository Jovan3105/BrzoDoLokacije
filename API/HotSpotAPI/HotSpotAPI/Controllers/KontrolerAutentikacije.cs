using HotSpotAPI.Data;
using HotSpotAPI.Modeli;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Options;
using System.Diagnostics;

namespace HotSpotAPI.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class KontrolerAutentikacije : Controller
    {
        private readonly MySqlDbContext _context;

        private readonly IConfiguration configuration;
        public KontrolerAutentikacije(IConfiguration configuration, MySqlDbContext context)
        {
            this.configuration = configuration;
            _context = context;
        }




        [HttpPost("Login")]
        public async Task<ActionResult<LoginDTO>> Login(LoginDTO zahtev)
        {
            

            var korisnik = _context.Korisnici.Where(x => x.Username.Equals(zahtev.Username)).FirstOrDefault();
            if (korisnik == null)
            {

                return BadRequest(new LoginResponse
                {
                    Message = "korisnik sa datim username-om ne postoji",
                    Data=null
                });
            }

            if(korisnik.PasswordHash.Equals(zahtev.Password)==false)
               
                return BadRequest(new LoginResponse
                {
                    Message = "pogresna sifra",
                    Data = null
                });


            return Ok(new LoginResponse
            {
                Message = "Uspesan login",
                Data = zahtev
            });

        }

    }
}
