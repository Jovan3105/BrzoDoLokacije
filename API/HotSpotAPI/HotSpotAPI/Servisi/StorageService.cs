using HotSpotAPI.Data;
using HotSpotAPI.Modeli;

namespace HotSpotAPI.Servisi
{
    public interface IStorageService
    {
        public string addProfilePhoto(int id);
        public string CreatePhoto();
        public string CreatePost();
        public bool deletePost(int id, int postID, int numOfPhotos);
        public bool deleteAcc(int id, bool znak);
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

        public bool deleteAcc(int id, bool znak)
        {
            string basepath = CreatePhoto();
            if (znak)
            {
                string imgpath = Path.Combine(basepath, "user" + id+".jpg");
                File.Delete(imgpath);
            }

            List<Post> posts = context.Postovi.Where(x => x.UserID == id).ToList();
            basepath = CreatePost();
            foreach (Post p in posts)
            {
                for(int i=1; i<=p.NumOfPhotos;i++)
                {
                    string path = Path.Combine(basepath, "user" + id + "post" + p.ID +"photo"+i+".jpg");
                    File.Delete(path);
                }
            }
            return true;
        }
    }
}
