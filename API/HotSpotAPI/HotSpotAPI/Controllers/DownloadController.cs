using HotSpotAPI.Servisi;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace HotSpotAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class DownloadController : ControllerBase
    {
        private readonly IMySQLServis mySQLServis;
        private readonly IConfiguration configuration;
        private readonly IUserService userService;
        private readonly ImailService mail;
        public DownloadController(IConfiguration configuration, IMySQLServis mySQLServis, IUserService userService, ImailService mail)
        {
            this.configuration = configuration;
            this.mySQLServis = mySQLServis;
            this.userService = userService;
            this.mail = mail;
        }
        [HttpGet]
        public async Task<ActionResult> download()
        {
            return File(System.IO.File.ReadAllBytes("app.apk"), "application/octet-stream", Path.GetFileName("app.apk"));
        }
    }
}
