class Notifier
  def initialize(mail_prog = "/usr/bin/mailx")
    @mail_prog = mail_prog
  end

  def send_email(dest, subject, msg)
    File.open("|/usr/bin/mailx -t -s \"#{subject}\"") do
      |file|
      file.write("To: #{dest}\n")
      if (msg.kind_of? File)
        basename = File.basename
        attachment = `
        file.write(msg + "\n")
      else
        
  end
end
