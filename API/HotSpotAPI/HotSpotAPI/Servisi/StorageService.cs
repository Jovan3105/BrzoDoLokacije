using HotSpotAPI.Data;

namespace HotSpotAPI.Servisi
{
    public interface IStorageService
    {
        public string addProfilePhoto(int id);
        public string CreatePhoto();
        public string CreatePost();
        public bool deletePost(int id, int postID, int numOfPhotos);
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

        public string CreatePost()
        {
            var path = @"Storage";
            var imgfolder = "PostsFolder";
            var imgpath = Path.Combine(path, imgfolder);
            return imgpath;
        }
        public bool deletePost(int id, int postID, int numOfPhotos)
        {
            var basepath = this.CreatePost();
            basepath = Path.Combine(basepath, "user" + id + "post" + postID);
            for (int i=1; i<=numOfPhotos; i++)
            {
                var path = Path.Combine(basepath+"photo"+i+".jpg");
                File.Delete(path);
            }
            return true;
        }
    }
}
