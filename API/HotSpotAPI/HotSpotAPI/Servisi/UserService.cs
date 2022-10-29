using HotSpotAPI.Data;
using System.Security.Claims;
using Microsoft.AspNetCore.Identity;

namespace HotSpotAPI.Servisi
{
    public interface IUserService
    {
        public int GetUserId();
        public string ChangePassword(string username, out bool ind);
    }
    public class UserService : IUserService
    {
        private readonly IHttpContextAccessor httpContext;
        private MySqlDbContext context;

        public UserService(IHttpContextAccessor httpContext, MySqlDbContext context)
        {
            this.httpContext = httpContext;
            this.context = context;
        }

        public int GetUserId()
        {
            int rez = -1; // ako nije ulogovan vraca -1
            var pom = httpContext.HttpContext.User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (httpContext.HttpContext.User.FindFirstValue(ClaimTypes.NameIdentifier) != null)
            {
                rez = int.Parse(httpContext.HttpContext.User.FindFirstValue("id"));
                var dbid = context.Korisnici.Find(rez);
                if (dbid == null)
                    rez = -1;
            }
            return rez;
        }
        public string ChangePassword(string username, out bool ind)
        {
            var user = context.Korisnici.FirstOrDefault(x => x.Username == username);
            if (user == null)
            {
                ind = false;
                return "wrong username";
            }
            //POSLATI E-MAIL 

            ind = true;
            return "Proverite vas email";

        }
    }
}
