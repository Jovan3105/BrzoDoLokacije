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
    public class PostController : ControllerBase
    {
        private readonly IMySQLServis mySQLServis;
        private readonly IConfiguration configuration;
        private readonly IUserService userService;
        private readonly ImailService mail;
        private readonly IStorageService storageService;
        private readonly IPostService postService;
        public PostController(IPostService postService, IConfiguration configuration, IMySQLServis mySQLServis, IUserService userService, ImailService mail, IStorageService storageService)
        {
            this.configuration = configuration;
            this.mySQLServis = mySQLServis;
            this.userService = userService;
            this.mail = mail;
            this.storageService = storageService;
            this.postService = postService;
        }
        [HttpPost("addpost")]
        public async Task<ActionResult<string>> AddPost([FromForm] addPost newPost)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.addNewPost(id, newPost);
            if (res)
                return Ok("SuccesCreatingPost");
            return BadRequest("FailedCreatingPost");
        }

        //VRACA SVE POSTOVE ULOGOVANOG KORISNIKA
        [HttpGet("getposts")]
        public async Task<ActionResult<string>> GetPosts()
        {

            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();


            List<getPosts> res = postService.getAllPosts(id);

            List<likes> lajkovi = postService.getLikes(id);

            if (res == null)
                return BadRequest("ErrorWhileGettingPosts");
            if (lajkovi != null)
                for (int i = 0; i < res.Count; i++)
                {
                    for (int j = 0; j < lajkovi.Count; j++)
                    {
                        if (lajkovi[j].postid == res[i].postID)
                        {
                            res[i].likedByMe = true;
                            lajkovi.RemoveAt(j);
                        }
                    }
                }

            return Ok(res);

        }
        //KADA KORISNIK HOCE DA VIDI PROFIL DRUGOG KORISNIKA
        [HttpGet("getpostsbyid/{userid}")]
        public async Task<ActionResult<string>> GetPostsById(int userid)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized("Unauthorized");

            List<getPosts> res = postService.getAllPosts(userid);

            List<likes> lajkovi = postService.getLikes(id);

            if (res == null)
                return BadRequest("ErrorWhileGettingPostsForSelectedUser");
            if (lajkovi != null)
                for (int i = 0; i < res.Count; i++)
                {
                    for (int j = 0; j < lajkovi.Count; j++)
                    {
                        if (lajkovi[j].postid == res[i].postID)
                        {
                            res[i].likedByMe = true;
                            lajkovi.RemoveAt(j);
                        }
                    }
                }
            return Ok(res);

        }
        //PRIKAZUJE KONKRETAN POST, POST CIJI JE ID PROSLEDJEN
        [HttpGet("getpost/{postid}")]
        public async Task<ActionResult<string>> GetPosts(int postid)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            getPosts res = postService.getPost(id, postid);

            Korisnik korisnik=userService.GetUserWithId(res.ownerID);


            List<likes> lajkovi = postService.getLikes(id);

            if (res == null)
                return BadRequest("NoPostWithThatId");
            if (lajkovi != null)
                for (int j = 0; j < lajkovi.Count; j++)
                {
                    if (lajkovi[j].postid == res.postID)
                    {
                        res.likedByMe = true;
                        break;
                    }
                }
            res.username = korisnik.Username;
            res.profilephoto = korisnik.ProfileImage;
            return Ok(res);
        }
        [HttpGet("getsorted/{sort}")]
        public async Task<ActionResult<string>> GetSorted(int sort)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized("Unauthorized");
            List<getPosts> sortedlist = new List<getPosts>();
            List<getPosts> res = postService.getAllPosts();

            List<likes> lajkovi = postService.getLikes(id);

            if (res == null)
                return BadRequest("ErrorWhileGettingPosts");
            if (lajkovi != null)
                for (int i = 0; i < res.Count; i++)
                {
                    for (int j = 0; j < lajkovi.Count; j++)
                    {
                        if (lajkovi[j].postid == res[i].postID)
                        {
                            res[i].likedByMe = true;
                            lajkovi.RemoveAt(j);
                        }
                    }
                }


            //OD STARIJEG POSTA KA NOVIJEM
            if (sort == 0)
            {
                sortedlist = res.OrderBy(x => x.DateTime).ToList();
            }
            //OD MANJE LAJKOVA KA VISE
            else if (sort == 1)
            {
                sortedlist = res.OrderBy(x => x.brojlajkova).ToList();
            }
            //OD NOVIJEG KA STARIJEM
            else if (sort == 2)
            {
                sortedlist = res.OrderByDescending(x => x.DateTime).ToList();
            }
            //OD VISE KA MANJE LAJKOVA
            else if (sort == 3)
            {
                sortedlist = res.OrderByDescending(x => x.brojlajkova).ToList();
            }
            if (sortedlist != null)
                return Ok(sortedlist);
            return BadRequest("ErrorWhileGettingPostsForSelectedUser");
        }
        [HttpGet("getpostbylocation/{xosa}/{yosa}")]
        public async Task<ActionResult<string>> GetPostsNear(string xosa, string yosa)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            decimal x = decimal.Parse(xosa);
            decimal y = decimal.Parse(yosa);

            List<getPosts> res = postService.getPostsNear(x, y);
            if (res != null)
                return Ok(res);
            return BadRequest();
        }
        [HttpGet("getpostbylocation/{location}")]
        public async Task<ActionResult<string>> GetPostsByLocation(string location)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<getPosts> res = postService.getAllPostsByLocaton(location);
            if (res != null)
                return Ok(res);
            return BadRequest();
        }

        [HttpGet("coordinate")]
        public async Task<ActionResult<string>> GetUserPostCoordinates()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<coordinates> res = postService.getMyCoordinates(id);
            if (res != null)
                return Ok(res);
            return BadRequest();
        }
        [HttpGet("coordinate/{id}")]
        public async Task<ActionResult<string>> GetUserPostCoordinatesById(int userId)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<coordinates> res = postService.getMyCoordinates(userId);
            if (res != null)
                return Ok(res);
            return BadRequest();
        }
        [HttpGet("coordinate/{xosa}/{yosa}")]
        public async Task<ActionResult<string>> GetPostByCoordinate(string xosa, string yosa)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            decimal x = decimal.Parse(xosa);
            decimal y = decimal.Parse(yosa);
            List<getPosts> res = postService.getPostsByCoordinate(x, y);
            if (res != null)
                return Ok(res);
            return BadRequest();
        }
        [HttpGet("allcoordinates")]
        public async Task<ActionResult<string>> GetAllCoordinates()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<coordinates> res = postService.getCoordinates();
            if (res != null)
                return Ok(res);
            return BadRequest();
        }
        [HttpGet("getposts/{brojstrane}/{brojpostova}")]//broj postova je broj koliko postova se salje
        public async Task<ActionResult<string>> GetPostsPage(int brojstrane, int brojpostova)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<getPosts> res = postService.getPostsPage(brojstrane, brojpostova);
            if(res!=null)
                return Ok(res);
            return BadRequest();
        }
        [HttpDelete("deletepost")]
        public async Task<ActionResult<string>> DeletePost(int postID)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();
            bool res = postService.deletePost(id, postID);
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

            bool res = postService.addComment(id, comm);
            if (!res)
                return BadRequest("ErrorWhileAddingComment");
            return Ok();
        }

        [HttpPost("history")]
        public async Task<ActionResult<string>> AddHistory(history his)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.addHistory(id, his.location);
            if (!res)
                return BadRequest("ErrorWhileAddingHistory");
            return Ok();
        }

        [HttpGet("history")]
        public async Task<ActionResult<string>> GetHistory()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<history> res = postService.getHistory(id);
            if (res == null)
                return BadRequest("ErrorWhileAddingHistory");
            return Ok(res);
        }
        [HttpGet("history/popular"), AllowAnonymous]
        public async Task<ActionResult<string>> GetPopularHistory()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<pophistory> res = postService.getPopularHistory();
            if (res == null)
                return BadRequest("ErrorWhileAddingHistory");
            return Ok(res);
        }
        [HttpDelete("history")]
        public async Task<ActionResult<string>> DeleteHistory(his history)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.deleteHistory(id, history.location);
            if (!res)
                return BadRequest("ErrorWhileAddingHistory");
            return Ok();
        }
        [HttpDelete("history/deleteall")]
        public async Task<ActionResult<string>> DeleteAllHistory()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.deleteAllHistory(id);
            if (!res)
                return BadRequest("ErrorWhileAddingHistory");
            return Ok();
        }
        [HttpGet("comments/{postid}")]
        public async Task<ActionResult<string>> GetComments(int postid)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<comments> res = postService.GetComments(postid);
            if (res == null)
                return BadRequest();
            return Ok(res);
        }

        [HttpGet("comments/{postid}/replies/{commid}")]
        public async Task<ActionResult<string>> GetComments(int postid, int commid)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<comments> res = postService.GetReplies(postid, commid);
            if (res == null)
                return BadRequest();
            return Ok(res);
        }
        [HttpDelete("comment")]
        public async Task<ActionResult<string>> DeleteComment(com com)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.DeleteComment(com.commid, com.postId, id);
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

            bool res = postService.EditComment(com.commid, com.postId, com.newtext, id);
            if (!res)
                return BadRequest();
            return Ok();
        }
        [HttpGet("getUserByID/{idusera}")]
        public async Task<ActionResult<string>> getUserInfo(int idusera)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            userinfo u = userService.getUserInfo(idusera,id);
            if (u != null)
                return Ok(u);
            return BadRequest(null);
        }

        [HttpPost("like")]
        public async Task<ActionResult<string>> LikePost(likes postid)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.addLike(id, postid.postid);
            if (!res)
                return BadRequest("ConnectionError");
            return Ok(postid.postid);
        }

        [HttpPost("dislike")]
        public async Task<ActionResult<string>> DislikePost(likes postid)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.dislike(id, postid.postid);
            if (!res)
                return BadRequest();
            return Ok(postid.postid);
        }
        [HttpGet("likes")]
        public async Task<ActionResult<List<likes>>> getLikesByUser()
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            List<likes> likes = postService.getLikes(id);
            if (likes == null)
                return BadRequest();
            return Ok(likes);
        }
        [HttpPost("likecomment")]
        public async Task<ActionResult<string>> LikeComm(comlikes like)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.addCommLike(id, like.postid, like.commid);
            if (!res)
                return BadRequest();
            return Ok();
        }
        [HttpPost("dislikecomment")]
        public async Task<ActionResult<string>> DislikeComm(comlikes postid)
        {
            int id = userService.GetUserId();
            if (id == -1)
                return Unauthorized();

            bool res = postService.dislikeComm(id, postid.postid, postid.commid);
            if (!res)
                return BadRequest();
            return Ok();
        }
    }
}
