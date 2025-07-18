require("dotenv").config();
const functions = require("firebase-functions");
const express = require("express");
const nodemailer = require("nodemailer");

const app = express();
app.use(express.json());

// קרא מה‑.env
const GMAIL_USER = process.env.GMAIL_USER;
const GMAIL_PASS = process.env.GMAIL_PASS;
if (!GMAIL_USER || !GMAIL_PASS) {
  throw new Error("Missing GMAIL_USER or GMAIL_PASS in .env");
}

// אתחל את המיילר
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: { user: GMAIL_USER, pass: GMAIL_PASS },
});

app.post("/", async (req, res) => {
  console.log(">>> REQUEST BODY >>>\n", JSON.stringify(req.body, null, 2), "\n<<< END REQUEST BODY <<<");

  const { size, concept, description, imageUrls, userName, userEmail, userPhone } = req.body;
  if (!size || !concept || !description) {
    return res.status(400).send("Missing required fields");
  }

  const images = Array.isArray(imageUrls) ? imageUrls : [];
  const imagesHtml = images.map(url => `<img src="${url}" style="max-width:300px;"/><br/>`).join("");

  const mailOptions = {
    from: GMAIL_USER,
    to: "lidars.studio1@gmail.com", // כתובת המייל של לידר
    subject: "New Tattoo Request",
    html: `
      <h2>New Tattoo Request</h2>
      <p><strong>User Name:</strong> ${userName || "Not provided"}</p>
      <p><strong>User Email:</strong> ${userEmail || "Not provided"}</p>
      <p><strong>User Phone:</strong> ${userPhone || "Not provided"}</p>
      <hr/>
      <p><strong>Size:</strong> ${size}</p>
      <p><strong>Concept:</strong> ${concept}</p>
      <p><strong>Description:</strong><br/>${description.replace(/\n/g, "<br/>")}</p>
      ${imagesHtml}
    `,
  };

  console.log(">>> MAIL HTML >>>\n", mailOptions.html, "\n<<< END MAIL HTML <<<");

  try {
    await transporter.sendMail(mailOptions);
    res.status(200).send("Email sent successfully via Gmail");
  } catch (err) {
    console.error("Error sending mail:", err);
    res.status(500).send("Failed to send email");
  }
});

exports.sendTattooRequest = functions.https.onRequest(app);

exports.getServerTime = functions.https.onCall((data, context) => {
  return { serverTimestamp: Date.now() };
});
