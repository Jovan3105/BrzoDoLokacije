using HotSpotAPI.Data;
using System.Security.Claims;
using Microsoft.AspNetCore.Identity;
using HotSpotAPI.Modeli;
using HotSpotAPI.ModeliZaZahteve;

namespace HotSpotAPI.Servisi
{
    public interface IUserService
    {
        public int GetUserId();
        string chengePassInDataBase(password password, out bool ind);
        public string ConfirmCode(string username, string code, out bool ind);
        public bool checkCode(vercode ver);
        public void verifyUser(string username);
        public UserInfo getUserInfo(int id);
        public string getPhoto(int id);
        public bool ChangePhoto(int id, IFormFile photo);
        public bool addNewPost(int id, addPost newPost);
        public List<getPosts> getAllPosts(int id);
        public getPosts getPost(int id, int postID);
        public bool deletePost(int id, int postID);
        public List<getPosts> getAllPostsByLocaton(string location);
        public bool addComment(int id, comment comm);
        public List<comments> GetComments(int postid);
        public bool DeleteComment(int commid, int postid, int userid);
        public bool EditComment(int commid, int postId, string newtext, int id);
    }
    public class UserService : IUserService
    {
        private readonly IHttpContextAccessor httpContext;
        private MySqlDbContext context;
        private readonly ImailService mailService;
        private readonly IMySQLServis mysqlServis;
        private readonly IStorageService storageService;

        public UserService(IHttpContextAccessor httpContext, MySqlDbContext context, ImailService mailService, IMySQLServis mysqlServis, IStorageService storageService)
        {
            this.httpContext = httpContext;
            this.context = context;
            this.mailService = mailService;
            this.mysqlServis = mysqlServis;
            this.storageService = storageService;
        }

        public int GetUserId()
        {
            int rez = -1; // ako nije ulogovan vraca -1
            var pom = httpContext.HttpContext.User.FindFirstValue("id");
            if (httpContext.HttpContext.User.FindFirstValue("id") != null)
            {
                rez = int.Parse(httpContext.HttpContext.User.FindFirstValue("id"));
                var dbid = context.Korisnici.Find(rez);
                if (dbid == null)
                    rez = -1;
            }
            return rez;
        }

        public string ConfirmCode(string username, string code, out bool ind)
        {
            var user = context.Korisnici.FirstOrDefault(x => x.Username == username);
            if (user == null)
            {
                ind = false;
                return "Username ne postoji";
            }
            int kod=int.Parse(code);
            var kod2 = context.Kodovi.FirstOrDefault(x => x.UserID == user.ID);
            if(kod2 != null && kod2.ForgotPassCode == kod)
            {
                var lozinka = context.NovaLozinka.FirstOrDefault(x=>x.UserID == user.ID);
                if(lozinka == null)
                {
                    ind = false;
                    return "Ovaj korisnik ne zeli da promeni lozinku";
                }
                mysqlServis.CreatePasswordHash(lozinka.Password, out byte[] passhash, out byte[] passsalt);
                if (passhash != null && passsalt != null)
                {
                    user.PasswordSalt = passsalt;
                    user.PasswordHash = passhash;
                    context.SaveChanges();
                    ind = true;
                    return "Uspesno izmenjena lozinka";
                }
                ind = false;
                return "neuspesno kreiranje novog passworda";
            }
            ind = false;
            return "Kod nije validan";
        }
        public string chengePassInDataBase(password password, out bool ind)
        {
            var user = context.Korisnici.FirstOrDefault(x => x.Username == password.username);
            if(user == null)
            {
                ind = false;
                return "korisnik sa ovim usernameom ne postoji";
            }

            var lozinke = context.NovaLozinka.FirstOrDefault(x => x.UserID == user.ID);
            if (lozinke == null)
            {
                Novalozinka nl = new Novalozinka();
                nl.UserID = user.ID;
                nl.Password = password.newpassword;
                context.NovaLozinka.Add(nl);
                context.SaveChanges();
            }
            else
            {
                lozinke.Password = password.newpassword;
                context.SaveChanges();
            }

            Random rnd = new Random();
            int code = rnd.Next(1000, 9999);

            var kod = context.Kodovi.FirstOrDefault(x => x.UserID == user.ID);
            if (kod != null)
            {
                kod.ForgotPassCode = code;
                context.SaveChanges();
            }

            MailData maildata = new MailData(new List<string> { user.Email }, "Izmena lozinke");
            Task<bool> sendResult = mailService.SendAsync(maildata, new CancellationToken(), code.ToString());
            if (sendResult != null)
            {
                ind = true;
                return "Proverite vas email i pratite dalja uputstva za izmenu lozinke";
            }
            else
            {
                ind = false;
                return "greska pri slanju e-mail-a";
            }
        }
        public bool checkCode(vercode ver)
        {
            var user = context.Korisnici.FirstOrDefault(x => x.Username == ver.username);
            if (user == null)
                return false;

            var code = context.Kodovi.FirstOrDefault(x => x.UserID == user.ID);
            if(code!=null && code.RegisterCode == int.Parse(ver.code))
            {
                return true;
            }
            return false;
        }
        public void verifyUser(string username)
        {
            var user = context.Korisnici.FirstOrDefault(x => x.Username == username);
            if (user != null)
            {
                user.EmailPotvrdjen = true;
                context.SaveChanges();
            }
        }
        public UserInfo getUserInfo(int id)
        {
            var kor = context.Korisnici.Find(id);
            if (kor == null)
                return null;

            UserInfo ui = new UserInfo();
        

            return ui;
        }
        public string getPhoto(int id)
        {
            var kor = context.Korisnici.Find(id);
            if (kor == null)
                return null;

            return kor.ProfileImage;
        }
        public bool ChangePhoto(int id, IFormFile photo)
        {
            var user = context.Korisnici.Find(id);
            if (user == null)
                return false;

            string path = storageService.CreatePhoto();
            if(!Directory.Exists(path))
                Directory.CreateDirectory(path);

            path = Path.Combine(path, "user" + id + ".jpg");
            if(File.Exists(path))
                System.IO.File.Delete(path);

            context.SaveChanges();

            using (FileStream stream = System.IO.File.Create(path))
            {
                photo.CopyTo(stream);
                stream.Flush();
            }

            return true;
        }
        public bool addNewPost(int id, addPost newPost)
        {
            var user = context.Korisnici.Find(id);
            if (user == null)
                return false;

            Post p = new Post();
            p.UserID = id;
            p.Description = newPost.description;
            p.Location = newPost.location;
            p.DateTime = DateTime.Now;
            p.NumOfPhotos = newPost.photos.Count;
            context.Postovi.Add(p);
            context.SaveChanges();

            string basepath = storageService.CreatePost();
            if (!Directory.Exists(basepath))
                Directory.CreateDirectory(basepath);

            int brojac = 1;
            foreach(IFormFile slika in newPost.photos)
            {
                string path = Path.Combine(basepath, "user" + id + "post"+p.ID+"photo"+brojac+".jpg");
                using (FileStream stream = System.IO.File.Create(path))
                {
                    slika.CopyTo(stream);
                    stream.Flush();
                }
                brojac += 1;
            }
            return true;
        }
        public List<getPosts> getAllPosts(int id)
        {
            List<Post> posts = context.Postovi.Where(x=>x.UserID == id).ToList();
            List<getPosts> postsList = new List<getPosts>();
            
            foreach(Post post in posts)
            {
                getPosts p = new getPosts();
                p.description = post.Description;
                p.location = post.Location;
                p.DateTime = post.DateTime;
                p.photos = new List<string>();
                p.brojslika = post.NumOfPhotos;
                string basepath = storageService.CreatePost();
                basepath = Path.Combine(basepath, "user" + id+"post"+post.ID);
                for (int i=1;i<=post.NumOfPhotos;i++)
                {
                    string path = Path.Combine(basepath + "photo" + i+".jpg");
                    Byte[] b = System.IO.File.ReadAllBytes(path);
                    string slika = Convert.ToBase64String(b, 0, b.Length);
                    p.photos.Add(slika);
                }
                postsList.Add(p);
            }

            return postsList;
        }
        public List<getPosts> getAllPostsByLocaton(string location)
        {
            List<Post> posts = context.Postovi.Where(x => x.Location.Equals(location)).ToList();
            List<getPosts> postsList = new List<getPosts>();

            foreach (Post post in posts)
            {
                getPosts p = new getPosts();
                p.description = post.Description;
                p.location = post.Location;
                p.DateTime = post.DateTime;
                p.photos = new List<string>();
                p.brojslika = post.NumOfPhotos;
                string basepath = storageService.CreatePost();
                basepath = Path.Combine(basepath, "user" + post.UserID + "post" + post.ID);
                for (int i = 1; i <= post.NumOfPhotos; i++)
                {
                    string path = Path.Combine(basepath + "photo" + i + ".jpg");
                    Byte[] b = System.IO.File.ReadAllBytes(path);
                    string slika = Convert.ToBase64String(b, 0, b.Length);
                    p.photos.Add(slika);
                }
                postsList.Add(p);
            }

            return postsList;
        }
        public getPosts getPost(int id, int postID)
        {
            Post post = context.Postovi.FirstOrDefault(x => x.UserID == id && x.ID == postID);
            if (post == null)
                return null;
            getPosts p = new getPosts();
            p.description = post.Description;
            p.location = post.Location;
            p.DateTime = post.DateTime;
            p.photos = new List<string>();
            p.brojslika = post.NumOfPhotos;
            string basepath = storageService.CreatePost();
            basepath = Path.Combine(basepath, "user" + id + "post" + post.ID);
            for (int i = 1; i <= post.NumOfPhotos; i++)
            {
                string path = Path.Combine(basepath + "photo" + i + ".jpg");
                Byte[] b = System.IO.File.ReadAllBytes(path);
                string slika = Convert.ToBase64String(b, 0, b.Length);
                p.photos.Add(slika);
            }
            return p;
        }
        public bool deletePost(int id, int postID)
        {
            Post post = context.Postovi.FirstOrDefault(x => x.UserID == id && x.ID == postID);
            if(post == null)
                return false;

            bool res = storageService.deletePost(id, postID, post.NumOfPhotos);
            if (!res)
                return false;

            context.Remove(post);
            context.SaveChanges();
            return true;
        }
        public bool addComment(int id, comment comm)
        {
            var post = context.Postovi.FirstOrDefault(x => x.ID == comm.postid);
            if (post == null)
                return false;

            Komentari kom = new Komentari();
            kom.PostID = comm.postid;
            kom.DateTime = DateTime.Now;
            kom.Text = comm.text;
            kom.UserID = id;
            context.Komentari.Add(kom);
            context.SaveChanges();

            return true;
        }

        public List<comments> GetComments(int postid)
        {
            var komentari = context.Komentari.Where(x=>x.PostID == postid).ToList();
            if(komentari==null)
                return null;

            List<comments> koms = new List<comments>();
            foreach(Komentari c in komentari)
            {
                comments kom = new comments();
                kom.OwnerID = c.UserID;
                kom.text = c.Text;
                kom.time = c.DateTime;

                koms.Add(kom);
            }
            return koms;
        }
        public bool DeleteComment(int commid, int postid, int userid)
        {
            var com = context.Komentari.FirstOrDefault(x=>x.ID== commid && x.PostID == postid && x.UserID == userid);
            if (com == null)
                return false;

            context.Komentari.Remove(com);
            context.SaveChanges();
            return true;
        }
        public bool EditComment(int commid, int postId, string newtext, int id)
        {
            var com = context.Komentari.FirstOrDefault(x => x.ID == commid && x.PostID == postId && x.UserID == id);
            if (com == null)
                return false;

            com.Text = newtext;
            com.DateTime = DateTime.Now;
            context.SaveChanges();

            return true;
        }
    }
}
