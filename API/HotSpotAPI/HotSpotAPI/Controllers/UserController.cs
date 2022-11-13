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
        public async Task<ActionResult<string>> EditUser([FromForm]EditUser zahtev)
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

        [HttpPost("photo")]
        public async Task<ActionResult<string>> SetProfilePhoto([FromForm]Photo photo)
        {
            //int id = userService.GetUserId();
            //if (id == -1)
               // return Unauthorized();
            bool res = userService.ChangePhoto(3, photo.slika);



            if (res)
                return Ok();
            return BadRequest();
        }
        [HttpPost("addpost")]
        public async Task<ActionResult<string>> AddPost([FromForm]addPost newPost)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = userService.addNewPost(id, newPost);
            if (res)
                return Ok();
            return BadRequest();
        }

        [HttpGet("getposts")]
        public async Task<ActionResult<string>> GetPosts()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<getPosts> res = userService.getAllPosts(id);
            if (res !=null)
                return Ok(res);
            return BadRequest();
        }

        [HttpGet("getpost")]
        public async Task<ActionResult<string>> GetPosts(int postID)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            getPosts res = userService.getPost(id, postID);
            if (res != null)
                return Ok(res);
            return BadRequest();
        }
        [HttpGet("getpostbylocation")]
        public async Task<ActionResult<string>> GetPostsByLocation(string location)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<getPosts> res = userService.getAllPostsByLocaton(location);
            if (res != null)
                return Ok(res);
            return BadRequest();
        }
        [HttpDelete("deletepost")]
        public async Task<ActionResult<string>> DeletePost(int postID)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();
            bool res = userService.deletePost(id, postID);
            if (res)
                return Ok();
            return BadRequest();
        }
        [HttpPost("KreirajPost")]
        public async Task<ActionResult<string>> KreirajPost()
        {
            Debug.WriteLine("RADI");
            return Ok("DOBAR ZAHTEV");
        }

        [HttpPost("comment")]
        public async Task<ActionResult<string>> AddComment(comment comm)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = userService.addComment(id, comm);
            if(!res)
                return BadRequest();
            return Ok();
        }

        [HttpGet("comments")]
        public async Task<ActionResult<string>> GetComments(int postid)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<comments> res = userService.GetComments(postid);
            if (res == null)
                return BadRequest();
            return Ok(res);
        }

        [HttpDelete("comment")]
        public async Task<ActionResult<string>> DeleteComment(deletecom com)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = userService.DeleteComment(com.commid, com.postId, id);
            if (!res)
                return BadRequest();
            return Ok();
        }

        [HttpPut("comment")]
        public async Task<ActionResult<string>> EditComment(editcom com)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = userService.EditComment(com.commid, com.postId, com.newtext, id);
            if (!res)
                return BadRequest();
            return Ok();
        }
    }
}
