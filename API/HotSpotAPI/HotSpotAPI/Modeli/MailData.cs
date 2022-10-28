namespace HotSpotAPI.Modeli
{
    public class MailData
    {
        public List<string> To { get; }
        public List<string> Bcc { get; }
        public List<string> Cc { get; }
        // Posiljaoc
        public string? From { get; }
        public string? DisplayName { get; }
        public string? ReplyTo { get; }
        public string? ReplyToName { get; }
        // Sadrzaj
        public string Subject { get; }
        public string? Body { get; }
        public MailData(List<string> to, string subject, string? body = null, string? from = null, string? displayName = null, string? replyTo = null, string? replyToName = null, List<string>? bcc = null, List<string>? cc = null)
        {
            // Primalac
            To = to;
            Bcc = bcc ?? new List<string>();
            Cc = cc ?? new List<string>();
            // Posiljaoc
            From = from;
            DisplayName = displayName;
            ReplyTo = replyTo;
            ReplyToName = replyToName;

            // Sadrzaj
            Subject = subject;
            Body = body;
        }
    }
}
