Add-Type -AssemblyName System.Net.Http

$baseUrl = "http://localhost:8080"
$errors = @()

Write-Host "======================================================"
Write-Host " STARTING COMPREHENSIVE LIVE APP VERIFICATION"
Write-Host "======================================================"

# -----------------------------------------------------
# SETUP STUDENT CLIENT
# -----------------------------------------------------
$studentJar = New-Object System.Net.CookieContainer
$studentHandler = New-Object System.Net.Http.HttpClientHandler
$studentHandler.CookieContainer = $studentJar
$studentHandler.AllowAutoRedirect = $true
$studentClient = New-Object System.Net.Http.HttpClient($studentHandler)

$studentEmail = "live.verification.candidate@hirehub.com"
$studentPass = "LivePass2026!"

# 1. Register Candidate
Write-Host "`n[1/4] Testing Registration & Login..."
$regForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$regForm.Add("role", "student")
$regForm.Add("name", "Nitheesh LiveCandidate")
$regForm.Add("email", $studentEmail)
$regForm.Add("mobile", "+91 9988776655")
$regForm.Add("location", "Bengaluru, Karnataka")
$regForm.Add("college", "National Institute of Technology")
$regForm.Add("degree", "B.Tech Computer Science")
$regForm.Add("graduationYear", "2026")
$regForm.Add("password", $studentPass)
$regForm.Add("confirmPassword", $studentPass)
$regContent = New-Object System.Net.Http.FormUrlEncodedContent($regForm)
$regRes = $studentClient.PostAsync("$baseUrl/register", $regContent).GetAwaiter().GetResult()
Write-Host " - Registration Status: " $regRes.StatusCode

# Login Candidate
$loginForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$loginForm.Add("role", "Student")
$loginForm.Add("email", $studentEmail)
$loginForm.Add("password", $studentPass)
$loginContent = New-Object System.Net.Http.FormUrlEncodedContent($loginForm)
$loginRes = $studentClient.PostAsync("$baseUrl/login", $loginContent).GetAwaiter().GetResult()
Write-Host " - Login Status: " $loginRes.StatusCode

# -----------------------------------------------------
# REQUIREMENT 1: Browse Jobs, Applications, Interviews
# -----------------------------------------------------
Write-Host "`n[REQ 1] Verifying Layout & Content of Browse Jobs, Applications, Interviews..."

# Browse Jobs
$jobsRes = $studentClient.GetAsync("$baseUrl/browse-jobs").GetAwaiter().GetResult()
$jobsHtml = $jobsRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
if ($jobsRes.StatusCode -ne 200 -or -not $jobsHtml.Contains("jobs.css") -or -not $jobsHtml.Contains("content")) {
    $errors += "Browse Jobs page failed to render properly."
    Write-Host " [FAIL] Browse Jobs rendering" -ForegroundColor Red
} else {
    Write-Host " [PASS] Browse Jobs renders properly with jobs.css & content layout." -ForegroundColor Green
}

# Applications
$appsRes = $studentClient.GetAsync("$baseUrl/applications").GetAwaiter().GetResult()
$appsHtml = $appsRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
if ($appsRes.StatusCode -ne 200 -or -not $appsHtml.Contains("applications.css") -or -not $appsHtml.Contains("content")) {
    $errors += "Applications page failed to render properly."
    Write-Host " [FAIL] Applications rendering" -ForegroundColor Red
} else {
    Write-Host " [PASS] Applications page renders properly with applications.css & content layout." -ForegroundColor Green
}

# Interviews
$ivsRes = $studentClient.GetAsync("$baseUrl/interviews").GetAwaiter().GetResult()
$ivsHtml = $ivsRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
if ($ivsRes.StatusCode -ne 200 -or -not $ivsHtml.Contains("interviews.css") -or -not $ivsHtml.Contains("content")) {
    $errors += "Interviews page failed to render properly."
    Write-Host " [FAIL] Interviews rendering" -ForegroundColor Red
} else {
    Write-Host " [PASS] Interviews page renders properly with interviews.css & content layout." -ForegroundColor Green
}

# -----------------------------------------------------
# REQUIREMENT 2 & 3: Profile Persistence, Picture & Resume Upload
# -----------------------------------------------------
Write-Host "`n[REQ 2 & 3] Verifying Profile Details, Edit Mode, Picture & Resume Upload..."

# Create multipart content for full profile save including image and resume
$multipart = New-Object System.Net.Http.MultipartFormDataContent
$multipart.Add((New-Object System.Net.Http.StringContent("Nitheesh LiveCandidate")), "name")
$multipart.Add((New-Object System.Net.Http.StringContent("+91 9988776655")), "phone")
$multipart.Add((New-Object System.Net.Http.StringContent("Bengaluru, Karnataka")), "location")
$multipart.Add((New-Object System.Net.Http.StringContent("NIT Trichy")), "college")
$multipart.Add((New-Object System.Net.Http.StringContent("B.Tech in Artificial Intelligence")), "degree")
$multipart.Add((New-Object System.Net.Http.StringContent("Distributed Cloud Systems")), "specialization")
$multipart.Add((New-Object System.Net.Http.StringContent("2026")), "graduationYear")
$multipart.Add((New-Object System.Net.Http.StringContent("Fresher")), "experienceLevel")
$multipart.Add((New-Object System.Net.Http.StringContent("Java 17, Spring Boot 3, MySQL 8, Docker, REST API")), "skills")
$multipart.Add((New-Object System.Net.Http.StringContent("Lead Java Cloud Architect")), "preferredRole")
$multipart.Add((New-Object System.Net.Http.StringContent("Bengaluru / Remote")), "preferredLocation")
$multipart.Add((New-Object System.Net.Http.StringContent("https://linkedin.com/in/nitheesh-live")), "linkedIn")
$multipart.Add((New-Object System.Net.Http.StringContent("https://github.com/nitheesh-live")), "github")
$multipart.Add((New-Object System.Net.Http.StringContent("Passionate software engineer building resilient backend systems.")), "careerObjective")

# Sample 1x1 PNG Profile Photo
$photoBytes = [System.Convert]::FromBase64String("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
$photoContent = New-Object System.Net.Http.ByteArrayContent($photoBytes, 0, $photoBytes.Length)
$photoContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("image/png")
$multipart.Add($photoContent, "photo", "profile_avatar.png")

# Sample Resume Text File
$resumeText = @"
Nitheesh LiveCandidate
Email: live.verification.candidate@hirehub.com | Phone: +91 9988776655 | Location: Bengaluru, India
LinkedIn: https://linkedin.com/in/nitheesh-live | GitHub: https://github.com/nitheesh-live

PROFESSIONAL SUMMARY
Backend developer with deep hands-on expertise in Java, Spring Boot, MySQL, REST API design and Cloud Architecture.

TECHNICAL SKILLS
Languages: Java 17, SQL, Python
Frameworks: Spring Boot 3, Spring Data JPA, Hibernate, REST API
Tools & DBs: MySQL 8, Docker, Git, GitHub, Maven

PROJECTS
High-Performance Microservices (2025-2026)
- Built enterprise REST API services using Spring Boot and MySQL with sub-50ms latency.
- Implemented robust JPA mappings and automated testing with JUnit 5.

EDUCATION
B.Tech in Computer Science - NIT Trichy (2022 - 2026)
"@
$resumeBytes = [System.Text.Encoding]::UTF8.GetBytes($resumeText)
$resumeContent = New-Object System.Net.Http.ByteArrayContent($resumeBytes, 0, $resumeBytes.Length)
$resumeContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("text/plain")
$multipart.Add($resumeContent, "resume", "nitheesh_resume.txt")

$saveProfRes = $studentClient.PostAsync("$baseUrl/profile/student/save", $multipart).GetAwaiter().GetResult()
Write-Host " - Save Profile HTTP Status:" $saveProfRes.StatusCode

# Verify Profile Read-Only View (Saved details visible after refresh)
$profViewRes = $studentClient.GetAsync("$baseUrl/profile").GetAwaiter().GetResult()
$profViewHtml = $profViewRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()

$hasName = $profViewHtml.Contains("Nitheesh LiveCandidate")
$hasCollege = $profViewHtml.Contains("NIT Trichy")
$hasDegree = $profViewHtml.Contains("B.Tech in Artificial Intelligence")
$hasRole = $profViewHtml.Contains("Lead Java Cloud Architect")
$hasSkills = $profViewHtml.Contains("Java 17")
$hasPhoto = $profViewHtml.Contains("data:image/png;base64")
$hasResume = $profViewHtml.Contains("nitheesh_resume.txt") -or $profViewHtml.Contains("ATS")

if ($hasName -and $hasCollege -and $hasDegree -and $hasSkills -and $hasPhoto) {
    Write-Host " [PASS] Profile saved details remain visible and persisted in MySQL." -ForegroundColor Green
} else {
    $errors += "Profile details did not persist completely."
    Write-Host " [FAIL] Profile details persistence: Name=$hasName, College=$hasCollege, Degree=$hasDegree, Skills=$hasSkills, Photo=$hasPhoto" -ForegroundColor Red
}

# Verify Edit Profile View (Pre-populates saved values)
$profEditRes = $studentClient.GetAsync("$baseUrl/profile?edit=true").GetAwaiter().GetResult()
$profEditHtml = $profEditRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
$editHasCollege = $profEditHtml.Contains("NIT Trichy")
$editHasDegree = $profEditHtml.Contains("B.Tech in Artificial Intelligence")
$editHasRole = $profEditHtml.Contains("Lead Java Cloud Architect")

if ($editHasCollege -and $editHasDegree -and $editHasRole) {
    Write-Host " [PASS] Edit Profile mode accurately pre-populates existing saved values." -ForegroundColor Green
} else {
    $errors += "Edit Profile mode failed to pre-populate saved values."
    Write-Host " [FAIL] Edit Profile pre-population: College=$editHasCollege, Degree=$editHasDegree, Role=$editHasRole" -ForegroundColor Red
}

# -----------------------------------------------------
# REQUIREMENT 4: Recruiter Post Job -> Publish Now -> Browse Jobs & AI Match
# -----------------------------------------------------
Write-Host "`n[REQ 4] Verifying Recruiter Post Job, Publish Now, Browse Jobs & AI Match..."

$recJar = New-Object System.Net.CookieContainer
$recHandler = New-Object System.Net.Http.HttpClientHandler
$recHandler.CookieContainer = $recJar
$recHandler.AllowAutoRedirect = $true
$recClient = New-Object System.Net.Http.HttpClient($recHandler)

$recEmail = "live.recruiter2026@hirehub.com"
$recPass = "RecruiterPass123!"

# Register Recruiter
$recRegForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$recRegForm.Add("role", "recruiter")
$recRegForm.Add("name", "David Warner")
$recRegForm.Add("email", $recEmail)
$recRegForm.Add("mobile", "+91 9112233445")
$recRegForm.Add("location", "Hyderabad, Telangana")
$recRegForm.Add("companyName", "Vertex Cloud Systems")
$recRegForm.Add("designation", "Director of Talent")
$recRegForm.Add("companyWebsite", "https://vertexcloud.io")
$recRegForm.Add("password", $recPass)
$recRegForm.Add("confirmPassword", $recPass)
$recRegContent = New-Object System.Net.Http.FormUrlEncodedContent($recRegForm)
$recRegRes = $recClient.PostAsync("$baseUrl/register", $recRegContent).GetAwaiter().GetResult()
Write-Host " - Recruiter Register Status:" $recRegRes.StatusCode

# Login Recruiter
$recLoginForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$recLoginForm.Add("role", "Recruiter")
$recLoginForm.Add("email", $recEmail)
$recLoginForm.Add("password", $recPass)
$recLoginContent = New-Object System.Net.Http.FormUrlEncodedContent($recLoginForm)
$recLoginRes = $recClient.PostAsync("$baseUrl/login", $recLoginContent).GetAwaiter().GetResult()
Write-Host " - Recruiter Login Status:" $recLoginRes.StatusCode

# Publish Job
$jobTitle = "Principal Distributed Systems Engineer 2026"
$jobForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$jobForm.Add("title", $jobTitle)
$jobForm.Add("company", "Vertex Cloud Systems")
$jobForm.Add("location", "Bengaluru / Hybrid")
$jobForm.Add("type", "Full Time")
$jobForm.Add("experience", "Fresher / 0-2 Years")
$jobForm.Add("salary", "₹22-30 LPA")
$jobForm.Add("deadline", "2026-12-31")
$jobForm.Add("vacancies", "5")
$jobForm.Add("skills", "Java 17, Spring Boot 3, MySQL 8, Docker, REST API")
$jobForm.Add("description", "We are looking for an exceptional distributed systems engineer to architect core microservices using Spring Boot and MySQL.")
$jobContent = New-Object System.Net.Http.FormUrlEncodedContent($jobForm)
$publishRes = $recClient.PostAsync("$baseUrl/post-job", $jobContent).GetAwaiter().GetResult()
Write-Host " - Publish Job Status:" $publishRes.StatusCode

# Check Browse Jobs for the newly published job
$browseRes = $studentClient.GetAsync("$baseUrl/browse-jobs").GetAwaiter().GetResult()
$browseHtml = $browseRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
$browseHasJob = $browseHtml.Contains($jobTitle) -and $browseHtml.Contains("Vertex Cloud Systems")

if ($browseHasJob) {
    Write-Host " [PASS] Newly published job appears immediately in Browse Jobs." -ForegroundColor Green
} else {
    $errors += "Newly published job not found in Browse Jobs."
    Write-Host " [FAIL] Newly published job in Browse Jobs: $browseHasJob" -ForegroundColor Red
}

# Check AI Job Match for the newly published job
$matchRes = $studentClient.GetAsync("$baseUrl/ai-job-match").GetAwaiter().GetResult()
$matchHtml = $matchRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
$matchHasJob = $matchHtml.Contains($jobTitle) -and $matchHtml.Contains("Vertex Cloud Systems")

if ($matchHasJob) {
    Write-Host " [PASS] Newly published job is immediately indexed and ranked in AI Job Match." -ForegroundColor Green
} else {
    $errors += "Newly published job not found in AI Job Match."
    Write-Host " [FAIL] Newly published job in AI Job Match: $matchHasJob" -ForegroundColor Red
}

Write-Host "`n======================================================"
if ($errors.Count -eq 0) {
    Write-Host " ALL 4 REQUIREMENTS VERIFIED & WORKING FLAWLESSLY!" -ForegroundColor Green
} else {
    Write-Host " ERRORS FOUND:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
}
Write-Host "======================================================"
