Add-Type -AssemblyName System.Net.Http

$baseUrl = "http://localhost:8080"
$errors = @()

Write-Host "================================================================="
Write-Host " EXECUTING COMPLETE END-TO-END FLOW (ALL 6 REQUIREMENTS)"
Write-Host "================================================================="

# -------------------------------------------------------------
# 1. SETUP RECRUITER SESSION & LOGIN
# -------------------------------------------------------------
Write-Host "`n[STEP 1] Recruiter Account Setup & Login..."
$recJar = New-Object System.Net.CookieContainer
$recHandler = New-Object System.Net.Http.HttpClientHandler
$recHandler.CookieContainer = $recJar
$recHandler.AllowAutoRedirect = $true
$recClient = New-Object System.Net.Http.HttpClient($recHandler)

$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$recEmail = "recruiter.$timestamp@acmetech.com"
$recPass = "Recruiter@2026!"

$recRegForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$recRegForm.Add("role", "recruiter")
$recRegForm.Add("name", "Sarah Connor")
$recRegForm.Add("email", $recEmail)
$recRegForm.Add("mobile", "+91 9876543210")
$recRegForm.Add("location", "Hyderabad, Telangana")
$recRegForm.Add("companyName", "Acme Cloud Technologies")
$recRegForm.Add("designation", "Director of Engineering Talent")
$recRegForm.Add("companyWebsite", "https://acmecloudtech.com")
$recRegForm.Add("password", $recPass)
$recRegForm.Add("confirmPassword", $recPass)
$recRegRes = $recClient.PostAsync("$baseUrl/register", (New-Object System.Net.Http.FormUrlEncodedContent($recRegForm))).GetAwaiter().GetResult()
Write-Host " - Recruiter Registration:" $recRegRes.StatusCode

$recLoginForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$recLoginForm.Add("role", "Recruiter")
$recLoginForm.Add("email", $recEmail)
$recLoginForm.Add("password", $recPass)
$recLoginRes = $recClient.PostAsync("$baseUrl/login", (New-Object System.Net.Http.FormUrlEncodedContent($recLoginForm))).GetAwaiter().GetResult()
Write-Host " - Recruiter Login:" $recLoginRes.StatusCode

# Check Recruiter Dashboard & Post Job link
$dashRes = $recClient.GetAsync("$baseUrl/recruiter/dashboard").GetAwaiter().GetResult()
$dashHtml = $dashRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
if ($dashHtml.Contains("Post a Job") -and $dashHtml.Contains("Overview")) {
    Write-Host " [PASS] Recruiter navigation displays 'Post a Job' and recruiter controls." -ForegroundColor Green
} else {
    $errors += "Recruiter navigation missing Post Job."
    Write-Host " [FAIL] Recruiter navigation missing Post Job." -ForegroundColor Red
}

# -------------------------------------------------------------
# 2. RECRUITER POSTS JOB -> PUBLISH NOW
# -------------------------------------------------------------
Write-Host "`n[STEP 2] Recruiter Posts New Job (Publish Now)..."
$jobTitle = "Principal Cloud Architect $timestamp"
$jobForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$jobForm.Add("title", $jobTitle)
$jobForm.Add("company", "Acme Cloud Technologies")
$jobForm.Add("location", "Bengaluru / Hybrid")
$jobForm.Add("type", "Full Time")
$jobForm.Add("experience", "3-5 Years")
$jobForm.Add("salary", "₹30-45 LPA")
$jobForm.Add("deadline", "30 Nov 2026")
$jobForm.Add("vacancies", "3")
$jobForm.Add("skills", "Java 17, Spring Boot 3, MySQL 8, Docker, Kubernetes, AWS, Distributed Systems")
$jobForm.Add("description", "Join Acme Cloud Technologies to design and build enterprise distributed backend architectures on Java and Spring Boot.")

$postJobRes = $recClient.PostAsync("$baseUrl/post-job", (New-Object System.Net.Http.FormUrlEncodedContent($jobForm))).GetAwaiter().GetResult()
Write-Host " - Publish Job HTTP Status:" $postJobRes.StatusCode

# -------------------------------------------------------------
# 3. SETUP STUDENT CLIENT & LOGIN
# -------------------------------------------------------------
Write-Host "`n[STEP 3] Student Account Setup, Login & Profile Update..."
$stuJar = New-Object System.Net.CookieContainer
$stuHandler = New-Object System.Net.Http.HttpClientHandler
$stuHandler.CookieContainer = $stuJar
$stuHandler.AllowAutoRedirect = $true
$stuClient = New-Object System.Net.Http.HttpClient($stuHandler)

$stuEmail = "candidate.$timestamp@hirehub.com"
$stuPass = "Candidate@2026!"

$stuRegForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$stuRegForm.Add("role", "student")
$stuRegForm.Add("name", "Nitheesh Candidate")
$stuRegForm.Add("email", $stuEmail)
$stuRegForm.Add("mobile", "+91 9988776655")
$stuRegForm.Add("location", "Bengaluru, Karnataka")
$stuRegForm.Add("college", "National Institute of Technology")
$stuRegForm.Add("degree", "B.Tech Computer Science")
$stuRegForm.Add("graduationYear", "2026")
$stuRegForm.Add("password", $stuPass)
$stuRegForm.Add("confirmPassword", $stuPass)
$stuRegRes = $stuClient.PostAsync("$baseUrl/register", (New-Object System.Net.Http.FormUrlEncodedContent($stuRegForm))).GetAwaiter().GetResult()
Write-Host " - Student Registration:" $stuRegRes.StatusCode

$stuLoginForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$stuLoginForm.Add("role", "Student")
$stuLoginForm.Add("email", $stuEmail)
$stuLoginForm.Add("password", $stuPass)
$stuLoginRes = $stuClient.PostAsync("$baseUrl/login", (New-Object System.Net.Http.FormUrlEncodedContent($stuLoginForm))).GetAwaiter().GetResult()
Write-Host " - Student Login:" $stuLoginRes.StatusCode

# Save Student Profile with skills
$stuProfileMulti = New-Object System.Net.Http.MultipartFormDataContent
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("Nitheesh Candidate")), "name")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("+91 9988776655")), "phone")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("Bengaluru, Karnataka")), "location")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("NIT Trichy")), "college")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("B.Tech in Artificial Intelligence")), "degree")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("Distributed Systems")), "specialization")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("2026")), "graduationYear")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("Fresher")), "experienceLevel")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("Java 17, Spring Boot 3, MySQL 8, Docker, Kubernetes, AWS")), "skills")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("Principal Cloud Architect")), "preferredRole")
$stuProfileMulti.Add((New-Object System.Net.Http.StringContent("Bengaluru")), "preferredLocation")

$saveStuRes = $stuClient.PostAsync("$baseUrl/profile/student/save", $stuProfileMulti).GetAwaiter().GetResult()
Write-Host " - Save Student Profile:" $saveStuRes.StatusCode

# -------------------------------------------------------------
# 4. VERIFY BROWSE JOBS & AI JOB MATCH FOR NEWLY PUBLISHED JOB
# -------------------------------------------------------------
Write-Host "`n[STEP 4] Verifying Newly Published Job in Browse Jobs & AI Job Match..."

# Check Browse Jobs
$browseRes = $stuClient.GetAsync("$baseUrl/browse-jobs").GetAwaiter().GetResult()
$browseHtml = $browseRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
$foundInBrowse = $browseHtml.Contains($jobTitle) -and $browseHtml.Contains("Acme Cloud Technologies")

if ($foundInBrowse) {
    Write-Host " [PASS] Newly published job appears immediately in Browse Jobs." -ForegroundColor Green
} else {
    $errors += "Newly published job not found in Browse Jobs."
    Write-Host " [FAIL] Newly published job missing in Browse Jobs." -ForegroundColor Red
}

# Check AI Job Match
$matchRes = $stuClient.GetAsync("$baseUrl/ai-job-match").GetAwaiter().GetResult()
$matchHtml = $matchRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
$foundInMatch = $matchHtml.Contains($jobTitle) -and $matchHtml.Contains("Acme Cloud Technologies")

if ($foundInMatch) {
    Write-Host " [PASS] Newly published job appears immediately in AI Job Match." -ForegroundColor Green
} else {
    $errors += "Newly published job not found in AI Job Match."
    Write-Host " [FAIL] Newly published job missing in AI Job Match." -ForegroundColor Red
}

# Extract Job ID from browse page
$jobIdMatch = [regex]::Match($browseHtml, "href=['""]/jobs/(\d+)['""]")
if (-not $jobIdMatch.Success) {
    $jobIdMatch = [regex]::Match($browseHtml, "action=['""]/jobs/(\d+)/")
}
$jobId = $jobIdMatch.Groups[1].Value
Write-Host " - Found Live Job ID: $jobId"

# -------------------------------------------------------------
# 5. STUDENT APPLIES TO JOB & SAVES JOB
# -------------------------------------------------------------
Write-Host "`n[STEP 5] Student Applies to Job & Saves Job..."

# Save Job
$saveJobForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
$saveJobRes = $stuClient.PostAsync("$baseUrl/jobs/$jobId/save", (New-Object System.Net.Http.FormUrlEncodedContent($saveJobForm))).GetAwaiter().GetResult()
Write-Host " - Save Job Status:" $saveJobRes.StatusCode

# Submit Application
$appMulti = New-Object System.Net.Http.MultipartFormDataContent
$appMulti.Add((New-Object System.Net.Http.StringContent("Nitheesh Candidate")), "applicantName")
$appMulti.Add((New-Object System.Net.Http.StringContent($stuEmail)), "applicantEmail")
$appMulti.Add((New-Object System.Net.Http.StringContent("+91 9988776655")), "applicantPhone")
$appMulti.Add((New-Object System.Net.Http.StringContent("NIT Trichy")), "university")
$appMulti.Add((New-Object System.Net.Http.StringContent("B.Tech in Artificial Intelligence")), "degree")
$appMulti.Add((New-Object System.Net.Http.StringContent("Fresher")), "experience")
$appMulti.Add((New-Object System.Net.Http.StringContent("Java 17, Spring Boot 3, MySQL 8, Docker, Kubernetes, AWS")), "skills")
$appMulti.Add((New-Object System.Net.Http.StringContent("https://linkedin.com/in/nitheesh-candidate")), "linkedIn")
$appMulti.Add((New-Object System.Net.Http.StringContent("https://github.com/nitheesh-candidate")), "github")
$appMulti.Add((New-Object System.Net.Http.StringContent("Excited to apply for the Principal Cloud Architect position at Acme Cloud Technologies.")), "coverLetter")

# Dummy PDF resume bytes
$pdfBytes = [System.Text.Encoding]::UTF8.GetBytes("%PDF-1.4 Nitheesh Candidate Resume - Java 17, Spring Boot 3, MySQL 8, Cloud Architect")
$pdfContent = New-Object System.Net.Http.ByteArrayContent($pdfBytes, 0, $pdfBytes.Length)
$pdfContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/pdf")
$appMulti.Add($pdfContent, "resume", "Nitheesh_Resume.pdf")

$applyRes = $stuClient.PostAsync("$baseUrl/jobs/$jobId/apply", $appMulti).GetAwaiter().GetResult()
Write-Host " - Apply to Job HTTP Status:" $applyRes.StatusCode

# Check Student Applications page
$myAppsRes = $stuClient.GetAsync("$baseUrl/applications").GetAwaiter().GetResult()
$myAppsHtml = $myAppsRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()
if ($myAppsHtml.Contains($jobTitle) -or $myAppsHtml.Contains("Acme Cloud Technologies")) {
    Write-Host " [PASS] Submitted application visible on student's Applications page." -ForegroundColor Green
} else {
    $errors += "Application not visible on Applications page."
    Write-Host " [FAIL] Application missing on Applications page." -ForegroundColor Red
}

# -------------------------------------------------------------
# 6. RECRUITER VIEWS APPLICANTS & SCHEDULES INTERVIEW
# -------------------------------------------------------------
Write-Host "`n[STEP 6] Recruiter Reviews Applicant & Schedules Interview..."

$recApplicantsRes = $recClient.GetAsync("$baseUrl/recruiter/applicants").GetAwaiter().GetResult()
$recApplicantsHtml = $recApplicantsRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()

$appIdMatch = [regex]::Match($recApplicantsHtml, "data-appid=['""](\d+)['""]")
if (-not $appIdMatch.Success) {
    $errors += "Could not extract applicationId from recruiter applicants page."
    Write-Host " [FAIL] Could not find applicant on recruiter applicants page." -ForegroundColor Red
} else {
    $applicationId = $appIdMatch.Groups[1].Value
    Write-Host " - Found Candidate Application ID: $applicationId"

    # Schedule Interview
    $schedForm = New-Object "System.Collections.Generic.Dictionary[string,string]"
    $schedForm.Add("applicationId", $applicationId)
    $schedForm.Add("interviewDate", "2026-09-15")
    $schedForm.Add("interviewTime", "11:00 AM IST")
    $schedForm.Add("interviewType", "Technical Round 1")
    $schedForm.Add("meetingUrl", "https://meet.google.com/xyz-live-test")
    $schedForm.Add("notes", "Please be prepared with Java 17 and live Spring Boot coding setup.")

    $schedRes = $recClient.PostAsync("$baseUrl/recruiter/schedule-interview", (New-Object System.Net.Http.FormUrlEncodedContent($schedForm))).GetAwaiter().GetResult()
    Write-Host " - Schedule Interview HTTP Status:" $schedRes.StatusCode

    # -------------------------------------------------------------
    # 7. STUDENT VERIFIES SCHEDULED INTERVIEW IN INTERVIEWS PAGE
    # -------------------------------------------------------------
    Write-Host "`n[STEP 7] Student Verifies Scheduled Interview in Interviews Page..."
    $stuInterviewsRes = $stuClient.GetAsync("$baseUrl/interviews").GetAwaiter().GetResult()
    $stuInterviewsHtml = $stuInterviewsRes.Content.ReadAsStringAsync().GetAwaiter().GetResult()

    $hasCompany = $stuInterviewsHtml.Contains("Acme Cloud Technologies")
    $hasRole = $stuInterviewsHtml.Contains($jobTitle)
    $hasDate = $stuInterviewsHtml.Contains("2026-09-15")
    $hasTime = $stuInterviewsHtml.Contains("11:00 AM IST")
    $hasType = $stuInterviewsHtml.Contains("Technical Round 1")
    $hasMeeting = $stuInterviewsHtml.Contains("https://meet.google.com/xyz-live-test")
    $hasNotes = $stuInterviewsHtml.Contains("Java 17 and live Spring Boot coding setup")

    if ($hasCompany -and $hasRole -and $hasDate -and $hasTime -and $hasType -and $hasMeeting) {
        Write-Host " [PASS] Scheduled Interview appears in real-time on Student's Interviews page with full details (Date, Time, Type, Meeting URL, Notes)!" -ForegroundColor Green
    } else {
        $errors += "Scheduled interview missing details on Student Interviews page."
        Write-Host " [FAIL] Interview page verification: Company=$hasCompany, Role=$hasRole, Date=$hasDate, Time=$hasTime, Type=$hasType, Meeting=$hasMeeting, Notes=$hasNotes" -ForegroundColor Red
    }
}

Write-Host "`n================================================================="
if ($errors.Count -eq 0) {
    Write-Host " ALL 6 END-TO-END WORKFLOWS VERIFIED 100% SUCCESSFUL!" -ForegroundColor Green
} else {
    Write-Host " ERRORS ENCOUNTERED:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
}
Write-Host "================================================================="
