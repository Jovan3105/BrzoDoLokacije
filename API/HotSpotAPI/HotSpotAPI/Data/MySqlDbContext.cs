using HotSpotAPI.Modeli;
using Microsoft.EntityFrameworkCore;

namespace HotSpotAPI.Data
{
    public class MySqlDbContext : DbContext
    {
        public MySqlDbContext(DbContextOptions<MySqlDbContext> options) : base(options) { }
        public DbSet<Korisnik> Korisnici { get; set; }
        


    }

}
