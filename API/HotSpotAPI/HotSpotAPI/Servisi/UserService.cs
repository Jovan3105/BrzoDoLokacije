using HotSpotAPI.Data;
using System.Security.Claims;
using Microsoft.AspNetCore.Identity;
using HotSpotAPI.Modeli;
using HotSpotAPI.ModeliZaZahteve;
using System.Diagnostics;

namespace HotSpotAPI.Servisi
{
    public interface IUserService
    {
        public int GetUserId();
        string chengePassInDataBase(password password, out bool ind);
        public string ConfirmCode(string username, string code, out bool ind);
        public bool checkCode(vercode ver);
        public void verifyUser(string username);
        public bool ChangePhoto(int id, IFormFile photo);
        public userinfo getUserInfo(int idFollow,int idUser);
        public bool deleteAccount(int id);
        public bool followUser(int userid, int followid);
        public List<follower> getfollowUser(int id);
        public bool unfollowUser(int userid, int followid);
        public specialInfo getSpecialInfo(int id);
        public List<follower> getPageFollowers(int id, int brojstrane, int brojkorisnika);
        public List<follower> getMyFollowes(int id);
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

            List<History> history = context.History.Where(x => x.userID == id).ToList();
            foreach (History h in history)
            {
                context.History.Remove(h);
                context.SaveChanges();
            }

            return true;
        }
        public userinfo getUserInfo(int idFollow, int idUser)
        {
            var user = context.Korisnici.Find(idFollow);
            if (user == null)
                return null;
            userinfo u = new userinfo();
            var pom = context.Followers.FirstOrDefault(x => x.userID == idUser && x.followID == idFollow);
            if (pom != null)
                u.following = true;
            else
                u.following=false;

            string basepath1 = storageService.CreatePhoto();
            if (user.ProfileImage == "" || user.ProfileImage == null)
                u.photo = "";
            else
            {
                u.photo = Directory.GetFiles(basepath1, "user" + pom.ID + ".jpg")
                                 .Select(Path.GetFileName)
                                 .ToList().First();
            }
            u.username = user.Username;
            u.email=user.Email;
            return u;
        }
        public string getUsernameById(int id)
        {
            Korisnik name = context.Korisnici.Find(id);
            if (name != null)
                return name.Username;
            return "";
        }
        public string getUserPhoto(int id)
        {
            Korisnik user = context.Korisnici.Find(id);
            if(user!=null)
            {
                if(user.ProfileImage != null || user.ProfileImage!="")
                {
                    Byte[] b = System.IO.File.ReadAllBytes(user.ProfileImage);
                    return Convert.ToBase64String(b, 0, b.Length);
                }
                return "1";
            }
            return "0";
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
        public List<follower> getMyFollowes(int id)
        {
            var fol = context.Followers.Where(x => x.followID == id).ToList();
            Debug.WriteLine(fol);
            if (fol == null)
                return null;

            List<follower> fols = new List<follower>();
            string basepath1 = storageService.CreatePhoto();
            foreach (Followers f in fol)
            {
                follower f1 = new follower();
                f1.username = getUsernameById(f.userID);
                if (f1.username == "" || f1.username == null)
                    return null;
                var pom = context.Korisnici.FirstOrDefault(x => x.ID == f.userID);

                if (pom.ProfileImage == "" || pom.ProfileImage == null)
                    f1.userPhoto = "";
                else
                {
                    f1.userPhoto = Directory.GetFiles(basepath1, "user" + pom.ID + ".jpg")
                                     .Select(Path.GetFileName)
                                     .ToList().First();
                }
                f1.ID = f.userID;
                fols.Add(f1);
            }
            return fols;
        }
        public List<follower> getfollowUser(int id)
        {
            var fol = context.Followers.Where(x => x.userID == id).ToList();
            Debug.WriteLine(fol);
            if (fol == null)
                return null;

            List<follower> fols = new List<follower>();
            //string pom;
            string basepath1 = storageService.CreatePhoto();
            foreach (Followers f in fol)
            {
                follower f1 = new follower();
                f1.username = getUsernameById(f.followID);
                if (f1.username == "" || f1.username == null)
                    return null;
                var pom = context.Korisnici.FirstOrDefault(x => x.ID == f.followID);

                if (pom.ProfileImage == "" || pom.ProfileImage == null)
                    f1.userPhoto = "";
                else
                {
                    f1.userPhoto = Directory.GetFiles(basepath1, "user" + pom.ID + ".jpg")
                                     .Select(Path.GetFileName)
                                     .ToList().First();
                }
                f1.ID = f.followID;
                fols.Add(f1);
            }
            return fols;
            
        }
        public List<follower> getPageFollowers(int id, int brojstrane, int brojkorisnika)
        {
            var fol = context.Followers.Where(x => x.userID == id).ToList();
            Debug.WriteLine(fol);
            if (fol == null)
                return null;
            string basepath1 = storageService.CreatePhoto();
            List<follower> fols = new List<follower>();
            //string pom;
            for (int i = (brojstrane - 1) * brojkorisnika; i < (brojstrane * brojkorisnika); i++)
            {
                follower f1 = new follower();
                f1.username = getUsernameById(fol[i].followID);
                if (f1.username == "" || f1.username == null)
                    return null;
                var pom = context.Korisnici.FirstOrDefault(x => x.ID == fol[i].followID);

                if (pom.ProfileImage == "" || pom.ProfileImage == null)
                    f1.userPhoto = "";
                else
                {
                    f1.userPhoto = Directory.GetFiles(basepath1, "user" + pom.ID + ".jpg")
                                     .Select(Path.GetFileName)
                                     .ToList().First();
                }
                f1.ID = fol[i].followID;
                fols.Add(f1);
            }
            return fols;
        }
        public specialInfo getSpecialInfo(int id)
        {
            specialInfo si = new specialInfo();
            var followers = context.Followers.Where(x => x.followID == id).ToList();
            si.brojpratilaca = followers.Count();
            var posts = context.Postovi.Where(x=>x.UserID==id).ToList();
            si.brojpostova = posts.Count();
            int numOfLikes = 0;
            foreach(var post in posts)
            {
                numOfLikes += post.NumOfLikes;
            }
            si.prosecanbrojlajkova = (double)numOfLikes/ (double)posts.Count();
            var posts2 = context.Postovi.Where(x => x.UserID == id).Select(x => x.Location).Distinct();
            si.brojlokacija = posts2.Count();
            return si;
        }

    }
}
