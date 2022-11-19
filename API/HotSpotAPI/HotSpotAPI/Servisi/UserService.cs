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
        public string getPhoto(int id);
        public bool ChangePhoto(int id, IFormFile photo);
        public userinfo getUserInfo(int id);
        public bool deleteAccount(int id);
        public bool followUser(int userid, int followid);
        public List<follower> getfollowUser(int id);
        public bool unfollowUser(int userid, int followid);
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
        public bool deleteAccount(int id)
        {
            var user = context.Korisnici.Find(id);
            if (user == null)
                return false;

            bool znak = false;
            if (user.ProfileImage != null && user.ProfileImage != "")
                znak = true;

            context.Korisnici.Remove(user);
            context.SaveChanges();

            bool res = storageService.deleteAcc(id, znak);
            if (!res)
                return false;

            List<Post> posts = context.Postovi.Where(x=>x.UserID == id).ToList();
            foreach(Post p in posts)
            {
                context.Postovi.Remove(p);
                context.SaveChanges();
            }

            List<Like> lajkovi = context.Likes.Where(x => x.UserID == id).ToList();
            
            foreach(Like l in lajkovi)
            {
                Post p = context.Postovi.FirstOrDefault(x => x.ID == l.PostID);
                if(p!=null)
                    p.NumOfLikes--;
                context.SaveChanges();

                context.Likes.Remove(l);
                context.SaveChanges();
            }

            List<Followers> followers = context.Followers.Where(x=>x.userID == id || x.followID==id).ToList();
            foreach(Followers f in followers)
            {
                context.Followers.Remove(f);
                context.SaveChanges();
            }
            return true;
        }
        public userinfo getUserInfo(int id)
        {
            var user = context.Korisnici.Find(id);
            if (user == null)
                return null;
            userinfo u = new userinfo();
            u.username = user.Username;

            string slika = getPhoto(id);
            if (slika == "" || slika == null)
            {
                u.photo = "";
            }
            else
            {
                Byte[] b = System.IO.File.ReadAllBytes(slika);
                u.photo = Convert.ToBase64String(b, 0, b.Length);
            }
            return u;
        }
        public string getUsernameById(int id)
        {
            Korisnik name = context.Korisnici.Find(id);
            if (name != null)
                return name.Username;
            return "";
        }
        public bool followUser(int userid, int followid)
        {
            var fol = context.Followers.FirstOrDefault(x => x.userID == userid && x.followID == followid);
            if (fol != null)
                return false;

            Followers f = new Followers();
            f.userID = userid;
            f.followID = followid;

            context.Followers.Add(f);
            context.SaveChanges();

            return true;
        }
        public bool unfollowUser(int userid, int followid)
        {
            var fol = context.Followers.FirstOrDefault(x => x.userID == userid && x.followID == followid);
            if (fol == null)
                return false;

            context.Followers.Remove(fol);
            context.SaveChanges();
            return true;
        }
        public List<follower> getfollowUser(int id)
        {
            var fol = context.Followers.Where(x => x.userID == id).ToList();
            if (fol == null)
                return null;

            List<follower> fols = new List<follower>();
            foreach(Followers f in fol)
            {
                follower f1 = new follower();
                f1.username = getUsernameById(f.followID);
                if (f1.username == "" || f1.username == null)
                    return null;
                f1.ID = f.followID;
                fols.Add(f1);
            }
            return fols;
            
        }

    }
}
