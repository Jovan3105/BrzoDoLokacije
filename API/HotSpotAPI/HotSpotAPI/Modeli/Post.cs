namespace HotSpotAPI.Modeli
{
    public class Post
    {
        public int ID { get; set; }
        public int UserID { get; set; }
        public string Description { get; set; }
        public DateTime DateTime { get; set; }
        public string Location { get; set; }
        public double longitude { get; set; }
        public double latitude { get; set; }
        public int NumOfLikes { get; set; } = 0;
        public int NumOfPhotos { get; set; }
        public string shortDescription { get; set; }
    }
}
