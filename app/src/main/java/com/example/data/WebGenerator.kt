package com.example.data

object WebGenerator {

    fun getDefaultProjects(): List<WebProject> {
        return listOf(
            WebProject(
                title = "Cyberpunk AI Portal",
                description = "Futuristic AI terminal with neon glow effects and live clock",
                htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Laprri AI Portal</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    background: #080c14;
    color: #f8fafc;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    padding: 20px;
    text-align: center;
  }
  .card {
    background: #0f172a;
    border: 1px solid #00e5ff;
    box-shadow: 0 0 24px rgba(0,229,255,0.25);
    border-radius: 20px;
    padding: 32px 24px;
    max-width: 440px;
    width: 100%;
  }
  .badge {
    display: inline-block;
    background: rgba(0,230,118,0.15);
    color: #00e676;
    border: 1px solid #00e676;
    padding: 6px 14px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 1px;
    margin-bottom: 20px;
  }
  h1 {
    font-size: 28px;
    background: linear-gradient(135deg, #00e5ff, #b388ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 12px;
  }
  p {
    color: #94a3b8;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 24px;
  }
  .clock {
    font-family: monospace;
    font-size: 32px;
    color: #00e5ff;
    text-shadow: 0 0 10px #00e5ff;
    margin-bottom: 24px;
  }
  .btn {
    background: linear-gradient(135deg, #00e5ff, #7c4dff);
    color: #080c14;
    font-weight: 700;
    padding: 14px 28px;
    border-radius: 12px;
    border: none;
    cursor: pointer;
    font-size: 15px;
    transition: transform 0.2s, box-shadow 0.2s;
    width: 100%;
  }
  .btn:active {
    transform: scale(0.98);
  }
  .status {
    margin-top: 20px;
    font-size: 12px;
    color: #64748b;
  }
</style>
</head>
<body>
<div class="card">
  <div class="badge">PRO UNLOCKED • LIFETIME FREE</div>
  <h1>Laprri Neural Core</h1>
  <p>Autonomous AI interface running without subscription or login restrictions.</p>
  <div class="clock" id="liveClock">00:00:00</div>
  <button class="btn" onclick="triggerPulse()">Execute Diagnostics</button>
  <div class="status" id="diagStatus">System status: Optimum • Latency 12ms</div>
</div>
<script>
  function updateClock() {
    const now = new Date();
    document.getElementById('liveClock').innerText = now.toLocaleTimeString();
  }
  setInterval(updateClock, 1000);
  updateClock();

  function triggerPulse() {
    const status = document.getElementById('diagStatus');
    status.style.color = '#00e5ff';
    status.innerText = '⚡ Diagnostics: All Modules Online & Fully Bypassed!';
    setTimeout(() => {
      status.style.color = '#64748b';
      status.innerText = 'System status: Optimum • Latency 12ms';
    }, 3000);
  }
</script>
</body>
</html>
""".trimIndent()
            ),
            WebProject(
                title = "Personal Portfolio",
                description = "Modern dark developer portfolio with skills and project showcase",
                htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Developer Portfolio</title>
<style>
  body { background: #0b0f19; color: #fff; font-family: sans-serif; padding: 24px; margin: 0; }
  .header { text-align: center; margin-bottom: 30px; }
  .avatar { width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(45deg, #00e5ff, #7c4dff); margin: 0 auto 16px; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: bold; }
  h2 { margin: 0 0 8px 0; font-size: 24px; }
  .role { color: #00e5ff; font-size: 14px; margin-bottom: 16px; }
  .skills { display: flex; gap: 8px; flex-wrap: wrap; justify-content: center; margin-bottom: 24px; }
  .tag { background: #1e293b; color: #b388ff; padding: 6px 12px; border-radius: 8px; font-size: 12px; }
  .projects { display: flex; flex-direction: column; gap: 12px; }
  .item { background: #161f30; padding: 16px; border-radius: 12px; border-left: 4px solid #00e5ff; }
  .item h3 { margin: 0 0 4px 0; font-size: 16px; }
  .item p { margin: 0; font-size: 13px; color: #94a3b8; }
</style>
</head>
<body>
  <div class="header">
    <div class="avatar">M</div>
    <h2>Vikram / Creator</h2>
    <div class="role">Full-Stack AI Developer & Mobile Engineer</div>
    <div class="skills">
      <span class="tag">Android Compose</span>
      <span class="tag">Gemini AI</span>
      <span class="tag">Python</span>
      <span class="tag">Next.js</span>
    </div>
  </div>
  <div class="projects">
    <div class="item">
      <h3>🤖 Laprri AI Assistant</h3>
      <p>Autonomous AI Desktop and Mobile Assistant with zero login barrier.</p>
    </div>
    <div class="item">
      <h3>⚡ WebForge Engine</h3>
      <p>Generative live web development engine with instant HTML/JS compilation.</p>
    </div>
  </div>
</body>
</html>
""".trimIndent()
            )
        )
    }

    fun generateHtmlFromPrompt(prompt: String): String {
        val safeTitle = prompt.take(30).replace(Regex("[^a-zA-Z0-9 ]"), "").trim().ifBlank { "Generated App" }
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>$safeTitle</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    background: #090d16;
    color: #e2e8f0;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    padding: 24px 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .container {
    max-width: 500px;
    width: 100%;
    background: #111827;
    border-radius: 16px;
    border: 1px solid rgba(0, 229, 255, 0.3);
    padding: 24px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.5);
  }
  .header {
    border-bottom: 1px solid #1f2937;
    padding-bottom: 16px;
    margin-bottom: 20px;
  }
  h1 {
    font-size: 22px;
    color: #00e5ff;
    margin-bottom: 6px;
  }
  p {
    font-size: 14px;
    color: #94a3b8;
    line-height: 1.5;
  }
  .interactive-box {
    background: #1f2937;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 20px;
  }
  .input-field {
    width: 100%;
    padding: 12px;
    background: #0f172a;
    border: 1px solid #334155;
    border-radius: 8px;
    color: #fff;
    margin-bottom: 12px;
    font-size: 14px;
  }
  .btn {
    width: 100%;
    background: linear-gradient(135deg, #00e5ff, #7c4dff);
    color: #080c14;
    font-weight: 700;
    padding: 12px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
  }
  .output {
    margin-top: 14px;
    padding: 12px;
    background: #0b0f19;
    border-radius: 8px;
    color: #00e676;
    font-family: monospace;
    font-size: 13px;
    min-height: 40px;
    display: flex;
    align-items: center;
  }
</style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>$safeTitle</h1>
      <p>Prompt: $prompt</p>
    </div>
    <div class="interactive-box">
      <input type="text" id="userInput" class="input-field" placeholder="Enter custom parameter..." />
      <button class="btn" onclick="executeAction()">Process Action</button>
      <div class="output" id="resultOutput">Ready to execute.</div>
    </div>
  </div>
  <script>
    function executeAction() {
      const val = document.getElementById('userInput').value;
      const out = document.getElementById('resultOutput');
      out.innerText = '⚡ Executed: ' + (val ? val : 'Success with Laprri Engine');
    }
  </script>
</body>
</html>
""".trimIndent()
    }
}
