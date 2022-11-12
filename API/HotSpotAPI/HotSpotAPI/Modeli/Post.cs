namespace HotSpotAPI.Modeli
{
    public class Post
    {
        public int ID { get; set; }
        public int UserID { get; set; }
        public string Description { get; set; }
        public DateTime DateTime { get; set; }
        public string Location { get; set; }
        public int NumOfLikes { get; set; } = 0;
        public int NumOfPhotos { get; set; }
    }
}
