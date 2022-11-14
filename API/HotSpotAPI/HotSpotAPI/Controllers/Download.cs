using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace HotSpotAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class Download : ControllerBase
    {
        [HttpGet("{code}")]
        public async Task<ActionResult> download(int code)
        {
            if (code != 1389)
                return BadRequest();

            return File(System.IO.File.ReadAllBytes("app.apk"), "application/octet-stream", Path.GetFileName("app.apk"));
        }
    }
}
