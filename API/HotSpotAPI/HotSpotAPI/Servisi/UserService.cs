using HotSpotAPI.Data;
using System.Security.Claims;

namespace HotSpotAPI.Servisi
{
    public interface IUserService
    {
        public int GetUserId();
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
            if (httpContext.HttpContext.User.FindFirstValue("id") != null)
            {
                rez = int.Parse(httpContext.HttpContext.User.FindFirstValue("id"));
                var dbid = context.Korisnici.Find(rez);
                if (dbid == null)
                    rez = -1;
            }
            return rez;
        }
    }
}
