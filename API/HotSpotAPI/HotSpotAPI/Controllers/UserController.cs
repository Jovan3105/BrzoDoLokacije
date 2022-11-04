using HotSpotAPI.ModeliZaZahteve;
using HotSpotAPI.Servisi;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace HotSpotAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class UserController : ControllerBase
    {
        private readonly IMySQLServis mySQLServis;
        private readonly IConfiguration configuration;
        private readonly IUserService userService;
        private readonly ImailService mail;
        private readonly IStorageService storageService;
        public UserController(IConfiguration configuration, IMySQLServis mySQLServis, IUserService userService, ImailService mail, IStorageService storageService)
        {
            this.configuration = configuration;
            this.mySQLServis = mySQLServis;
            this.userService = userService;
            this.mail = mail;
            this.storageService = storageService;
        }

        [HttpPut("{username}/edituser")]
        public async Task<ActionResult<string>> EditUser(string username, EditUser zahtev)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Ok("korisnik sa usernameom ne postoji");
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
        [HttpGet]
        public async Task<ActionResult<UserInfo>> GetUserinfo()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            UserInfo userInfo = userService.getUserInfo(id);
            return userInfo;
        }

        [HttpGet("photo")]
        public async Task<ActionResult<dynamic>> GetProfilePhoto()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();
            string slika = userService.getPhoto(id);
            if (slika == "" || slika == null)
                slika = Path.Combine("Storage", "profilna.png");

            Byte[] b = System.IO.File.ReadAllBytes(slika);
            return File(b, "image/jpeg");
        }
        [HttpPost("photo")]
        public async Task<ActionResult<string>> SetProfilePhoto(IFormFile photo)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();
            bool res = userService.ChangePhoto(id, photo);

            if (res)
                return Ok();
            return BadRequest();
        }
    }
}
