using HotSpotAPI.Modeli;
using HotSpotAPI.ModeliZaZahteve;
using HotSpotAPI.Servisi;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System.Diagnostics;

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

        [HttpPost("edituser")]
        public async Task<ActionResult<string>> EditUser([FromForm] EditUser zahtev)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();
            // bool res = userService.ChangePhoto(3, photo.slika);
            // int id = userService.GetUserId();
            // if (id == -1)
            // return Ok("korisnik sa usernameom ne postoji");
            bool pass = mySQLServis.checkPass(id, zahtev.OldPassword);
            if (pass == false)
                return BadRequest
                    (
                        new changeAccDataResponse
                        {
                            message = "wrongPassword",
                            token="",
                            Id=id

                        }
                    );
            string res = mySQLServis.izmeniKorisnika(id, zahtev, out bool ind, out bool indPromeneTokena);
            if (!ind)
            {
                return BadRequest
                    (
                        new changeAccDataResponse
                        {
                            message= res,
                            token="",
                            Id=id

                        }
                    );
            }
            if(indPromeneTokena && ind)
            {
                return Ok
                    (
                        new changeAccDataResponse
                        {
                            message = "successfullChangeAccountData",
                            token = res,
                            Id = id
                        }
                    );
            }
            if(!indPromeneTokena && ind)
            {
                return Ok
                    (
                        new changeAccDataResponse
                        {
                            message = "successfullChangeAccountData",
                            token = "",
                            Id = id
                        }
                    );
            }
            return Ok
                    (
                        new changeAccDataResponse
                        {
                            message = "successfullChangeAccountData",
                            token = "",
                            Id = id
                        }
                    );
        }
        [HttpDelete]
        public async Task<ActionResult<string>> DeleteAccount()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = userService.deleteAccount(id);
            if (res)
                return Ok();
            return BadRequest();
        }
        [HttpGet("GetPhoto")]
        public async Task<ActionResult<string>> GetUserinfo()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();
            string slika = userService.getPhoto(id);
            if (slika == "" || slika == null)
            {
                return null;
            }
            Byte[] b = System.IO.File.ReadAllBytes(slika);
            return Convert.ToBase64String(b, 0, b.Length);
        }
        
    }
}
