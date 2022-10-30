using HotSpotAPI.Modeli;
using HotSpotAPI.Ostalo;
using Microsoft.Extensions.Options;
using MailKit.Net.Smtp;
using MailKit.Security;
using MimeKit;
using System.Text;
using RazorEngineCore;
using System.Diagnostics;
using MimeKit.Text;

namespace HotSpotAPI.Servisi
{
    public interface ImailService
    {
        Task<bool> SendAsync(MailData mailData, CancellationToken ct, string username = "");
        string GetEmailTemplate<T>(string emailTemplate, T emailTemplateModel);
    }
    public class MailService : ImailService
    {

        private readonly MailPodesavanja _settings;
        public MailService(IOptions<MailPodesavanja> settings)
        {
            _settings = settings.Value;
        }
        public string LoadTemplate(string emailTemplate)
        {
            string baseDir = Directory.GetCurrentDirectory();
            string templateDir = Path.Combine(baseDir, "Ostalo/EmailTemplejt");
            string templatePath = Path.Combine(templateDir, $"{emailTemplate}.cshtml");
            Debug.WriteLine(templatePath);
            using FileStream fileStream = new FileStream(templatePath, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
            using StreamReader streamReader = new StreamReader(fileStream, Encoding.Default);
            string mailTemplate = streamReader.ReadToEnd();
            streamReader.Close();
            return mailTemplate;
        }
        public string GetEmailTemplate<T>(string emailTemplate, T emailTemplateModel)
        {
            string mailTemplate = LoadTemplate(emailTemplate);
            IRazorEngine razorEngine = new RazorEngine();
            IRazorEngineCompiledTemplate modifiedMailTemplate = razorEngine.Compile(mailTemplate);
            return modifiedMailTemplate.Run(emailTemplateModel);
        }

        public async Task<bool> SendAsync(MailData mailData, CancellationToken ct, string username = "")
        {
            try
            {
                // Initialize a new instance of the MimeKit.MimeMessage class
                var mail = new MimeMessage();
                #region Sender / Receiver
                // Sender
                mail.From.Add(new MailboxAddress(_settings.DisplayName, mailData.From ?? _settings.From));
                mail.Sender = new MailboxAddress(mailData.DisplayName ?? _settings.DisplayName, mailData.From ?? _settings.From);
                // Receiver
                foreach (string mailAddress in mailData.To)
                    mail.To.Add(MailboxAddress.Parse(mailAddress));
                // Set Reply to if specified in mail data
                if (!string.IsNullOrEmpty(mailData.ReplyTo))
                    mail.ReplyTo.Add(new MailboxAddress(mailData.ReplyToName, mailData.ReplyTo));
                // BCC
                // Check if a BCC was supplied in the request
                if (mailData.Bcc != null)
                {
                    // Get only addresses where value is not null or with whitespace. x = value of address
                    foreach (string mailAddress in mailData.Bcc.Where(x => !string.IsNullOrWhiteSpace(x)))
                        mail.Bcc.Add(MailboxAddress.Parse(mailAddress.Trim()));
                }
                // CC
                // Check if a CC address was supplied in the request
                if (mailData.Cc != null)
                {
                    foreach (string mailAddress in mailData.Cc.Where(x => !string.IsNullOrWhiteSpace(x)))
                        mail.Cc.Add(MailboxAddress.Parse(mailAddress.Trim()));
                }
                #endregion
                #region Content
                // Add Content to Mime Message
                var body = new BodyBuilder();
                mail.Subject = mailData.Subject;
                body.HtmlBody = mailData.Body;
                if (username != "")
                {
                    mail.Body = new TextPart(TextFormat.Html) { Text = "Vaš link za verifikaciju je:http://localhost:5140/username="+username+"/" };
                }
                else
                    mail.Body = new TextPart(TextFormat.Html) { Text = "Vaš link za verifikaciju je:http://localhost:5140/"};
                #endregion
                #region Send Mail
                using var smtp = new SmtpClient();
                if (_settings.UseSSL)
                {
                    await smtp.ConnectAsync(_settings.Host, _settings.Port, SecureSocketOptions.SslOnConnect, ct);
                }
                else if (_settings.UseStartTls)
                {
                    await smtp.ConnectAsync(_settings.Host, _settings.Port, SecureSocketOptions.StartTls, ct);
                }
                await smtp.AuthenticateAsync(_settings.UserName, _settings.Password, ct);
                await smtp.SendAsync(mail, ct);
                await smtp.DisconnectAsync(true, ct);

                #endregion
                return true;
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return false;
            }
        }
    }
}
