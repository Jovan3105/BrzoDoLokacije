using HotSpotAPI.Data;

namespace HotSpotAPI.Servisi
{
    public interface IStorageService
    {
        public string addProfilePhoto(int id);
        public string CreatePhoto();
    }
    public class StorageService : IStorageService
    {
        private MySqlDbContext context;

        public StorageService(MySqlDbContext context)
        {
            this.context = context;
        }

        public string addProfilePhoto(int id)
        {
            var path = @"Storage";
            return path;
        }
        public string CreatePhoto()
        {
            var path = @"Storage";
            var imgfolder = "ProfileImages";
            var imgpath = Path.Combine(path, imgfolder);
            return imgpath;
        }
    }
}
